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

import org.apache.log4j.Logger;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.annotation.Annotation;
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
	protected static final String JAVAX_PERSISTENCE_ID = "javax.persistence.Id";
	@Nonnull
	protected static final Class<? extends Annotation> idClass;

	static {
		try {
			idClass = (Class<? extends Annotation>) Class.forName(JAVAX_PERSISTENCE_ID);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find class: " + JAVAX_PERSISTENCE_ID, e);
		}
	}

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
	 * <p/>
	 * Note that this method does not use {@link org.hibernate.Hibernate#getClass(Object) Hibernate.getClass(Object)}
	 * to avoid the initialization of the proxy.
	 *
	 * @param proxy a persistable object or proxy
	 * @return the true class of the instance
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> Class<T> getClass(@Nonnull Object proxy) {
		Class<?> clazz = proxy.getClass();
		while (isJavassistProxy(clazz)) {
			clazz = clazz.getSuperclass();
		}
		return (Class<T>) clazz;
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
	@Nonnull
	public Set<String> getPropertyNames(@Nonnull Object obj) {
		return IntrospectorUtils.getDescriptorMap(getClass(obj)).keySet();
	}

	/**
	 * Returns a {@link PropertyAccessor} for accessing the objects properties.
	 * <p/>
	 * This method automatically detects the best strategy for accessing field values, which is either field access
	 * (by using {@link org.springframework.beans.BeanWrapper BeanWrapper} or property access if necessary (by using
	 * {@link org.springframework.beans.DirectFieldAccessor DirectFieldAccessor}.
	 * <p/>
	 * The decision is made by applying the following algorithm:<br/>
	 * If there is Hibernate class metadata available and
	 * <ul>
	 * <li>if the {@code javax.persistence.Id} annotation was found on a getter method or</li>
	 * <li>if the object is a proxy modified by Javassist and its {@link LazyInitializer} is not accessible</li>
	 * </ul>
	 * the properties are access via getter and setter methods.<br/>
	 * Otherwise the fields are accessed directly via reflection.
	 * <p/>
	 * <b>Note that if the given object is an uninitialized Javassist proxy, the object becomes initialized immediately
	 * if the direct field access strategy is chosen.<br/>
	 * Otherwise Hibernate will trigger the initialization automatically upon getter invocation as usual.</b>
	 *
	 * @param metadata the class metadata
	 * @param obj      the object
	 * @return the property accessor
	 */
	@Nonnull
	public PropertyAccessor getPropertyAccessor(@Nullable ClassMetadata metadata, @Nonnull Object obj) {
		if (metadata != null) {
			Field idField = ReflectionUtils.findField(obj.getClass(), metadata.getIdentifierPropertyName());
			if (AnnotationUtils.getAnnotation(idField, idClass) == null) {
				return PropertyAccessorFactory.forBeanPropertyAccess(obj);
			}
			if (isJavassistProxy(obj.getClass())) {
				// If the object is a proxy, attempt to find its LazyInitializer
				Field handlerField = ReflectionUtils.findField(obj.getClass(), "handler");
				if (handlerField != null) {
					// If the LazyInitializer is available, obtain the underlying object
					LazyInitializer lazyInitializer = (LazyInitializer) ReflectionTestUtils.getField(obj, "handler");
					// Initialize it if necessary and return a DirectFieldAccessor for the nested object
					return PropertyAccessorFactory.forDirectFieldAccess(lazyInitializer.getImplementation());
				} else {
					/*
					 * Otherwise log a warning message because this is very unlikely to happen or even impossible.
					 * However, instead of throwing an exception, property access is used as a fallback solution.
					 */
					Logger.getLogger(getClass()).warn("Cannot retrieve field named 'handler' of type 'org.hibernate.proxy.LazyInitializer' from " + ObjectUtils.identityToString(obj));
					return PropertyAccessorFactory.forBeanPropertyAccess(obj);
				}
			}
		}
		return PropertyAccessorFactory.forDirectFieldAccess(obj);
	}
}
