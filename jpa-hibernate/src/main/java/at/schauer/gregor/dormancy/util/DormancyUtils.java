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
package at.schauer.gregor.dormancy.util;

import at.schauer.gregor.dormancy.access.AbstractPropertyAccessStrategy;
import at.schauer.gregor.dormancy.access.JpaAccessTypeStrategy;
import at.schauer.gregor.dormancy.access.StrategyPropertyAccessor;
import at.schauer.gregor.dormancy.persistence.JpaPersistenceUnitProvider;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.PropertyValueException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class DormancyUtils extends AbstractDormancyUtils<EntityManagerFactory, EntityManager, EntityType<?>,
		JpaPersistenceUnitProvider> {

	@Inject
	public DormancyUtils(@Nonnull JpaPersistenceUnitProvider persistenceUnitProvider) {
		super(persistenceUnitProvider);
	}

	@Nullable
	@Override
	public EntityType<?> getMetadata(@Nullable Class<?> clazz) {
		return clazz != null ? persistenceUnitProvider.getMetadata(clazz) : null;
	}

	/**
	 * @see EntityType#getJavaType()
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
					Class<?> idClassType = idClassAttribute.getJavaMember().getDeclaringClass();
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
			SingularAttribute<?, Long> version = metadata.getVersion(Long.class);
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
		if (metadata == null) {
			return PropertyAccessorFactory.forDirectFieldAccess(obj);
		}
		if (isJavassistProxy(obj.getClass())) {
			// If the object is a proxy, attempt to find its LazyInitializer
			Field handlerField = ReflectionUtils.findField(obj.getClass(), "handler");
			if (handlerField != null) {
				// If the LazyInitializer is available, obtain the underlying object
				LazyInitializer lazyInitializer = (LazyInitializer) ReflectionTestUtils.getField(obj, "handler");
				// Initialize it if necessary and return a DirectFieldAccessor for the nested object
				obj = lazyInitializer.getImplementation();
			} else {
				/*
				 * Otherwise log a warning message because this is very unlikely to happen or even impossible.
				 * However, instead of throwing an exception, property access is used as a fallback solution.
				 */
				Logger.getLogger(getClass()).warn("Cannot retrieve field named 'handler' of type 'org.hibernate.proxy.LazyInitializer' from " + ObjectUtils.identityToString(obj));
			}
		}
		return new StrategyPropertyAccessor(obj, getAccessTypeStrategy(AopUtils.getTargetClass(obj)));
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
	 * @see HibernateProxy
	 */
	@Override
	public boolean isProxy(@Nullable Object obj) {
		return obj instanceof HibernateProxy;
	}

	/**
	 * @see Hibernate#isInitialized(Object)
	 */
	@Override
	public boolean isInitialized(@Nullable Object obj) {
		return Hibernate.isInitialized(obj);
	}

	@Override
	public boolean isUninitialized(@Nonnull String propertyName, @Nonnull Object dbObj, @Nonnull Object dbValue, @Nullable Object trValue) {
		HibernateProxy hibernateProxy = HibernateProxy.class.cast(dbValue);
		LazyInitializer lazyInitializer = hibernateProxy.getHibernateLazyInitializer();
		if (lazyInitializer.isUninitialized()) {
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
		return obj instanceof PersistentCollection;
	}

	/**
	 * @see PersistentCollection#wasInitialized()
	 */
	@Override
	public boolean isInitializedPersistentCollection(@Nullable Object obj) {
		return isPersistentCollection(obj) && PersistentCollection.class.cast(obj).wasInitialized();
	}

	/**
	 * Throws an {@link IllegalStateException} indicating that the object has no valid identifier.
	 *
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
	 * Throws an {@link PropertyValueException} when a lazy property holds a value while {@code null} was expected.
	 *
	 * @throws PropertyValueException the exception
	 */
	@Override
	public RuntimeException throwLazyPropertyNotNullException(@Nonnull Object trValue, @Nonnull Object dbObj, @Nonnull String propertyName) {
		throw new PropertyValueException("Property is loaded lazily. Therefore it must be null but was: " + trValue,
				getEntityName(getClass(dbObj)), propertyName);
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
	 * Throws a {@link PropertyValueException} when a property is not accessible or holds an unexpected value.
	 *
	 * @throws PropertyValueException the exception
	 */
	@Override
	public RuntimeException throwPropertyValueException(@Nonnull String message, @Nonnull Object bean) {
		throw new PropertyValueException(message, getEntityName(bean.getClass()), getIdentifierPropertyName(bean.getClass()));
	}

	@Nonnull
	@Override
	public EntityManager getPersistenceContext() {
		return persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();
	}
}
