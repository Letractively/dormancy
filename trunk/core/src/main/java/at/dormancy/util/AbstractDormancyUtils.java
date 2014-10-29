/*
 * Copyright 2014 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.util;

import at.dormancy.metadata.ObjectMetadata;
import at.dormancy.persistence.PersistenceUnitProvider;
import com.google.common.collect.Maps;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Common utility methods for Dormancy support code.
 *
 * @param <PU>  the type of the persistence unit to use
 * @param <PC>  the type of the persistence context to use
 * @param <PMD> the type of the persistence metadata to use
 * @param <PUP> the type of the {@code PersistenceUnitProvider} to use
 * @author Gregor Schauer
 */
public abstract class AbstractDormancyUtils<PU, PC, PMD, PUP extends PersistenceUnitProvider<PU, PC, PMD>> {
	protected static final Class<? extends Annotation> ID_CLASS;

	protected final Map<Class<?>, ObjectMetadata> objectMetadataMap = Maps.newConcurrentMap();
	protected PUP persistenceUnitProvider;

	static {
		String javaxPersistenceId = "javax.persistence.Id";
		ID_CLASS = ClassLookup.find(javaxPersistenceId).orThrow("Cannot find class: %s", javaxPersistenceId).get();
	}

	@Inject
	protected AbstractDormancyUtils(@Nonnull PUP persistenceUnitProvider) {
		this.persistenceUnitProvider = persistenceUnitProvider;
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
	 * @param <T>        the type of the object
	 * @return the object found or {@code null} if the collection does not contain such an object
	 * @see #getIdentifierValue(Object, Object)
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T findPendant(@Nonnull T obj, @Nonnull Collection<?> collection) {
		Method method = ReflectionUtils.findMethod(getClass(obj), "equals", Object.class);
		if (method.getDeclaringClass() != Object.class) {
			// If the given object overrides the equals() method, invoke it for every object in the collection
			for (Object elem : collection) {
				if (obj.equals(elem) && collection.remove(elem)) {
					return (T) elem;
				}
			}
		} else {
			// Otherwise get the Hibernate metadata and a PropertyAccessor to get the identifier
			PMD objMetadata = getMetadata(obj);
			if (objMetadata == null) {
				return null;
			}
			Serializable objIdentifier = getIdentifier(objMetadata, obj);

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
	}

	/**
	 * Gets the persistence metadata associated with the given entity class.
	 *
	 * @param obj the object to retrieve ClassMetadata for
	 * @return the ClassMetadata or {@code null} if the type is not an Hibernate managed entity
	 * @see #getMetadata(Class)
	 * @see #getClass(Object)
	 */
	@Nullable
	public PMD getMetadata(@Nullable Object obj) {
		return obj != null ? getMetadata(getClass(obj)) : null;
	}

	/**
	 * Gets the persistence metadata associated with the given entity class.
	 *
	 * @param clazz the type to retrieve ClassMetadata for
	 * @return the ClassMetadata or {@code null} if the type is not an Hibernate managed entity
	 */
	@Nullable
	public abstract PMD getMetadata(@Nullable Class<?> clazz);

	/**
	 * Gets the unproxified type of the given object.
	 *
	 * @param proxy a persistable object or proxy
	 * @param <T>   the type of the object
	 * @return the true class of the instance
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> Class<T> getClass(@Nonnull Object proxy) {
		return getClass(proxy.getClass());
	}

	/**
	 * Gets the real type of the given class.
	 *
	 * @param clazz the type
	 * @param <T>   the type of the object
	 * @return the true class of the type
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> Class<T> getClass(@Nonnull Class<?> clazz) {
		while (isProxy(clazz)) {
			clazz = clazz.getSuperclass();
		}
		return (Class<T>) clazz;
	}

	/**
	 * Returns the entity name of the given class.<br/>
	 * The entity name is the unique name of the entity in the meta-model or the simple class name if the type is not
	 * known by the meta-model.
	 *
	 * @param clazz the type
	 * @return the entity name of the class
	 */
	@Nonnull
	protected abstract String getEntityName(@Nonnull Class<?> clazz);

	/**
	 * Attempts to get the identifier of the given object by using the provided persistence metadata.
	 *
	 * @param metadata the ClassMetadata of the object (may be null)
	 * @param bean     the object
	 * @param <T>      the type of the object
	 * @return the identifier or {@code null} if the identifier cannot be retrieved or is {@code null}
	 * @see #getIdentifierValue(Object, Object)
	 */
	@Nullable
	public abstract <T> Serializable getIdentifier(@Nonnull PMD metadata, @Nonnull T bean);

	/**
	 * Retrieves the identifier of the given object by using the provided persistence metadata or
	 * {@link org.springframework.beans.PropertyAccessor}.<br/>
	 * If the identifier cannot be retrieved, an exception is thrown.
	 *
	 * @param metadata the ClassMetadata of the object (may be null)
	 * @param bean     the object
	 * @param <T>      the type of the object
	 * @return the identifier or {@code null} if the identifier cannot be retrieved or is {@code null}
	 * @see #getIdentifier(Object, Object)
	 */
	@Nonnull
	public <T> Serializable getIdentifierValue(@Nonnull PMD metadata, @Nonnull T bean) {
		Serializable identifier = getIdentifier(metadata, bean);
		if (identifier == null) {
			/*
			If the identifier of the database object is null, it is really null, which indicates a database problem,
			or it cannot be retrieved
			 */
			throw exceptions().throwPropertyValueException("Cannot read identifier", bean);
		}
		return identifier;
	}

	/**
	 * Returns whether the JPA entity associated with the given metadata is versioned.<br/>
	 *
	 * @param metadata the metadata
	 * @return {@code true} if the entity is versioned, {@code false} otherwise
	 */
	public abstract boolean isVersioned(@Nonnull PMD metadata);

	/**
	 * Returns the name of the property of the entity used for versioning (if available).
	 *
	 * @param metadata the metadata
	 * @return the name of the version property or {@code null} if the entity is not versioned
	 */
	@Nullable
	public abstract String getVersionPropertyName(@Nonnull PMD metadata);

	/**
	 * Returns the {@link ObjectMetadata} to use for accessing properties declared within the given type.<br/>
	 * If no metadata are defined for the type, a new one is created on-demand.
	 *
	 * @param clazz the type
	 * @return the metadata to use
	 * @see #createObjectMetadata(Class)
	 */
	@Nonnull
	public ObjectMetadata getObjectMetadata(@Nonnull Class<?> clazz) {
		clazz = getClass(clazz);
		ObjectMetadata metadata = objectMetadataMap.get(clazz);
		if (metadata == null) {
			metadata = createObjectMetadata(clazz);
			objectMetadataMap.put(clazz, metadata);
		}
		return metadata;
	}

	/**
	 * Sets the {@link ObjectMetadata} to use for accessing properties declared within the given type.<br/>
	 *
	 * @param clazz    the type
	 * @param metadata the metadata to use (can be {@code null})
	 */
	@SuppressWarnings("unused")
	public void setObjectMetadata(@Nonnull Class<?> clazz, @Nullable ObjectMetadata metadata) {
		objectMetadataMap.put(getClass(clazz), metadata);
	}

	/**
	 * Creates a new {@link ObjectMetadata} instance for the given type.
	 *
	 * @param clazz the type
	 * @return the metadata to use
	 */
	@Nonnull
	protected abstract ObjectMetadata createObjectMetadata(@Nonnull Class<?> clazz);

	/**
	 * Check if the proxy or persistent collection is initialized.<br/>
	 * If the objects is neither an entity nor a persistent collection e.g., a {@link String}, {@code true} is
	 * returned.
	 *
	 * @param obj the object to check
	 * @return {@code true} if the argument is already initialized, or is not a proxy or collection
	 */
	public abstract boolean isInitialized(@Nullable Object obj);

	/**
	 * Checks if the given class is a proxy of a persistent object.
	 *
	 * @param clazz the class to check
	 * @return {@code true} if the given class is a proxy.
	 */
	public boolean isProxy(@Nonnull Class<?> clazz) {
		return clazz.getSimpleName().contains("$$_javassist");
	}

	/**
	 * Checks if the given object is a persistent collection.
	 *
	 * @param obj the object to check
	 * @return {@code true} if the object is a persistent collection, {@code false} otherwise
	 * @see #isInitializedPersistentCollection(Object)
	 */
	public abstract boolean isPersistentCollection(@Nullable Object obj);

	/**
	 * Checks if the given object is an initialized persistent collection.
	 *
	 * @param obj the object to check
	 * @return {@code true} if the object is an initialized persistent collection, {@code false} otherwise
	 * @see #isPersistentCollection(Object)
	 */
	public abstract boolean isInitializedPersistentCollection(@Nullable Object obj);

	/**
	 * Return the persistent instance of the given entity class with the given identifier, or {@code null} if there is
	 * no such persistent instance.<br/>
	 * If the instance is already associated with the persistence context, return that instance.
	 * This method never returns an uninitialized instance.
	 *
	 * @param clazz the persistent class
	 * @param id    the identifier
	 * @param <T>   the type of the object
	 * @return the persistent instance
	 */
	@Nullable
	public abstract <T> T find(@Nonnull Class<T> clazz, @Nonnull Serializable id);

	/**
	 * Force the current persistence context to flush.
	 * <p/>
	 * <i>Flushing</i> is the process of synchronizing the underlying persistent store with persistable state held in
	 * memory.
	 */
	public abstract void flush();

	/**
	 * Persists the given transient instance.
	 *
	 * @param obj a transient instance of a persistent class
	 * @param <I> the type of the identifier
	 * @return the generated identifier
	 */
	@Nonnull
	public abstract <I> I persist(@Nonnull Object obj);

	/**
	 * Obtains the current persistence context.
	 *
	 * @return the persistence context to use
	 */
	@Nonnull
	public abstract PC getPersistenceContext();

	/**
	 * Returns a helper class for throwing JPA provider specific exceptions.
	 *
	 * @return the exception utilities
	 */
	@Nonnull
	public abstract AbstractExceptions exceptions();

	/**
	 * Provides utilities for throwing JPA provider specific exceptions.
	 *
	 * @author Gregor Schauer
	 */
	public abstract class AbstractExceptions {
		/**
		 * Throws an exception indicating that the object must be saved manually before continuing.
		 *
		 * @param object the object
		 * @return nothing
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		public abstract RuntimeException throwUnsavedTransientInstanceException(@Nonnull Object object);

		/**
		 * Throws an exception indicating that the object has no valid identifier.
		 *
		 * @param object the object
		 * @return nothing
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		public abstract RuntimeException throwNullIdentifierException(@Nonnull Object object);

		/**
		 * Throws an exception when an uninitialized object was accessed.
		 *
		 * @param object the persistent entity
		 * @return nothing
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		public abstract RuntimeException throwLazyInitializationException(@Nonnull Object object);

		/**
		 * Throws an exception when a lazy property holds a value while {@code null} was expected.
		 *
		 * @param trValue      the transient value (that should be {@code null})
		 * @param dbObj        the persistent entity
		 * @param propertyName the name of the property
		 * @return nothing
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		public abstract RuntimeException throwLazyPropertyNotNullException(@Nonnull Object trValue,
																		   @Nonnull Object dbObj,
																		   @Nonnull String propertyName);

		/**
		 * Throws an exception when an optimistic locking conflict occurs.
		 *
		 * @param dbValue    the persistent value causing the conflict
		 * @param identifier the identifier of the entity
		 * @return nothing
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		public abstract RuntimeException throwOptimisticLockException(@Nonnull Object dbValue,
																	  @Nonnull Serializable identifier);

		/**
		 * Throws an exception when an entity reference is accessed but the entity does not exist.
		 *
		 * @param identifier the identifier of the entity attempted to retrieve
		 * @param trObj      the transient object
		 * @return nothing
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		public abstract RuntimeException throwEntityNotFoundException(@Nonnull Serializable identifier,
																	  @Nonnull Object trObj);

		/**
		 * Throws an exception when a property is not accessible or holds an unexpected value.
		 *
		 * @param message the detail message
		 * @param bean    the bean caused the exception
		 * @return nothing
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		public abstract RuntimeException throwPropertyValueException(@Nonnull String message, @Nonnull Object bean);
	}
}
