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

import org.hibernate.Hibernate;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

/**
 * Common utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 */
public abstract class AbstractDormancyUtils {
	protected AbstractDormancyUtils() {
	}

	/**
	 * Iterates over the given collection, looking for an object which is semantically equal to a certain object.<br/>
	 * An object is semantically equal if one of the following conditions apply:
	 * <ul>
	 * <li>the given object overrides the {@link #equals(Object)} equals methods, which returns {@code true} for
	 * another object</li>
	 * <li>the given object has a non-null identifier and its type as well as the identifier value is equals to the
	 * type and identifier value of another object</li>
	 * </ul>
	 *
	 * @param obj        the object
	 * @param collection the collection to traverse
	 * @param session    the Hibernate Session to use
	 * @return the object found or {@code null} if the collection does not contain such an object
	 * @see #getIdentifierValue(org.hibernate.metadata.ClassMetadata, Object, org.hibernate.Session)
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T findPendant(@Nonnull T obj, @Nonnull Collection<?> collection, @Nonnull Session session) {
		try {
			Method method = obj.getClass().getMethod("equals", Object.class);
			if (method.getDeclaringClass() != Object.class) {
				// If the given object overrides the equals() method, invoke it for every object in the collection
				for (Object elem : collection) {
					if (obj.equals(elem) && collection.remove(elem)) {
						return (T) elem;
					}
				}
			} else {
				// Otherwise get the Hibernate metadata and a PropertyAccessor to get the identifier
				ClassMetadata objMetadata = getClassMetadata(obj, session.getSessionFactory());
				Object objIdentifier = getIdentifier(objMetadata, obj, session);

				// For every object in the collection, check if the type matches and if the identifier is equal
				for (Object elem : collection) {
					if (elem != null && elem.getClass() == obj.getClass()
							&& objIdentifier.equals(getIdentifier(objMetadata, elem, session))
							&& collection.remove(elem)) {
						return (T) elem;
					}
				}
			}
			return null;
		} catch (NoSuchMethodException e) {
			// Must not happen because Object defines an equals() method
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the {@link ClassMetadata} associated with the given entity class.
	 *
	 * @param obj            the object to retrieve ClassMetadata for
	 * @param sessionFactory the SessionFactory to use
	 * @return the ClassMetadata or {@code null} if the type is not an Hibernate managed entity
	 * @see #getClassMetadata(Class, org.hibernate.SessionFactory)
	 * @see #getClass(Object)
	 */
	@Nullable
	public ClassMetadata getClassMetadata(@Nullable Object obj, @Nonnull SessionFactory sessionFactory) {
		return obj != null ? getClassMetadata(getClass(obj), sessionFactory) : null;
	}

	/**
	 * Gets the {@link ClassMetadata} associated with the given entity class.
	 *
	 * @param clazz          the type to retrieve ClassMetadata for
	 * @param sessionFactory the SessionFactory to use
	 * @return the ClassMetadata or {@code null} if the type is not an Hibernate managed entity
	 */
	@Nullable
	public ClassMetadata getClassMetadata(@Nullable Class<?> clazz, @Nonnull SessionFactory sessionFactory) {
		return clazz != null ? sessionFactory.getClassMetadata(clazz) : null;
	}

	/**
	 * Attempts to get the identifier of the given object by using the provided {@link ClassMetadata} or
	 * {@link PropertyAccessor}.
	 *
	 * @param metadata the ClassMetadata of the object (may be null)
	 * @param bean     the object
	 * @param session  the Hibernate session to use
	 * @return the identifier or {@code null} if the identifier cannot be retrieved or is {@code null}
	 * @see #getIdentifierValue(org.hibernate.metadata.ClassMetadata, Object, org.hibernate.Session)
	 */
	@Nullable
	public abstract <T> Serializable getIdentifier(@Nonnull ClassMetadata metadata, @Nonnull T bean, @Nonnull Session session);

	/**
	 * Retrieves the identifier of the given object by using the provided {@link ClassMetadata} or
	 * {@link PropertyAccessor}.
	 * If the identifier cannot be retrieved, an {@link org.hibernate.PropertyValueException} is thrown.
	 *
	 * @param metadata the ClassMetadata of the object (may be null)
	 * @param bean     the object
	 * @param session  the Hibernate session to use
	 * @return the identifier or {@code null} if the identifier cannot be retrieved or is {@code null}
	 * @see #getIdentifier(org.hibernate.metadata.ClassMetadata, Object, org.hibernate.Session)
	 */
	@Nonnull
	public <T> Serializable getIdentifierValue(@Nonnull ClassMetadata metadata, @Nonnull T bean, @Nonnull Session session) {
		Serializable identifier = getIdentifier(metadata, bean, session);
		if (identifier == null) {
			// If the identifier of the database object is null, it is really null, which indicates a database problem, or it cannot be retrieved
			throw new PropertyValueException("Cannot read identifier", metadata.getEntityName(), metadata.getIdentifierPropertyName());
		}
		return identifier;
	}

