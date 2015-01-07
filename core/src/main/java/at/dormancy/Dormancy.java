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
package at.dormancy;

import at.dormancy.access.MetadataPropertyAccessor;
import at.dormancy.handler.*;
import at.dormancy.handler.callback.EntityCallback;
import at.dormancy.handler.registry.ObjectHandlerRegistry;
import at.dormancy.metadata.ObjectMetadata;
import at.dormancy.persistence.PersistenceUnitProvider;
import at.dormancy.util.AbstractDormancyUtils;
import at.dormancy.util.ClassLookup;
import at.dormancy.util.DormancyContext;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.CollectionFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Disconnects JPA entities and applies changes to their persistence counterparts.
 *
 * @param <PU>  the type of the persistence unit to use
 * @param <PC>  the type of the persistence context to use
 * @param <PMD> the type of the persistence metadata to use
 * @author Gregor Schauer
 */
public class Dormancy<PU, PC, PMD> implements ApplicationContextAware {
	private static final Logger logger = Logger.getLogger(Dormancy.class);
	PersistenceUnitProvider<PU, PC, PMD> persistenceUnitProvider;
	AbstractDormancyUtils<PU, PC, PMD, PersistenceUnitProvider<PU, PC, PMD>> utils;

	DormancyObjectHandler dormancyObjectHandler = new DormancyObjectHandler();
	DormancyConfiguration config = new DormancyConfiguration();
	ObjectHandlerRegistry registry = new ObjectHandlerRegistry(this);

	@Inject
	public Dormancy(@Nonnull PersistenceUnitProvider<PU, PC, PMD> persistenceUnitProvider) {
		this.persistenceUnitProvider = persistenceUnitProvider;
	}

	/**
	 * Initializes this instance.<br/>
	 * If no {@link DormancyConfiguration} is set, a default configuration is created.
	 */
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void initialize() {
		// Initialize JPA provider specific DormancyUtils
		if (utils == null) {
			String className = "at.dormancy.util.DormancyUtils";
			Class<? extends AbstractDormancyUtils<PU, PC, PMD, PersistenceUnitProvider<PU, PC, PMD>>> type =
					ClassLookup.find(className).orThrow("Cannot initialize Dormancy: Missing class \"%s\"\n"
							+ "Please make sure that there is exactly one Dormancy backend in the classpath.\n"
							+ "Official implementations are:\n"
							+ "x) eclipselink\n"
							+ "x) hibernate3\n"
							+ "x) hibernate4\n"
							+ "x) hibernate-jpa", className).get();

			Constructor<? extends AbstractDormancyUtils<PU, PC, PMD, PersistenceUnitProvider<PU, PC, PMD>>> ctor =
					ConstructorUtils.getAccessibleConstructor(type, persistenceUnitProvider.getClass());
			utils = BeanUtils.instantiateClass(ctor, persistenceUnitProvider);
		}

		// Register all default object handlers if necessary
		registry.addObjectHandler(ArrayHandler.class);
		registry.addObjectHandler(BasicTypeHandler.class);
		registry.addObjectHandler(CollectionHandler.class);
		registry.addObjectHandler(MapHandler.class);
		registry.addObjectHandler(NullObjectHandler.class);
	}

