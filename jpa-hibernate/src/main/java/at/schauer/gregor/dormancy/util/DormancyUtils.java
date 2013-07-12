/*
 * Copyright 2012 Gregor Schauer
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

import at.schauer.gregor.dormancy.access.AnnotationPropertyAccessStrategy;
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
import org.springframework.beans.PropertyAccessor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	public DormancyUtils(JpaPersistenceUnitProvider persistenceProvider) {
		super(persistenceProvider);
	}

	@Nullable
	@Override
	public EntityType<?> getClassMetadata(@Nullable Class<?> clazz) {
		return persistenceUnitProvider.getMetadata(clazz);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> Serializable getIdentifier(@Nonnull EntityType<?> metadata, @Nonnull T bean) {
		SingularAttribute<? super T, ?> idAttribute = (SingularAttribute<? super T, ?>) metadata.getId(metadata.getIdType().getJavaType());
		return (Serializable) getPropertyValue(metadata, bean, idAttribute.getName());
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

	@Nullable
	@Override
	public Class<?> getMappedClass(@Nonnull EntityType<?> metadata) {
		return metadata.getJavaType();
	}

	@Override
	public boolean isPersistentCollection(@Nullable Object obj) {
		return obj != null && JpaProviderUtils.getPersistentCollectionClass().isAssignableFrom(obj.getClass());
	}

	@Override
	public boolean isInitializedPersistentCollection(@Nullable Object obj) {
		return isPersistentCollection(obj) && PersistentCollection.class.cast(obj).wasInitialized();
	}

	@Nonnull
	@Override
	public String getEntityName(@Nonnull Class<?> clazz) {
		EntityType<?> metadata = getClassMetadata(clazz);
		return metadata != null ? metadata.getName() : clazz.getSimpleName();
	}

	@Nullable
	@Override
	protected String getIdentifierPropertyName(@Nonnull Class<?> clazz) {
		EntityType<?> metadata = getClassMetadata(clazz);
		return metadata != null ? metadata.getId(metadata.getIdType().getJavaType()).getName() : null;
	}

	@Nonnull
	@Override
	public Class<?> getPropertyType(@Nonnull Class<?> clazz, @Nonnull String propertyName) {
		EntityType<?> metadata = getClassMetadata(clazz);
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

	@Nonnull
	@Override
	protected AnnotationPropertyAccessStrategy createStrategy(@Nonnull Class<?> clazz) {
		return new JpaAccessTypeStrategy(clazz);
	}

	@Override
	public boolean isVersioned(@Nonnull EntityType<?> metadata) {
		return metadata.hasVersionAttribute();
	}

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
		return (K) getIdentifier(getClassMetadata(obj), obj);
	}

	@Nonnull
	@Override
	public EntityManager getPersistenceContext() {
		return this.persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();
	}

	@Nullable
	@Override
	public PropertyAccessor getPropertyAccessor(@Nullable EntityType<?> metadata, @Nonnull Object obj) {
		if (metadata != null) {
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
		}
		return new StrategyPropertyAccessor(obj, getAccessTypeStrategy(AopUtils.getTargetClass(obj)));
	}

	/**
	 * Throws a {@link org.hibernate.TransientObjectException} indicating that the object has no valid identifier.
	 *
	 * @param object the object
	 */
	@Override
	public void throwNullIdentifierException(@Nonnull Object object) {
		throw new IllegalStateException("The given object has a null identifier: " + getEntityName(getClass(object)));
	}

	/**
	 * Throws {@link org.hibernate.TransientObjectException} indicating that the object must be saved manually before continuing.
	 *
	 * @param object the object
	 */
	@Override
	public void throwUnsavedTransientInstanceException(@Nonnull Object object) {
		throw new IllegalStateException(
				"object references an unsaved transient instance - save the transient instance before flushing: " +
						getEntityName(getClass(object))
		);
	}

	@Override
	public void throwLazyPropertyNotNull(Object trValue, Object dbObj, String propertyName) {
		throw new PropertyValueException("Property is loaded lazily. Therefore it must be null but was: " + trValue,
				getEntityName(getClass(dbObj)), propertyName);
	}

	@Override
	public void throwStaleObjectStateException(Object dbValue, Serializable identifier) {
		throw new OptimisticLockException(String.format("Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect): [%s#%s]", getEntityName(getClass(dbValue)), identifier));
	}

	@Override
	public void throwObjectNotFoundException(Serializable identifier, Object trObj) {
		throw new EntityNotFoundException(String.format("No row with the given identifier exists: [%s#%s]", getClass(trObj).getSimpleName(), identifier));
	}

	@Override
	public void throwPropertyValueException(String message, Object bean) {
		throw new PropertyValueException(message, getEntityName(bean.getClass()), getIdentifierPropertyName(bean.getClass()));
	}

	@Override
	public boolean isInitialized(Object obj) {
		return Hibernate.isInitialized(obj);
	}

	@Override
	public boolean isProxy(Object dbValue) {
		return dbValue instanceof HibernateProxy;
	}

	@Override
	public boolean isUninitialized(String propertyName, Object dbObj, Object dbValue, Object trValue) {
		HibernateProxy hibernateProxy = HibernateProxy.class.cast(dbValue);
		LazyInitializer lazyInitializer = hibernateProxy.getHibernateLazyInitializer();
		if (lazyInitializer.isUninitialized()) {
			// If property is loaded lazily, the value of the given object must be null
			if (trValue != null) {
				throwLazyPropertyNotNull(trValue, dbObj, propertyName);
			}
			return true;
		}
		return false;
	}
}
