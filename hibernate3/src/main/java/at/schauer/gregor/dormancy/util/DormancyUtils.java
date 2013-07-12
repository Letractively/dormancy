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

import at.schauer.gregor.dormancy.access.HibernatePropertyAccessStrategy;
import at.schauer.gregor.dormancy.access.AbstractPropertyAccessStrategy;
import at.schauer.gregor.dormancy.access.StrategyPropertyAccessor;
import at.schauer.gregor.dormancy.persistence.HibernatePersistenceUnitProvider;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.SuppressWarnings;
import java.lang.reflect.Field;

/**
 * Utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 */
public class DormancyUtils extends AbstractDormancyUtils<SessionFactory, Session, ClassMetadata,
		HibernatePersistenceUnitProvider> {

	public DormancyUtils(HibernatePersistenceUnitProvider persistenceUnitProvider) {
		super(persistenceUnitProvider);
	}

	@Nullable
	@Override
	public ClassMetadata getClassMetadata(@Nullable Class<?> clazz) {
		return clazz != null ? persistenceUnitProvider.getMetadata(clazz) : null;
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

	/**
	 * @inheritDoc
	 * @see ClassMetadata#getMappedClass(org.hibernate.EntityMode)
	 */
	@Override
	public Class<?> getMappedClass(@Nonnull ClassMetadata metadata) {
		return metadata.getMappedClass(EntityMode.POJO);
	}

	@Override
	public boolean isPersistentCollection(Object obj) {
		return obj instanceof PersistentCollection;
	}

	/**
	 * @inheritDoc
	 * @see org.hibernate.collection.PersistentCollection#wasInitialized()
	 */
	@Override
	public boolean isInitializedPersistentCollection(Object obj) {
		return isPersistentCollection(obj) && PersistentCollection.class.cast(obj).wasInitialized();
	}

	@Nonnull
	@Override
	public String getEntityName(@Nonnull Class<?> clazz) {
		ClassMetadata metadata = getClassMetadata(clazz);
		return metadata != null ? metadata.getEntityName() : clazz.getSimpleName();
	}

	@Override
	protected String getIdentifierPropertyName(@Nonnull Class<?> clazz) {
		ClassMetadata metadata = getClassMetadata(clazz);
		return metadata != null ? metadata.getIdentifierPropertyName() : null;
	}

	@Nonnull
	@Override
	public Class<?> getPropertyType(@Nonnull Class<?> clazz, @Nonnull String propertyName) {
		ClassMetadata metadata = getClassMetadata(clazz);
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

	@Nonnull
	@Override
	protected AbstractPropertyAccessStrategy createStrategy(@Nonnull Class<?> clazz) {
		return new HibernatePropertyAccessStrategy(clazz);
	}

	@Override
	public boolean isVersioned(@Nonnull ClassMetadata metadata) {
		return metadata.isVersioned();
	}

	@Override
	public String getVersionPropertyName(@Nonnull ClassMetadata metadata) {
		int index = metadata.getVersionProperty();
		return index >= 0 ? metadata.getPropertyNames()[index] : null;
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

	@Nonnull
	@Override
	public Session getPersistenceContext() {
		return persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();
	}

	/**
	 * Throws a {@link TransientObjectException} indicating that the object has no valid identifier.
	 *
	 * @param object the object
	 */
	@Override
	public void throwNullIdentifierException(@Nonnull Object object) {
		throw new TransientObjectException("The given object has a null identifier: " + getEntityName(getClass(object)));
	}

	/**
	 * Throws {@link TransientObjectException} indicating that the object must be saved manually before continuing.
	 *
	 * @param object the object
	 */
	@Override
	public void throwUnsavedTransientInstanceException(@Nonnull Object object) {
		throw new TransientObjectException(
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
		throw new StaleObjectStateException(getEntityName(getClass(dbValue)), identifier);
	}

	@Override
	public void throwObjectNotFoundException(Serializable identifier, Object trObj) {
		throw new ObjectNotFoundException(identifier, getClass(trObj).getSimpleName());
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