	/**
	 * Retrieves the property of the given object by using the provided {@link ClassMetadata} or
	 * {@link PropertyAccessor}.
	 *
	 * @param metadata     the ClassMetadata of the object (may be null)
	 * @param bean         the object
	 * @param propertyName the name of the property
	 * @return the property
	 * @see PropertyAccessor#getPropertyValue(String)
	 */
	@Nullable
	public abstract Object getPropertyValue(@Nullable ClassMetadata metadata, @Nonnull Object bean, @Nonnull String propertyName);

	/**
	 * Sets the property of the given object by using the provided {@link ClassMetadata} or {@link PropertyAccessor}.
	 *
	 * @param metadata     the ClassMetadata of the object (may be null)
	 * @param bean         the object
	 * @param propertyName the name of the property
	 * @param value        the value to set
	 * @see PropertyAccessor#setPropertyValue(String, Object)
	 */
	public abstract void setPropertyValue(@Nullable ClassMetadata metadata, @Nonnull Object bean, @Nonnull String propertyName, @Nullable Object value);

	/**
	 * Gets the unproxified type of the given object.
	 *
	 * @param proxy a persistable object or proxy
	 * @return the true class of the instance
	 *
	 * @see Hibernate#getClass(Object)
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> Class<T> getClass(@Nonnull Object proxy) {
		try {
			return Hibernate.getClass(proxy);
		} catch (Exception ignored) {
			// If the object is not present in the session, use the fallback solution for determining the type.
			Class<?> clazz = proxy.getClass();
			while (isJavassistProxy(clazz)) {
				clazz = clazz.getSuperclass();
			}
			return (Class<T>) clazz;
		}
	}

	/**
	 * Returns the persistent class, or {@code null}.
	 *
	 * @param metadata the class metadata
	 * @return the persistent class, or {@code null}
	 */
	@Nullable
	public abstract Class<?> getMappedClass(@Nonnull ClassMetadata metadata);

	/**
	 * Checks if the given class is a Hibernate proxy that has been generated by using Javassist.
	 *
	 * @param clazz the class to check
	 * @return {@code true} if the given class is a Javassist proxy.
	 */
	public boolean isJavassistProxy(@Nonnull Class<?> clazz) {
		return clazz.getSimpleName().contains("$$_javassist");
	}

	/**
	 * Checks if the given object is a {@code PersistentCollection}.
	 *
	 * @param obj the object to check
	 * @return {@code true} if the object is a {@code PersistentCollection}, {@code false} otherwise
	 * @see #isInitializedPersistentCollection(Object)
	 */
	public abstract boolean isPersistentCollection(@Nullable Object obj);

	/**
	 * Checks if the given object is an initialized {@code PersistentCollection}.
	 *
	 * @param obj the object to check
	 * @return {@code true} if the object is an initialized {@code PersistentCollection}, {@code false} otherwise
	 * @see #isPersistentCollection(Object)
	 */
	public abstract boolean isInitializedPersistentCollection(@Nullable Object obj);

	/**
	 * Returns the property names of the given object.
	 *
	 * @param obj The object
	 * @return The property names
	 */
	public Set<String> getPropertyNames(@Nonnull Object obj) {
		return IntrospectorUtils.getDescriptorMap(getClass(obj)).keySet();
	}

	/**
	 * Returns a {@link PropertyAccessor} for accessing the objects properties.
	 *
	 * If the given objects is a Hibernate proxy modified by Javassist or the {@link Id} annotation is found on
	 * method level, a {@link org.springframework.beans.BeanWrapper BeanWrapper} is returned. Otherwise a
	 * {@link org.springframework.beans.DirectFieldAccessor DirectFieldAccessor} is returned.
	 *
	 * @param metadata the class metadata
	 * @param obj the object
	 * @return the property accessor
	 */
	@Nonnull
	public PropertyAccessor getPropertyAccessor(@Nullable ClassMetadata metadata, @Nonnull Object obj) {
		if (metadata != null) {
			if (isJavassistProxy(obj.getClass())) {
				return PropertyAccessorFactory.forBeanPropertyAccess(obj);
			}
			Field idField = ReflectionUtils.findField(obj.getClass(), metadata.getIdentifierPropertyName());
			if (AnnotationUtils.getAnnotation(idField, Id.class) == null) {
				return PropertyAccessorFactory.forBeanPropertyAccess(obj);
			}
		}
		return PropertyAccessorFactory.forDirectFieldAccess(obj);
	}
}
