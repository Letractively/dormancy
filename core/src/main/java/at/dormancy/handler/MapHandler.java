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
package at.dormancy.handler;

import at.dormancy.Dormancy;
import at.dormancy.util.DormancyContext;
import org.springframework.core.CollectionFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Processes all types of {@link Map}s by traversing them and invoking the desired operation of the appropriate
 * {@link ObjectHandler} for all keys and values.
 *
 * @author Gregor Schauer
 */
public class MapHandler implements StaticObjectHandler<Map<?, ?>> {
	private final Dormancy dormancy;
	private final Set<Class<?>> supportedTypes;

	@Inject
	@SuppressWarnings("unchecked")
	public MapHandler(@Nonnull Dormancy dormancy) {
		this.dormancy = dormancy;
		supportedTypes = new HashSet<Class<?>>();
		supportedTypes.add((Class) Map.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Map<?, ?>, O extends R> R disconnect(@Nullable O dbObj, @Nonnull DormancyContext ctx) {
		Map<Object, Object> adjacencyMap = ctx.getAdjacencyMap();
		if (dbObj == null || adjacencyMap.containsKey(dbObj)) {
			return (R) adjacencyMap.get(dbObj);
		}
		Map<Object, Object> map = (Map<Object, Object>) createObject(dbObj);
		adjacencyMap.put(dbObj, map);

		for (Map.Entry<?, ?> entry : dbObj.entrySet()) {
			Object key = dormancy.asObjectHandler().disconnect(entry.getKey(), ctx);
			Object value = dormancy.asObjectHandler().disconnect(entry.getValue(), ctx);
			map.put(key, value);
		}
		return (R) map;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <O extends Map<?, ?>, R extends O> R apply(@Nullable O trObj, @Nonnull R dbObj,
													  @Nonnull DormancyContext ctx) {
		Map<Object, Object> adjacencyMap = ctx.getAdjacencyMap();
		if (trObj == null || adjacencyMap.containsKey(trObj)) {
			return (R) adjacencyMap.get(trObj);
		}

		if (dormancy.getUtils().isPersistentCollection(dbObj)
				&& !dormancy.getUtils().isInitializedPersistentCollection(dbObj)) {
			// If property is loaded lazily, the value of the given object must be null or empty
			if (trObj != dbObj && Map.class.cast(dbObj).size() > 0) {
				throw dormancy.getUtils().exceptions().throwLazyInitializationException(dbObj);
			}
			return dbObj;
		}

		Map container = createObject(dbObj);
		adjacencyMap.put(trObj, container);

		// Create a modifiable copy of the persistent map
		Map<Object, Object> dbCopy = (Map<Object, Object>) createObject(dbObj);
		dbCopy.putAll(dbObj);

		for (Map.Entry<?, ?> trEntry : trObj.entrySet()) {
			// For every transient key, find a persistent element and the associated value
			Object dbKey = trEntry.getKey() == null ? null
					: dormancy.getUtils().findPendant(trEntry.getKey(), dbCopy.keySet());
			Object dbValue = dbKey != null ? dbObj.get(dbKey) : null;

			// Merge the retrieved keys and values (if possible)
			Object mKey = null, mValue = null;
			if (trEntry.getKey() != null) {
				mKey = dbKey != null ? dormancy.asObjectHandler().apply(trEntry.getKey(), dbKey, ctx)
						: dormancy.asObjectHandler().apply(trEntry.getKey(), null, ctx);
			}
			if (trEntry.getValue() != null) {
				mValue = dbValue != null ? dormancy.asObjectHandler().apply(trEntry.getValue(), dbValue, ctx)
						: dormancy.asObjectHandler().apply(trEntry.getValue(), null, ctx);
			}

			container.put(mKey, mValue);
		}

		// Add the processed entities to the persistent collection
		dbObj.clear();
		dbObj.putAll(container);
		return dbObj;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <R extends Map<?, ?>> R createObject(@Nonnull R obj) {
		return (R) CollectionFactory.createApproximateMap(obj, obj.size());
	}

	@Nonnull
	@Override
	public Set<Class<?>> getSupportedTypes() {
		return supportedTypes;
	}
}