	@Nullable
	public <R, O extends R> R disconnect(O dbObj) {
		return disconnect(dbObj, new DormancyContext());
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <R, O extends R> R disconnect(@Nullable O dbObj, @Nonnull DormancyContext ctx) {
		Map<Object, Object> adjacencyMap = ctx.getAdjacencyMap();
		if (dbObj == null) {
			return null;
		} else if (adjacencyMap.containsKey(dbObj)) {
			return (R) adjacencyMap.get(dbObj);
		}

		Class<R> dbType = utils.getClass(dbObj);
		ObjectHandler<R> handler = registry.getObjectHandler(dbType);
		if (handler != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Invoking ObjectHandler %s for object of type %s",
						handler.getClass().getSimpleName(), dbType.getName()));
			}
			return handler.disconnect(dbObj, ctx);
		}


		R trObj = config.isCloneObjects() || utils.isProxy(dbObj.getClass()) ? (R) createNewObject(dbObj) : dbObj;
		Class<Object> trType = utils.getClass(trObj);
		adjacencyMap.put(dbObj, trObj);

		if (config.isFlushAutomatically()) {
			if (logger.isTraceEnabled()) {
				logger.trace("Flushing persistence context");
			}
			utils.flush();
		}

		ObjectMetadata trMetadata = getObjectMetadata(ctx, trType);
		ObjectMetadata dbMetadata = trType == dbType ? trMetadata : getObjectMetadata(ctx, dbType);

		PropertyAccessor dbAccessor = new MetadataPropertyAccessor(dbObj, dbMetadata);
		PropertyAccessor trAccessor = new MetadataPropertyAccessor(trObj, trMetadata);

		for (String propertyName : dbMetadata.getProperties()) {
			Object dbValue = dbAccessor.getPropertyValue(propertyName);

			Object trValue = null;
			if (dbValue != null) {
				if (utils.isInitialized(dbValue)) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("Disconnecting property %s of type %s",
								propertyName, dbType.getName()));
					}
					trValue = disconnect(dbValue, ctx);
				} else if (utils.isPersistentCollection(dbValue) && config.isCreateEmptyCollections()) {
					trValue = dbValue instanceof Map
							? CollectionFactory.createApproximateMap(dbValue, 0)
							: CollectionFactory.createApproximateCollection(dbValue, 0);
					if (logger.isTraceEnabled()) {
						logger.debug(String.format("Uninitialized collection '%s' of %s will be replaced by %s",
								propertyName, dbType.getName(), trValue));
					}
				}
			}

			trAccessor.setPropertyValue(propertyName, trValue);
		}

		return trObj;
	}

	@Nonnull
	protected ObjectMetadata getObjectMetadata(@Nonnull DormancyContext ctx, @Nonnull Class<?> type) {
		ObjectMetadata metadata = ctx.getObjectMetadata(type);
		if (metadata == null) {
			metadata = utils.getObjectMetadata(type);
		} else if (logger.isDebugEnabled()) {
			logger.debug("Using custom ObjectMetadata for type " + type);
		}
		return metadata;
	}

	@Nullable
	public <O, R extends O> R apply(O trObj) {
		return apply(trObj, new DormancyContext());
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <O> O apply(@Nullable O trObj, O dbObj) {
		if (trObj == null) {
			return null;
		}

		Class<O> trType = utils.getClass(trObj);
		ObjectHandler<O> handler = registry.getObjectHandler(trType);
		if (handler != null) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Invoking ObjectHandler %s for applying %s",
						handler.getClass().getSimpleName(), trObj));
			} else if (logger.isDebugEnabled()) {
				logger.debug(String.format("Invoking ObjectHandler %s for applying changes on type %s",
						handler.getClass().getSimpleName(), trType.getName()));
			}
			return handler.apply(trObj, dbObj, new DormancyContext());
		}

		return apply(trObj, dbObj, new DormancyContext());
	}

	/**
	 * Invokes the given {@link EntityCallback} and passes its result to {@link #apply(Object, Object)}.
	 *
	 * @param trObj    the object to apply
	 * @param callback the callback to execute
	 * @param <T>      the type of the object
	 * @return the merged object
	 * @see #apply(Object, Object)
	 */
	@Nullable
	public final <T> T apply(@Nullable T trObj, @Nonnull EntityCallback<T, PU, PC, PMD> callback) {
		T dbObj = trObj == null ? null : callback.work(persistenceUnitProvider);
		return apply(trObj, dbObj);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <O, R extends O> R apply(@Nullable O trObj, @Nonnull DormancyContext ctx) {
		Map<Object, Object> adjacencyMap = ctx.getAdjacencyMap();
		if (trObj == null) {
			return null;
		} else if (adjacencyMap.containsKey(trObj)) {
			return (R) adjacencyMap.get(trObj);
		}

		Class<O> trType = utils.getClass(trObj);
		ObjectHandler<O> handler = registry.getObjectHandler(trType);
		if (handler != null) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Invoking ObjectHandler %s for applying changes of %s",
						handler.getClass().getSimpleName(), trObj));
			} else if (logger.isDebugEnabled()) {
				logger.debug(String.format("Invoking ObjectHandler %s for applying changes on type %s",
						handler.getClass().getSimpleName(), trType.getName()));
			}
			return handler.apply(trObj, null, ctx);
		}


		PMD metadata = utils.getMetadata(utils.getClass(trObj));
		if (metadata == null) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Skipping object %s because no metadata are available", trObj));
			}
			adjacencyMap.put(trObj, trObj);
			return (R) trObj;
		}


		// Retrieve the identifier of the persistent object
		Serializable identifier = utils.getIdentifier(metadata, trObj);
		if (identifier == null) {
			throw utils.exceptions().throwUnsavedTransientInstanceException(trObj);
		}

		// Retrieve the persistent object from the database
		Class<R> clazz = (Class) utils.getClass(trObj.getClass());
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Attempting to find entity %s with identifier %s",
					clazz.getName(), identifier));
		}
		R dbObj = utils.find(clazz, identifier);
		if (dbObj == null) {
			// Throw an exception indicating that the persistent object cannot be retrieved.
			throw utils.exceptions().throwEntityNotFoundException(identifier, trObj);
		}

		return apply(trObj, dbObj, ctx);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <O, R extends O> R apply(@Nullable O trObj, @Nullable R dbObj, @Nonnull DormancyContext ctx) {
		Map<Object, Object> adjacencyMap = ctx.getAdjacencyMap();
		if (trObj == null || dbObj == null) {
			return dbObj;
		} else if (adjacencyMap.containsKey(trObj)) {
			return (R) adjacencyMap.get(trObj);
		}

		adjacencyMap.put(trObj, dbObj);

		Class<O> trType = utils.getClass(trObj);
		ObjectHandler<O> handler = registry.getObjectHandler(trType);
		if (handler != null) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Invoking ObjectHandler %s for applying %s",
						handler.getClass().getSimpleName(), trObj));
			} else if (logger.isDebugEnabled()) {
				logger.debug(String.format("Invoking ObjectHandler %s for applying changes on type %s",
						handler.getClass().getSimpleName(), trType.getName()));
			}
			return handler.apply(trObj, dbObj, ctx);
		}


		ObjectMetadata objectMetadata = getObjectMetadata(ctx, utils.getClass(dbObj));

		MetadataPropertyAccessor dbAccessor = new MetadataPropertyAccessor(dbObj, objectMetadata);
		MetadataPropertyAccessor trAccessor = new MetadataPropertyAccessor(trObj, objectMetadata);

		PMD metadata = utils.getMetadata(dbObj);
		if (metadata == null) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Skipping object %s because no metadata are available", trObj));
			}
			return (R) trObj;
		} else if (trObj == dbObj) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Skipping object %s because transient and persistent object are the same",
						trObj));
			}
			return dbObj;
		}

		Serializable identifier = utils.getIdentifier(metadata, dbObj);
		if (identifier == null) {
			// Throw an exception indicating that the entity should have be saved before
			throw utils.exceptions().throwNullIdentifierException(trObj);
		}

		if (!utils.isInitialized(dbObj)) {
			throw utils.exceptions().throwLazyInitializationException(dbObj);
		}

		String versionPropertyName = utils.getVersionPropertyName(metadata);
		if (config.isCheckVersion() && utils.isVersioned(metadata)) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Checking version property '%s' of %s", versionPropertyName, trObj));
			}
			Object dbValue = dbAccessor.getPropertyValue(versionPropertyName);
			Object trValue = trAccessor.getPropertyValue(versionPropertyName);
			if (dbValue != null && !dbValue.equals(trValue)) {
				throw utils.exceptions().throwOptimisticLockException(dbValue, identifier);
			}
		}

		for (String propertyName : objectMetadata.getProperties()) {
			// Do not apply the version property if version checking is enabled
			if (propertyName.equals(versionPropertyName) && config.isCheckVersion()) {
				continue;
			}

			Object trValue = trAccessor.getPropertyValue(propertyName);
			Object dbValue = dbAccessor.getPropertyValue(propertyName);

			if (trValue != null && trValue != dbValue) {
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("Processing property %s of %s - applying %s",
							propertyName, trObj, trValue));
				} else if (logger.isDebugEnabled()) {
					logger.debug(String.format("Processing property %s of type %s", propertyName, trType.getName()));
				}
				trValue = apply(trValue, dbValue, ctx);

				if (trValue != dbValue) {
					dbAccessor.setPropertyValue(propertyName, trValue);
				}
			}
		}

		return dbObj;
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	protected <R, O extends R> R createNewObject(@Nonnull O obj) {
		return BeanUtils.instantiateClass((Class<R>) utils.getClass(obj));
	}

	@SuppressWarnings("unchecked")
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
		Map<String, AbstractDormancyUtils> utilsMap = applicationContext.getBeansOfType(AbstractDormancyUtils.class);
		if (utilsMap.size() > 1) {
			throw new IllegalStateException(String.format("Cannot initialize %s: Multiple beans of type %s found: %s",
					getClass().getName(), AbstractDormancyUtils.class.getName(), utilsMap.keySet()));
		} else if (utilsMap.size() == 1) {
			this.utils = utilsMap.get(utilsMap.keySet().iterator().next());
		}

		applicationContext.getAutowireCapableBeanFactory().initializeBean(registry, "objectHandlerRegistry");
		initialize();
	}

	@Nonnull
	public DormancyObjectHandler asObjectHandler() {
		return dormancyObjectHandler;
	}

	@Nonnull
	public ObjectHandlerRegistry getRegistry() {
		return registry;
	}

	@Nonnull
	public DormancyConfiguration getConfig() {
		return config;
	}

	public void setConfig(@Nonnull DormancyConfiguration config) {
		this.config = config;
	}

	@Nonnull
	public AbstractDormancyUtils<PU, PC, PMD, PersistenceUnitProvider<PU, PC, PMD>> getUtils() {
		return utils;
	}

	public class DormancyObjectHandler implements ObjectHandler<Object> {
		@Nonnull
		@Override
		@SuppressWarnings("unchecked")
		public <R> R createObject(@Nonnull R obj) {
			return BeanUtils.instantiate((Class<R>) utils.getClass(obj));
		}

		@Nullable
		@Override
		public <R, O extends R> R disconnect(@Nullable O dbObj, @Nonnull DormancyContext ctx) {
			return Dormancy.this.disconnect(dbObj, ctx);
		}

		@Nullable
		public <O, R extends O> R apply(@Nullable O trObj, @Nonnull DormancyContext ctx) {
			return Dormancy.this.apply(trObj, ctx);
		}

		@Nullable
		@Override
		public <O, R extends O> R apply(@Nullable O trObj, @Nullable R dbObj, @Nonnull DormancyContext ctx) {
			return dbObj == null
					? Dormancy.this.<O, R>apply(trObj, ctx)
					: Dormancy.this.apply(trObj, dbObj, ctx);
		}
	}
}
