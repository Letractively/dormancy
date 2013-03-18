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
package at.schauer.gregor.dormancy;

import at.schauer.gregor.dormancy.persister.*;
import at.schauer.gregor.dormancy.util.AbstractDormancyUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.CollectionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Clones Hibernate entities and merges them into a {@link Session}.<br/>
 *
 * @author Gregor Schauer
 * @see EntityPersister
 */
public class Dormancy extends AbstractEntityPersister<Object> implements ApplicationContextAware {
	protected Map<Class<?>, AbstractEntityPersister<?>> persisterMap;
	protected SessionFactory sessionFactory;
	protected EntityPersisterConfiguration config;
	protected AbstractDormancyUtils utils;
	protected Logger logger = Logger.getLogger(Dormancy.class);
	protected boolean registerDefaultEntityPersisters = true;

	/**
	 * Initializes this instance.<br/>
	 * If no {@link EntityPersisterConfiguration} is set, a default configuration is created.
	 */
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void initialize() {
		try {
			Class<?> type = getClass().getClassLoader().loadClass("at.schauer.gregor.dormancy.util.DormancyUtils");
			utils = (AbstractDormancyUtils) ConstructorUtils.invokeConstructor(type, sessionFactory);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (config == null) {
			config = new EntityPersisterConfiguration();
		}
		if (registerDefaultEntityPersisters) {
			addEntityPersister(ArrayPersister.class);
			addEntityPersister(CollectionPersister.class);
			addEntityPersister(MapPersister.class);
			addEntityPersister(NoOpPersister.class);
		}
	}

	@Nullable
	@Override
	public final <T> T clone(@Nullable T dbObj) {
		return clone_(dbObj, createAdjacencyMap());
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		// Check if the object has already been processed
		if (tree.containsKey(dbObj) || dbObj == null) {
			return (T) tree.get(dbObj);
		}

		// Use an EntityPersister if possible
		AbstractEntityPersister<T> entityPersister = getEntityPersister((Class<T>) dbObj.getClass());
		if (entityPersister != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Using %s for %s", entityPersister.getClass().getSimpleName(), dbObj));
				if (logger.isTraceEnabled()) {
					try {
						logger.trace(org.apache.commons.beanutils.BeanUtils.describe(dbObj));
					} catch (Exception e) {
						// ignore
					}
				}
			}
			return entityPersister.clone_(dbObj, tree);
		}

		// Create a new instance of the same type
		T trObj = config.getCloneObjects() || utils.isJavassistProxy(dbObj.getClass()) ? (T) BeanUtils.instantiateClass(utils.getClass(dbObj)) : dbObj;

		// Add the object to the adjacency list
		tree.put(dbObj, trObj);

		// If automatic flushing is enabled, flush the session to make sure that there are no pending changes
		if (config.getFlushAutomatically()) {
			utils.getSession().flush();
		}

		// Retrieve the Hibernate class metadata (if available)
		ClassMetadata metadata = utils.getClassMetadata(dbObj);

		// Process the properties
		String[] propertyNames = utils.getPropertyNames(dbObj);
		PropertyAccessor dbPropertyAccessor = utils.getPropertyAccessor(metadata, dbObj);
		PropertyAccessor trPropertyAccessor = dbObj == trObj ? dbPropertyAccessor : utils.getPropertyAccessor(metadata, trObj);
		for (String propertyName : propertyNames) {
			Object dbValue;
			try {
				dbValue = dbPropertyAccessor.getPropertyValue(propertyName);
			} catch (BeansException e) {
				if (metadata != null) {
					/**
					 * If the property value cannot bet read and the object is a Hibernate entity, throw an exception.
					 * Note that this is a security mechanism to ensure database consistency.
					 * Otherwise it would be possible that references, which are not initialized properly,
					 * cause constraint violations or even delete associations.
					 */
					throw e;
				} else if (logger.isDebugEnabled()) {
					// If the property value of a non entity cannot be read, write a debug message to the log.
					logger.debug(ExceptionUtils.getMessage(e));
				}
				continue;
			}

			Object trValue = null;
			// If the property (e.g., a lazy persistent collection) is initialized traverse the object graph recursively
			if (dbValue != null) {
				if (Hibernate.isInitialized(dbValue)) {
					trValue = clone_((T) dbValue, tree);
				} else if (utils.isPersistentCollection(dbValue) && config.getCreateEmptyCollections()) {
					trValue = dbValue instanceof Map
							? CollectionFactory.createApproximateMap(dbValue, 0)
							: CollectionFactory.createApproximateCollection(dbValue, 0);
				}
			}

			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Setting property %s of %s to %s", propertyName, trObj, trValue));
			}

			// Attempt to set the property value
			try {
				trPropertyAccessor.setPropertyValue(propertyName, trValue);
			} catch (BeansException e) {
				if (metadata != null) {
					/**
					 * If the property value cannot bet set and the object is a Hibernate entity, throw an exception.
					 * Note that this is a security mechanism to ensure database consistency.
					 * Otherwise it would be possible that references, which are not initialized properly,
					 * cause constraint violations or even delete associations.
					 */
					throw e;
				} else if (logger.isEnabledFor(Level.WARN)) {
					// If the property value of a non entity cannot be set, write a warning to the log.
					logger.warn(ExceptionUtils.getMessage(e));
				}
			}
		}

