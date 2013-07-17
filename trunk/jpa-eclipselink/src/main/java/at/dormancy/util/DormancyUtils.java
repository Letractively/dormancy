/*
 * Copyright 2013 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.util;

import at.dormancy.access.AbstractPropertyAccessStrategy;
import at.dormancy.access.JpaAccessTypeStrategy;
import at.dormancy.access.StrategyPropertyAccessor;
import at.dormancy.persistence.JpaPersistenceUnitProvider;
import org.eclipse.persistence.indirection.IndirectCollection;
import org.eclipse.persistence.internal.weaving.PersistenceWeaved;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.spi.LoadState;
import java.io.Serializable;

/**
 * Utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class DormancyUtils extends AbstractDormancyUtils<EntityManagerFactory, EntityManager, EntityType<?>,
		JpaPersistenceUnitProvider> {

	protected PersistenceProvider persistenceProvider = new PersistenceProvider();

	@Inject
	public DormancyUtils(@Nonnull JpaPersistenceUnitProvider persistenceUnitProvider) {
		super(persistenceUnitProvider);
	}

	@Nullable
	@Override
	public EntityType<?> getMetadata(@Nullable Class<?> clazz) {
		return persistenceUnitProvider.getMetadata(clazz);
	}

	/**
	 * @see javax.persistence.metamodel.EntityType#getJavaType()
	 */
	@Nonnull
	@Override
	public Class<?> getMappedClass(@Nonnull EntityType<?> metadata) {
		return metadata.getJavaType();
	}

	@Nonnull
	@Override
	public String getEntityName(@Nonnull Class<?> clazz) {
		EntityType<?> metadata = getMetadata(clazz);
		return metadata != null ? metadata.getName() : clazz.getSimpleName();
	}

	@Nullable
	@Override
	protected String getIdentifierPropertyName(@Nonnull Class<?> clazz) {
		EntityType<?> metadata = getMetadata(clazz);
		return metadata != null ? metadata.getId(metadata.getIdType().getJavaType()).getName() : null;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> Serializable getIdentifier(@Nonnull EntityType<?> metadata, @Nonnull T bean) {
		if (metadata.hasSingleIdAttribute()) {
			SingularAttribute<? super T, ?> idAttribute = (SingularAttribute<? super T, ?>) metadata.getId(metadata.getIdType().getJavaType());
			return (Serializable) getPropertyValue(metadata, bean, idAttribute.getName());
		} else {
			Serializable idClass = null;
			for (SingularAttribute<?, ?> idClassAttribute : metadata.getIdClassAttributes()) {
				if (idClass == null) {
					Class<?> idClassType = idClassAttribute.getJavaMember().getDeclaringClass().getAnnotation(IdClass.class).value();
					idClass = (Serializable) BeanUtils.instantiateClass(idClassType);
				}
				Object value = getPropertyValue(metadata, bean, idClassAttribute.getName());
				IntrospectorUtils.setValue(idClass, idClassAttribute.getName(), value);
			}
			return idClass;
		}
	}

	@Override
	public boolean isVersioned(@Nonnull EntityType<?> metadata) {
		return metadata.hasVersionAttribute();
	}

	@Nullable
	@Override
	public String getVersionPropertyName(@Nonnull EntityType<?> metadata) {
		if (metadata.hasVersionAttribute()) {
			SingularAttribute<?, Long> version = metadata.getVersion(null);
			if (version != null) {
				return version.getName();
			}
		}
		return null;
	}

	@Nullable
	@Override
	public Object getPropertyValue(@Nullable EntityType<?> metadata, @Nonnull Object bean, @Nonnull String propertyName) {
		return IntrospectorUtils.getValue(bean, propertyName);
	}

	@Override
	public void setPropertyValue(@Nullable EntityType<?> metadata, @Nonnull Object bean, @Nonnull String propertyName, @Nullable Object value) {
		IntrospectorUtils.setValue(bean, propertyName, value);
	}

	@Nonnull
	@Override
	public Class<?> getPropertyType(@Nonnull Class<?> clazz, @Nonnull String propertyName) {
		EntityType<?> metadata = getMetadata(clazz);
		if (metadata != null) {
			try {
				Attribute attribute = metadata.getAttribute(propertyName);
				if (attribute != null) {
					return attribute.getJavaType();
				}
			} catch (IllegalArgumentException e) {
				// May happen e.g., for transient properties
			}
		}
		return ReflectionUtils.findField(clazz, propertyName).getType();
	}

	@Nullable
	@Override
	public PropertyAccessor getPropertyAccessor(@Nullable EntityType<?> metadata, @Nonnull Object obj) {
		return metadata == null ? PropertyAccessorFactory.forDirectFieldAccess(obj)
				: new StrategyPropertyAccessor(obj, getAccessTypeStrategy(AopUtils.getTargetClass(obj)));
	}

	@Nonnull
	@Override
	protected AbstractPropertyAccessStrategy createStrategy(@Nonnull Class<?> clazz) {
		return new JpaAccessTypeStrategy(clazz);
	}

	@Nullable
	@Override
	public <T> T find(@Nonnull Class<T> clazz, @Nonnull Serializable id) {
		return getPersistenceContext().find(clazz, id);
	}

	@Override
	public void flush() {
		getPersistenceContext().flush();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <K> K persist(@Nonnull Object obj) {
		getPersistenceContext().persist(obj);
		return (K) getIdentifier(getMetadata(obj), obj);
	}

	/**
	 * @see #isInitialized(Object)
	 * @see PersistenceWeaved
	 */
	@Override
	public boolean isProxy(@Nullable Object obj) {
		return obj instanceof PersistenceWeaved;
	}

	/**
	 * @see PersistenceProvider#isLoaded(Object)
	 */
	@Override
	public boolean isInitialized(@Nullable Object obj) {
		if (isProxy(obj)) {
			return persistenceProvider.isLoaded(obj) == LoadState.LOADED;
		} else if (isPersistentCollection(obj)) {
			return IndirectCollection.class.cast(obj).isInstantiated();
		}
		return true;
	}

	@Override
	public boolean isUninitialized(@Nonnull String propertyName, @Nonnull Object dbObj, @Nonnull Object dbValue, @Nullable Object trValue) {
		LoadState state = this.persistenceProvider.isLoadedWithoutReference(dbObj, propertyName);
		if (state == LoadState.NOT_LOADED) {
			// If property is loaded lazily, the value of the given object must be null
			if (trValue != null) {
				throw throwLazyPropertyNotNullException(trValue, dbObj, propertyName);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isPersistentCollection(@Nullable Object obj) {
		return obj instanceof IndirectCollection;
	}

	/**
	 * @see IndirectCollection#isInstantiated()
	 */
	@Override
	public boolean isInitializedPersistentCollection(@Nullable Object obj) {
		return isPersistentCollection(obj) && IndirectCollection.class.cast(obj).isInstantiated();
	}

	/**
	 * Throws an {@link IllegalStateException} indicating that the object has no valid identifier.
	 *
	 * @param object the object
	 * @throws IllegalStateException the exception
	 */
	@Override
	public RuntimeException throwNullIdentifierException(@Nonnull Object object) {
		throw new IllegalStateException("The given object has a null identifier: " + getEntityName(getClass(object)));
	}

	/**
	 * Throws an {@link IllegalStateException} indicating that the object must be saved manually before continuing.
	 *
	 * @throws IllegalStateException the exception
	 */
	@Override
	public RuntimeException throwUnsavedTransientInstanceException(@Nonnull Object object) {
		throw new IllegalStateException(
				"object references an unsaved transient instance - save the transient instance before flushing: " +
						getEntityName(getClass(object))
		);
	}

	/**
	 * Throws an {@link IllegalStateException} when a lazy property holds a value while {@code null} was expected.
	 *
	 * @throws IllegalStateException the exception
	 */
	@Override
	public RuntimeException throwLazyPropertyNotNullException(@Nonnull Object trValue, @Nonnull Object dbObj, @Nonnull String propertyName) {
		throw new IllegalStateException(String.format("Property is loaded lazily. Therefore it must be null but was: %s: %s.%s",
				trValue, getEntityName(getClass(dbObj)), propertyName));
	}

	/**
	 * Throws an {@link OptimisticLockException} when an optimistic locking conflict occurs.
	 *
	 * @throws OptimisticLockException the exception
	 */
	@Override
	public RuntimeException throwOptimisticLockException(@Nonnull Object dbValue, @Nonnull Serializable identifier) {
		throw new OptimisticLockException(String.format("Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect): [%s#%s]", getEntityName(getClass(dbValue)), identifier));
	}

	/**
	 * Throws an {@link EntityNotFoundException} when an entity reference is accessed but the entity does not exist.
	 *
	 * @throws EntityNotFoundException the exception
	 */
	@Override
	public RuntimeException throwEntityNotFoundException(@Nonnull Serializable identifier, @Nonnull Object trObj) {
		throw new EntityNotFoundException(String.format("No row with the given identifier exists: [%s#%s]", getClass(trObj).getSimpleName(), identifier));
	}

	/**
	 * Throws an {@link IllegalStateException} when a property is not accessible or holds an unexpected value.
	 *
	 * @throws IllegalStateException the exception
	 */
	@Override
	public RuntimeException throwPropertyValueException(@Nonnull String message, @Nonnull Object bean) {
		throw new IllegalStateException(String.format("%s: %s.%s", message, getEntityName(bean.getClass()), getIdentifierPropertyName(bean.getClass())));
	}

	@Nonnull
	@Override
	public EntityManager getPersistenceContext() {
		return persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();
	}
}
