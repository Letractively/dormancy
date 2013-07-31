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
import at.dormancy.access.HibernatePropertyAccessStrategy;
import at.dormancy.access.StrategyPropertyAccessor;
import at.dormancy.persistence.HibernatePersistenceUnitProvider;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 */
public class DormancyUtils extends AbstractDormancyUtils<SessionFactory, Session, ClassMetadata,
		HibernatePersistenceUnitProvider> {

	@Inject
	public DormancyUtils(@Nonnull HibernatePersistenceUnitProvider persistenceUnitProvider) {
		super(persistenceUnitProvider);
	}

	@Nullable
	@Override
	public ClassMetadata getMetadata(@Nullable Class<?> clazz) {
		return clazz != null ? persistenceUnitProvider.getMetadata(clazz) : null;
	}

	/**
	 * @see ClassMetadata#getMappedClass(org.hibernate.EntityMode)
	 */
	@Nonnull
	@Override
	public Class<?> getMappedClass(@Nonnull ClassMetadata metadata) {
		return metadata.getMappedClass(EntityMode.POJO);
	}

	@Nonnull
	@Override
	public String getEntityName(@Nonnull Class<?> clazz) {
		ClassMetadata metadata = getMetadata(clazz);
		return metadata != null ? metadata.getEntityName() : clazz.getSimpleName();
	}

	@Nullable
	@Override
	protected String getIdentifierPropertyName(@Nonnull Class<?> clazz) {
		ClassMetadata metadata = getMetadata(clazz);
		return metadata != null ? metadata.getIdentifierPropertyName() : null;
	}

	@Nullable
	@Override
	public <T> Serializable getIdentifier(@Nonnull ClassMetadata metadata, @Nonnull T bean) {
		Serializable identifier = metadata.getIdentifier(bean, EntityMode.POJO);
		if (identifier == null) {
			identifier = Serializable.class.cast(getPropertyValue(metadata, bean, metadata.getIdentifierPropertyName()));
		}
		return identifier;
	}

	@Override
	public boolean isVersioned(@Nonnull ClassMetadata metadata) {
		return metadata.isVersioned();
	}

	@Nullable
	@Override
	public String getVersionPropertyName(@Nonnull ClassMetadata metadata) {
		int index = metadata.getVersionProperty();
		return index >= 0 ? metadata.getPropertyNames()[index] : null;
	}

	@Nullable
	@Override
	public Object getPropertyValue(@Nullable ClassMetadata metadata, @Nonnull Object bean, @Nonnull String propertyName) {
		IntrospectorUtils.getDescriptorMap(getClass(bean));
		return IntrospectorUtils.getValue(bean, propertyName);
	}

	@Override
	public void setPropertyValue(@Nullable ClassMetadata metadata, @Nonnull Object bean, @Nonnull String propertyName, @Nullable Object value) {
		IntrospectorUtils.getDescriptorMap(getClass(bean));
		IntrospectorUtils.setValue(bean, propertyName, value);
	}

	@Nonnull
	@Override
	public Class<?> getPropertyType(@Nonnull Class<?> clazz, @Nonnull String propertyName) {
		ClassMetadata metadata = getMetadata(clazz);
		if (metadata != null) {
			try {
				return metadata.getPropertyType(propertyName).getReturnedClass();
			} catch (QueryException e) {
				if (ArrayUtils.contains(metadata.getPropertyNames(), propertyName)) {
					throw e;
				}
			}
		}
		return ReflectionUtils.findField(clazz, propertyName).getType();
	}

	@Nullable
	@Override
	public PropertyAccessor getPropertyAccessor(@Nullable ClassMetadata metadata, @Nonnull Object obj) {
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
		return new StrategyPropertyAccessor(obj, getPropertyAccessStrategy(AopUtils.getTargetClass(obj)));
	}

	@Nonnull
	@Override
	protected AbstractPropertyAccessStrategy createStrategy(@Nonnull Class<?> clazz) {
		return new HibernatePropertyAccessStrategy(clazz);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T find(@Nonnull Class<T> clazz, @Nonnull Serializable id) {
		return (T) getPersistenceContext().get(clazz, id);
	}

	@Override
	public void flush() {
		getPersistenceContext().flush();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <K> K persist(@Nonnull Object obj) {
		return (K) getPersistenceContext().save(obj);
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
	 * Throws a {@link TransientObjectException} indicating that the object has no valid identifier.
	 *
	 * @param object the object
	 * @throws TransientObjectException the exception
	 */
	@Override
	public RuntimeException throwNullIdentifierException(@Nonnull Object object) {
		throw new TransientObjectException("The given object has a null identifier: " + getEntityName(getClass(object)));
	}

	/**
	 * Throws a {@link TransientObjectException} indicating that the object must be saved manually before continuing.
	 *
	 * @throws TransientObjectException the exception
	 */
	@Override
	public RuntimeException throwUnsavedTransientInstanceException(@Nonnull Object object) {
		throw new TransientObjectException(
				"object references an unsaved transient instance - save the transient instance before flushing: " +
						getEntityName(getClass(object))
		);
	}

	/**
	 * Throws a {@link PropertyValueException} when a lazy property holds a value while {@code null} was expected.
	 *
	 * @throws PropertyValueException the exception
	 */
	@Override
	public RuntimeException throwLazyPropertyNotNullException(@Nonnull Object trValue, @Nonnull Object dbObj, @Nonnull String propertyName) {
		throw new PropertyValueException("Property is loaded lazily. Therefore it must be null but was: " + trValue,
				getEntityName(getClass(dbObj)), propertyName);
	}

	/**
	 * Throws a {@link StaleObjectStateException} when an optimistic locking conflict occurs.
	 *
	 * @throws StaleObjectStateException the exception
	 */
	@Override
	public RuntimeException throwOptimisticLockException(@Nonnull Object dbValue, @Nonnull Serializable identifier) {
		throw new StaleObjectStateException(getEntityName(getClass(dbValue)), identifier);
	}

	/**
	 * Throws an {@link ObjectNotFoundException} when an entity reference is accessed but the entity does not exist.
	 *
	 * @throws ObjectNotFoundException the exception
	 */
	@Override
	public RuntimeException throwEntityNotFoundException(@Nonnull Serializable identifier, @Nonnull Object trObj) {
		throw new ObjectNotFoundException(identifier, getClass(trObj).getSimpleName());
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
	public Session getPersistenceContext() {
		return persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();
	}
}