		return trObj;
	}

	@Nullable
	@Override
	public final <T> T merge(@Nullable T trObj) {
		return merge_(trObj, createAdjacencyMap());
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		// Check if the object has already been processed
		if (tree.containsKey(trObj) || trObj == null) {
			return (T) tree.get(trObj);
		}

		// Use a EntityPersister if possible
		AbstractEntityPersister<T> entityPersister = getEntityPersister((Class<? extends T>) trObj.getClass());
		if (entityPersister != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Using %s for %s", entityPersister.getClass().getSimpleName(), trObj));
			}
			return entityPersister.merge_(trObj, tree);
		}

		// Verify that the given object is a non-null managed entity.
		ClassMetadata metadata = utils.getClassMetadata(trObj);
		if (metadata == null) {
			return trObj;
		}

		// Retrieve the identifier of the persistent object
		Serializable identifier = utils.getIdentifier(metadata, trObj);

		// If the identifier cannot be retrieved via getter, try to access it directly.
		if (identifier == null) {
			// If the object has no identifier, it is considered to be new
			if (config.getSaveNewEntities()) {
				// If desired, try to persist the object
				utils.persist(trObj);
				identifier = utils.getIdentifier(metadata, trObj);
			} else {
				// Otherwise throw an exception indicating that session.save() should be called
				throwNullIdentifierException(trObj);
			}
		}

		// Retrieve the persistent object from the database
		T dbObj = (T) utils.find(utils.getClass(trObj), identifier);
		if (dbObj == null) {
			// Throw an exception indicating that the persistent object cannot be retrieved.
			throw new ObjectNotFoundException(identifier, utils.getClass(trObj).getSimpleName());
		}

		return merge_(trObj, dbObj, tree);
	}

	/**
	 * Invokes the given {@link HibernateCallback} and passes its result to {@link #merge(Object, Object)}.
	 *
	 * @param trObj    the object to merge
	 * @param callback the callback to execute
	 * @return the merged object
	 * @throws SQLException if thrown by Hibernate-exposed JDBC API
	 */
	@Nullable
	public <T> T merge(@Nullable T trObj, @Nonnull HibernateCallback<T> callback) throws SQLException {
		return merge(trObj, trObj != null ? callback.doInHibernate(utils.getSession()) : null);
	}

	@Nullable
	@Override
	public final <T> T merge(@Nullable T trObj, @Nullable T dbObj) {
		return merge_(trObj, dbObj, createAdjacencyMap());
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		// Check if the object has already been processed
		if (tree.containsKey(trObj) || dbObj == null) {
			return (T) tree.get(trObj);
		}

		// Use an EntityPersister if possible
		AbstractEntityPersister<T> entityPersister = getEntityPersister(trObj != null ? (Class<? extends T>) trObj.getClass() : null);
		if (entityPersister != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Using %s for %s", entityPersister.getClass().getSimpleName(), trObj));
			}
			return entityPersister.merge_(trObj, dbObj, tree);
		}

		// Add the object to the adjacency list
		tree.put(trObj, dbObj);

		// Verify that the given object is a non-null managed entity or it is not necessary to merge it
		ClassMetadata metadata = utils.getClassMetadata(trObj);
		if (metadata == null || trObj == dbObj) {
			return trObj;
		}

		// Retrieve the identifier of the persistent object
		Serializable identifier = utils.getIdentifierValue(metadata, dbObj);

		// Compare the version property (if present and enabled)
		PropertyAccessor dbPropertyAccessor = utils.getPropertyAccessor(metadata, dbObj);
		PropertyAccessor trPropertyAccessor = utils.getPropertyAccessor(metadata, trObj);
		String[] propertyNames = utils.getPropertyNames(trObj);
		if (config.getCheckVersion() && utils.isVersioned(metadata)) {
			Object dbValue = dbPropertyAccessor.getPropertyValue(utils.getVersionPropertyName(metadata));
			Object trValue = trPropertyAccessor.getPropertyValue(utils.getVersionPropertyName(metadata));
			if (dbValue != null && !dbValue.equals(trValue)) {
				throw new StaleObjectStateException(utils.getEntityName(utils.getClass(dbValue)), identifier);
			}
		}

		// Process the properties
		for (int i = 0; i < propertyNames.length; i++) {
			// Do not apply the version property if version checking is enabled
			String versionPropertyName = utils.getVersionPropertyName(metadata);
			if (propertyNames[i].equals(versionPropertyName) && config.getCheckVersion()) {
				continue;
			}

			// Read the property values
			String propertyName = propertyNames[i];
			Object trValue = trPropertyAccessor.getPropertyValue(propertyName);
			Object dbValue = dbPropertyAccessor.getPropertyValue(propertyName);
			Class<?> type = utils.getPropertyType(utils.getClass(dbObj), propertyName);

			// Lazily loaded collections are not copied
			if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
				if (!utils.isInitializedPersistentCollection(dbValue)) {
					// If property is loaded lazily, the value of the given object must be null or empty
					if (trValue != null && trValue != dbValue && CollectionUtils.size(trValue) > 0) {
						throw new PropertyValueException("Property is loaded lazily. Therefore it must be null but was: " + trValue, utils.getEntityName(utils.getClass(dbObj)), propertyName);
					}
					continue;
				}
				trValue = merge_(trValue, dbValue, tree);
			}

			// Lazily loaded properties are not copied
			if (!ClassUtils.isPrimitiveOrWrapper(type) && !type.getName().startsWith("java.") && !type.isArray()) {
				// If the persistent value is a Hibernate proxy, it might be loaded lazily
				if (dbValue instanceof HibernateProxy) {
					HibernateProxy hibernateProxy = HibernateProxy.class.cast(dbValue);
					LazyInitializer lazyInitializer = hibernateProxy.getHibernateLazyInitializer();
					if (lazyInitializer.isUninitialized()) {
						// If property is loaded lazily, the value of the given object must be null
						if (trValue != null) {
							throw new PropertyValueException("Property is loaded lazily. Therefore it must be null but was: " + trValue, utils.getEntityName(utils.getClass(dbObj)), propertyName);
						}
						continue;
					}
				} else if (trValue != dbValue) {
					// Get the identifier of the associated transient object
					ClassMetadata valueMetadata = utils.getClassMetadata(dbValue);
					Serializable trValueId = Serializable.class.cast(utils.getIdentifier(valueMetadata, trValue));
					// Get the identifier of the associated persistent object
					Serializable dbValueId = Serializable.class.cast(utils.getIdentifier(valueMetadata, dbValue));

					// If the transient object is new
					if (trValueId == null) {
						if (config.getSaveNewEntities()) {
							// If desired, try to persist the object
							trValueId = utils.persist(trValue);
							trValue = utils.find(trValue.getClass(), trValueId);
						} else {
							// Otherwise throw an exception indicating that session.save() should be called
							throw new TransientObjectException("object references an unsaved transient instance - save the transient instance before flushing: " + utils.getClass(trValue).getName());
						}
					} else if (!trValueId.equals(dbValueId)) {
						// Load the entity with the given identifier
						trValue = utils.find(dbValue.getClass(), trValueId);
					} else {
						// Use the persistent value because the object identities are equals
						trValue = dbValue;
					}
				}
			}

			if (trValue != dbValue) {
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("Setting property %s of %s to %s", propertyName, dbObj, trValue));
				}
				dbPropertyAccessor.setPropertyValue(propertyName, trValue);
			}
		}

		return dbObj;
	}

	/**
	 * Sets the Hibernate SessionFactory that should be used to create Hibernate Sessions.
	 *
	 * @param sessionFactory the SessionFactory to use
	 */
	@Inject
	@Named("sessionFactory")
	public void setSessionFactory(@Nonnull SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Returns the EntityPersisterConfiguration that should be used.
	 *
	 * @return the EntityPersisterConfiguration to use
	 */
	@Nonnull
	public EntityPersisterConfiguration getConfig() {
		return config;
	}

	/**
	 * Sets the EntityPersisterConfiguration that should be used.
	 *
	 * @param config the EntityPersisterConfiguration to use
	 */
	public void setConfig(@Nonnull EntityPersisterConfiguration config) {
		this.config = config;
	}

	/**
	 * Sets a flag indicating that the default {@link EntityPersister}s should be initialized upon initialization.
	 *
	 * @param registerDefaultEntityPersisters
	 *         {@code true} if the default EntityPersisters should be registered, {@code false} otherwise
	 */
	public void setRegisterDefaultEntityPersisters(boolean registerDefaultEntityPersisters) {
		this.registerDefaultEntityPersisters = registerDefaultEntityPersisters;
	}

	/**
	 * Returns a modifiable map containing all registered EntityPersisters.
	 *
	 * @return the registered EntityPersisters
	 */
	@Nonnull
	public Map<Class<?>, AbstractEntityPersister<?>> getPersisterMap() {
		if (persisterMap == null) {
			persisterMap = new LinkedHashMap<Class<?>, AbstractEntityPersister<?>>();
		}
		return persisterMap;
	}

	/**
	 * Sets the mapping of the AbstractEntityPersisters that should be used.
	 *
	 * @param persisterMap the entity persister mapping
	 */
	public void setPersisterMap(@Nonnull Map<Class<?>, AbstractEntityPersister<?>> persisterMap) {
		this.persisterMap = persisterMap;
	}

	/**
	 * Returns an {@link EntityPersister} that is capable of processing instances
	 * of the given type.
	 *
	 * @param clazz the type of the object to process
	 * @return the EntityPersister or {@code null} if there is none available.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> AbstractEntityPersister<T> getEntityPersister(@Nullable Class<? extends T> clazz) {
		AbstractEntityPersister<T> entityPersister = (AbstractEntityPersister<T>) getPersisterMap().get(clazz);
		if (entityPersister == null && clazz != null && !getPersisterMap().containsKey(clazz)) {
			entityPersister = findEntityPersister(clazz);

			if (entityPersister == null) {
				for (Map.Entry<Class<?>, AbstractEntityPersister<?>> entry : persisterMap.entrySet()) {
					if (entry.getValue() instanceof DynamicEntityPersister) {
						if (((DynamicEntityPersister<?>) entry.getValue()).supports(clazz)) {
							entityPersister = (AbstractEntityPersister<T>) entry.getValue();
							break;
						}
					}
				}
			}
			if (logger.isDebugEnabled()) {
				logger.trace(String.format("Registering %s for type %s", entityPersister, clazz.getName()));
			}
			getPersisterMap().put(clazz, entityPersister);
		}
		return entityPersister;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <T> AbstractEntityPersister<T> findEntityPersister(@Nonnull Class<? extends T> clazz) {
		try {
			for (Map.Entry<Class<?>, AbstractEntityPersister<?>> entry : getPersisterMap().entrySet()) {
				if (entry.getKey().isAssignableFrom(clazz)) {
					return (AbstractEntityPersister<T>) entry.getValue();
				}
			}
		} catch (ConcurrentModificationException e) {
			/*
			 * The persister map is not synchronized because of the performance requirements.
			 * Thus, a ConcurrentModificationException may rarely happen while iterating through it.
			 * Therefore, the exception is ignored and Dormancy attempts to retry finding an appropriate EntityPersister.
			 */
			return findEntityPersister(clazz);
		}
		return null;
	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
		initialize();
		// Retrieve all AbstractEntityPersisters from the application context and register them
		Map<String, AbstractEntityPersister> map = applicationContext.getBeansOfType(AbstractEntityPersister.class);
		for (AbstractEntityPersister<?> entityPersister : map.values()) {
			addEntityPersister(entityPersister);
		}
	}

	/**
	 * Registers the given {@link AbstractEntityPersister} for certain types.<br/>
	 * The {@link AbstractEntityPersister} is registered for every type returned by
	 * {@link at.schauer.gregor.dormancy.persister.AbstractEntityPersister#getSupportedTypes()} and the parameter types.
	 * Furthermore, the type of the {@link AbstractEntityPersister} itself is registered so it can be used by in
	 * {@link at.schauer.gregor.dormancy.interceptor.PersistenceEndpoint#types()}.
	 *
	 * @param entityPersister the EntityPersister to register
	 * @param types           the types of objects supported by the EntityPersister (may be {@code null})
	 * @see #addEntityPersister(Class, Class[])
	 */
	public void addEntityPersister(@Nonnull AbstractEntityPersister<?> entityPersister, @Nullable Class<?>... types) {
		if (ArrayUtils.isNotEmpty(entityPersister.getSupportedTypes())) {
			for (Class<?> type : entityPersister.getSupportedTypes()) {
				getPersisterMap().put(type, entityPersister);
			}
		}
		if (ArrayUtils.isNotEmpty(types)) {
			// Register the given types for advanced customization
			for (Class<?> type : types) {
				getPersisterMap().put(type, entityPersister);
			}
		}
		// Register the unproxified persister itself to make it available for PersistenceEndpoint
		getPersisterMap().put(AopUtils.getTargetClass(entityPersister), entityPersister);
	}

	/**
	 * Registers an instance of the given {@link AbstractEntityPersister} type for certain types.<br/>
	 * The {@link AbstractEntityPersister} is registered for every type returned by
	 * {@link at.schauer.gregor.dormancy.persister.AbstractEntityPersister#getSupportedTypes()} and the parameter types.
	 * Furthermore, the type of the {@link AbstractEntityPersister} itself is registered so it can be used by in
	 * {@link at.schauer.gregor.dormancy.interceptor.PersistenceEndpoint#types()}.
	 *
	 * @param entityPersisterClass the type of the EntityPersister to register
	 * @param types                the types of objects supported by the EntityPersister (may be {@code null})
	 * @see #addEntityPersister(at.schauer.gregor.dormancy.persister.AbstractEntityPersister, Class[])
	 */
	public void addEntityPersister(@Nonnull Class<? extends AbstractEntityPersister> entityPersisterClass, @Nullable Class<?>... types) {
		Constructor<? extends AbstractEntityPersister> constructor = ClassUtils.getConstructorIfAvailable(entityPersisterClass, Dormancy.class);
		AbstractEntityPersister<?> entityPersister = constructor != null ? BeanUtils.instantiateClass(constructor, this) : BeanUtils.instantiateClass(entityPersisterClass);
		if (entityPersister instanceof AbstractContainerPersister) {
			AbstractContainerPersister.class.cast(entityPersister).setSessionFactory(sessionFactory);
		}
		addEntityPersister(entityPersister, types);
	}

	/**
	 * Throws a {@link TransientObjectException} indicating that the object has no valid identifier.
	 *
	 * @param object the object
	 */
	protected void throwNullIdentifierException(@Nonnull Object object) {
		throw new TransientObjectException("The given object has a null identifier: " + utils.getEntityName(utils.getClass(object)));
	}

	/**
	 * Throws {@link TransientObjectException} indicating that the object must be saved manually before continuing.
	 *
	 * @param object the object
	 */
	protected void throwUnsavedTransientInstanceException(@Nonnull Object object) {
		throw new TransientObjectException(
				"object references an unsaved transient instance - save the transient instance before flushing: " +
						utils.getEntityName(utils.getClass(object))
		);
	}

	/**
	 * Returns the Dormancy utilities associated with this instance.
	 *
	 * @return the Dormancy utilities to use
	 */
	@Nonnull
	public AbstractDormancyUtils getUtils() {
		return utils;
	}
}
