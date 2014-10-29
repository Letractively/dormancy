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
package at.dormancy.handler.registry;

import at.dormancy.Dormancy;
import at.dormancy.handler.DynamicObjectHandler;
import at.dormancy.handler.ObjectHandler;
import at.dormancy.handler.StaticObjectHandler;
import at.dormancy.persistence.PersistenceUnitProvider;
import at.dormancy.persistence.PersistenceUnitProviderAware;
import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Provides registry capabilities for looking up {@link ObjectHandler} instances.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class ObjectHandlerRegistry {
	private static final Logger logger = Logger.getLogger(ObjectHandlerRegistry.class);
	protected Map<Class<?>, ObjectHandler<?>> handlerMap = new ConcurrentHashMap<Class<?>, ObjectHandler<?>>();
	protected Set<DynamicObjectHandler<?>> dynamicHandlers =
			new ConcurrentSkipListSet<DynamicObjectHandler<?>>(Ordering.allEqual());
	protected PersistenceUnitProvider<?, ?, ?> persistenceUnitProvider;
	protected Dormancy<?, ?, ?> dormancy;

	@Inject
	public ObjectHandlerRegistry(@Nonnull Dormancy<?, ?, ?> dormancy) {
		this.dormancy = dormancy;
	}

	/**
	 * Returns an {@link ObjectHandler} that is capable of processing instances of the given type.
	 *
	 * @param <T>   the type of the object to process
	 * @param clazz the type of the object to process
	 * @return the ObjectHandler or {@code null} if there is none available.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> ObjectHandler<T> getObjectHandler(@Nullable Class<? extends T> clazz) {
		ObjectHandler<T> handler = (ObjectHandler<T>) handlerMap.get(clazz);
		if (handler == null && clazz != null && !handlerMap.containsKey(clazz)) {
			handler = findObjectHandler(clazz);

			if (handler == null) {
				for (DynamicObjectHandler dynamicHandler : dynamicHandlers) {
					if (dynamicHandler.getPredicate().apply(clazz)) {
						handler = dynamicHandler;
						logger.info(String.format("Using DynamicObjectHandler %s for type '%s'",
								handler.getClass().getSimpleName(), clazz.getName()));
						break;
					}
				}
			}
			if (handler != null) {
				logger.info(String.format("Registering handler %s for type %s",
						handler.getClass().getSimpleName(), clazz.getName()));
				handlerMap.put(clazz, handler);
			}
		}
		if (handler == null && clazz != null) {
			for (ObjectHandler<?> h : handlerMap.values()) {
				if (h instanceof DynamicObjectHandler) {
					if (((DynamicObjectHandler) h).getPredicate().apply(clazz)) {
						handler = (ObjectHandler<T>) h;
						logger.info(String.format("Registering handler %s for type %s",
								handler.getClass().getSimpleName(), clazz.getName()));
						handlerMap.put(clazz, handler);
						break;
					}
				}
			}
		}

		return handler;
	}

	/**
	 * Finds the {@link ObjectHandler} to be used for the given type.
	 *
	 * @param clazz the type
	 * @param <T>   the type of the class
	 * @return the ObjectHandler found or {@code null} if none is appropriate
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	protected <T> ObjectHandler<T> findObjectHandler(@Nonnull Class<? extends T> clazz) {
		for (Map.Entry<Class<?>, ObjectHandler<?>> entry : handlerMap.entrySet()) {
			if (entry.getKey().isAssignableFrom(clazz)) {
				return (ObjectHandler<T>) entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Registers the given {@link ObjectHandler} for certain types.<br/>
	 * The {@link ObjectHandler} is registered for every type returned by
	 * {@link StaticObjectHandler#getSupportedTypes()} and the parameter types.
	 * Furthermore, the type of the {@link ObjectHandler} itself is registered so it can be used by in
	 * {@link at.dormancy.aop.PersistenceEndpoint#types()}.
	 *
	 * @param handler the ObjectHandler to register
	 * @param types   the types of objects supported by the ObjectHandler (may be {@code null})
	 * @see #addObjectHandler(Class, Class[])
	 */
	public void addObjectHandler(@Nonnull ObjectHandler<?> handler, @Nullable Class<?>... types) {
		if ((handler instanceof StaticObjectHandler<?>)) {
			Set<Class<?>> supportedTypes = ((StaticObjectHandler<?>) handler).getSupportedTypes();
			if (!isEmpty(supportedTypes)) {
				logger.info(String.format("Registering ObjectHandler %s for the following types: %s",
						handler.getClass().getSimpleName(), Joiner.on(", ").join(supportedTypes)));
				for (Class<?> type : supportedTypes) {
					handlerMap.put(type, handler);
				}
			}
		}
		if (ArrayUtils.isNotEmpty(types)) {
			// Register the given types for advanced customization
			logger.info(String.format("Registering ObjectHandler %s for the custom types: %s",
					handler.getClass().getSimpleName(), Joiner.on(", ").join(types)));
			for (Class<?> type : types) {
				handlerMap.put(type, handler);
			}
		}
		// Register the unproxified handler itself to make it available for PersistenceEndpoint
		if (handler instanceof DynamicObjectHandler) {
			logger.info(String.format("Registering dynamic ObjectHandler %s", handler.getClass().getSimpleName()));
			dynamicHandlers.add((DynamicObjectHandler) handler);
		}
	}

	/**
	 * Registers an instance of the given {@link ObjectHandler} type for certain types.<br/>
	 * The {@link ObjectHandler} is registered for every type returned by
	 * {@link StaticObjectHandler#getSupportedTypes()} and the parameter types.
	 *
	 * @param objectHandlerClass the type of the ObjectHandler to register
	 * @param types              the types of objects supported by the ObjectHandler (may be {@code null})
	 * @see #addObjectHandler(ObjectHandler, Class[])
	 */
	@SuppressWarnings("unchecked")
	public void addObjectHandler(@Nonnull Class<? extends ObjectHandler> objectHandlerClass,
								 @Nullable Class<?>... types) {
		Constructor<? extends ObjectHandler<?>> constructor = (Constructor<ObjectHandler<?>>) ClassUtils
				.getConstructorIfAvailable(objectHandlerClass, Dormancy.class);
		ObjectHandler<?> handler = constructor != null
				? BeanUtils.instantiateClass(constructor, dormancy)
				: (ObjectHandler<?>) BeanUtils.instantiateClass(objectHandlerClass);
		if (handler instanceof PersistenceUnitProviderAware) {
			((PersistenceUnitProviderAware) handler).setPersistenceUnitProvider(persistenceUnitProvider);
		}
		addObjectHandler(handler, types);
	}

	/**
	 * Registers the given {@link ObjectHandler ObjectHandlers}.<br/>
	 * Note that yny previously registered {@link ObjectHandler ObjectHandlers} are removed immediately.
	 *
	 * @param objectHandlers the object handlers to use
	 */
	@Inject
	public void setObjectHandlers(@Nonnull Iterable<ObjectHandler<?>> objectHandlers) {
		handlerMap.clear();
		dynamicHandlers.clear();

		for (ObjectHandler<?> handler : objectHandlers) {
			addObjectHandler(handler);
		}
	}
}
