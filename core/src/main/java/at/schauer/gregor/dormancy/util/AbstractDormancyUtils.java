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

import at.schauer.gregor.dormancy.access.PropertyAccessStrategy;
import at.schauer.gregor.dormancy.access.StrategyPropertyAccessor;
import org.apache.log4j.Logger;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.PropertyAccessor;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Common utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 */
public abstract class AbstractDormancyUtils {
	protected static final Map<Class<?>, PropertyAccessStrategy> STRATEGY_MAP = new HashMap<Class<?>, PropertyAccessStrategy>();
	@Nonnull
	protected static final Class<? extends Annotation> idClass;
	protected SessionFactory sessionFactory;

	static {
		String javaxPersistenceId = "javax.persistence.Id";
		try {
			idClass = (Class<? extends Annotation>) Class.forName(javaxPersistenceId);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find class: " + javaxPersistenceId, e);
		}
	}

	protected AbstractDormancyUtils(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
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
	 * @return the object found or {@code null} if the collection does not contain such an object
	 * @see #getIdentifierValue(ClassMetadata, Object)
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T findPendant(@Nonnull T obj, @Nonnull Collection<?> collection) {
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
				ClassMetadata objMetadata = getClassMetadata(obj);
				Object objIdentifier = getIdentifier(objMetadata, obj);

				// For every object in the collection, check if the type matches and if the identifier is equal
				for (Object elem : collection) {
					if (elem != null && elem.getClass() == obj.getClass()
							&& objIdentifier.equals(getIdentifier(objMetadata, elem))
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
	 * @param obj the object to retrieve ClassMetadata for
	 * @return the ClassMetadata or {@code null} if the type is not an Hibernate managed entity
	 * @see #getClassMetadata(Class)
	 * @see #getClass(Object)
	 */
	@Nullable
	public ClassMetadata getClassMetadata(@Nullable Object obj) {
		return obj != null ? getClassMetadata(getClass(obj)) : null;
	}

	/**
	 * Gets the {@link ClassMetadata} associated with the given entity class.
	 *
	 * @param clazz the type to retrieve ClassMetadata for
	 * @return the ClassMetadata or {@code null} if the type is not an Hibernate managed entity
	 */
	@Nullable
	public abstract ClassMetadata getClassMetadata(@Nullable Class<?> clazz);

	/**
	 * Attempts to get the identifier of the given object by using the provided {@link ClassMetadata} or
	 * {@link PropertyAccessor}.
	 *
	 * @param metadata the ClassMetadata of the object (may be null)
	 * @param bean     the object
	 * @return the identifier or {@code null} if the identifier cannot be retrieved or is {@code null}
	 * @see #getIdentifierValue(ClassMetadata, Object)
	 */
	@Nullable
	public abstract <T> Serializable getIdentifier(@Nonnull ClassMetadata metadata, @Nonnull T bean);

	/**
	 * Retrieves the identifier of the given object by using the provided {@link ClassMetadata} or
	 * {@link PropertyAccessor}.
	 * If the identifier cannot be retrieved, an {@link org.hibernate.PropertyValueException} is thrown.
	 *
	 * @param metadata the ClassMetadata of the object (may be null)
	 * @param bean     the object
	 * @return the identifier or {@code null} if the identifier cannot be retrieved or is {@code null}
	 * @see #getIdentifier(ClassMetadata, Object)
	 */
	@Nonnull
	public <T> Serializable getIdentifierValue(@Nonnull ClassMetadata metadata, @Nonnull T bean) {
		Serializable identifier = getIdentifier(metadata, bean);
		if (identifier == null) {
			// If the identifier of the database object is null, it is really null, which indicates a database problem, or it cannot be retrieved
			throw new PropertyValueException("Cannot read identifier", getEntityName(bean.getClass()), getIdentifierPropertyName(bean.getClass()));
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
	 * Returns the entity name of the given class.<br/>
	 * The entity name is the unique name of the entity in the meta-model or the simple class name if the type is not
	 * known by the meta-model.
	 *
	 * @param clazz the type
	 * @return the entity name of the class
	 */
	@Nonnull
	public abstract String getEntityName(@Nonnull Class<?> clazz);

	/**
	 * Returns the name of the identifier property of the given type.<br/>
	 * If the type has no identifier property e.g., it is not Hibernate entity, {@code null} is returned instead.
	 *
	 * @param clazz the type
	 * @return the identifier property name or {@code null} if there is no identifier property
	 */
	@Nullable
	protected abstract String getIdentifierPropertyName(@Nonnull Class<?> clazz);

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
	public String[] getPropertyNames(@Nonnull Object obj) {
		Set<String> names = IntrospectorUtils.getDescriptorMap(getClass(obj)).keySet();
		return names.toArray(new String[names.size()]);
	}

	/**
	 * Returns the type of the given named property.
	 *
	 * @param clazz        the class containing the property
	 * @param propertyName the name of the property
	 * @return the type of the property
	 */
	@Nonnull
	public abstract Class<?> getPropertyType(@Nonnull Class<?> clazz, @Nonnull String propertyName);

	/**
	 * Returns a {@link PropertyAccessor} for accessing the objects properties.
	 * <p/>
	 * This method automatically detects the best strategy for accessing field values, which is either property access
	 * (by using {@link org.springframework.beans.BeanWrapper BeanWrapper} or field access (by using
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
	 * Returns the {@link at.schauer.gregor.dormancy.access.PropertyAccessStrategy} to use for accessing properties of the given type.<br/>
	 * If no strategy is defined for the type, a new one is created on-demand.
	 *
	 * @param clazz the type
	 * @return the strategy to use
	 * @see #createStrategy(Class)
	 */
	@Nonnull
	public PropertyAccessStrategy getAccessTypeStrategy(@Nonnull Class<?> clazz) {
		PropertyAccessStrategy strategy = STRATEGY_MAP.get(clazz);
		if (strategy == null) {
			synchronized (STRATEGY_MAP) {
				strategy = STRATEGY_MAP.get(clazz);
				if (strategy == null) {
					strategy = createStrategy(clazz);
					STRATEGY_MAP.put(clazz, strategy);
				}
			}
		}
		return strategy;
	}

	/**
	 * Creates a new {@link at.schauer.gregor.dormancy.access.PropertyAccessStrategy} instance for the given type.
	 *
	 * @param clazz the type
	 * @return the strategy to use
	 */
	@Nonnull
	protected abstract PropertyAccessStrategy createStrategy(@Nonnull Class<?> clazz);

	/**
	 * Returns whether the Hibernate entity associated with the given metadata is versioned.<br/>
	 *
	 * @param metadata the metadata
	 * @return {@link true}
	 */
	public abstract boolean isVersioned(@Nonnull ClassMetadata metadata);

	/**
	 * Returns the name of the property of the Hibernate entity used for versioning (if available).
	 *
	 * @param metadata the metadata
	 * @return the name of the version property or {@code null} if the Hibernate entity is not versioned
	 */
	@Nullable
	public abstract String getVersionPropertyName(@Nonnull ClassMetadata metadata);

	/**
	 * Return the persistent instance of the given entity class with the given identifier, or null if there is no such persistent instance. (If the instance is already associated with the session, return that instance. This method never returns an uninitialized instance.)
	 *
	 * @param clazz the persistent class
	 * @param id    the identifier
	 * @return the persistent instance
	 */
	@Nullable
	public abstract <T> T find(@Nonnull Class<T> clazz, @Nonnull Serializable id);

	/**
	 * Persists the given transient instance.
	 *
	 * @param obj a transient instance of a persistent class
	 * @return the generated identifier
	 */
	@Nonnull
	public abstract <K> K persist(@Nonnull Object obj);

	/**
	 * Obtains the current Hibernate {@link Session}.
	 *
	 * @return the session to use
	 */
	@Nonnull
	public abstract Session getSession();
}
