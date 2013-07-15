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
package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import org.springframework.core.CollectionFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Map;

/**
 * Processes all types of {@link java.util.Map}s by traversing them and invoking the desired operation of the
 * appropriate {@link EntityPersister} for all keys and values.
 *
 * @author Gregor Schauer
 */
public class MapPersister extends AbstractContainerPersister<Map<?, ?>> {
	@Inject
	@SuppressWarnings("unchecked")
	public MapPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
		supportedTypes = new Class[]{Map.class};
	}

	@Nullable
	@Override
	public <T extends Map<?, ?>> Map clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (Map) tree.get(dbObj);
		}
		Map<Object, Object> map = createContainer(dbObj);
		tree.put(dbObj, map);

		for (Map.Entry<?, ?> entry : dbObj.entrySet()) {
			Object key = dormancy.clone_(entry.getKey(), tree);
			Object value = dormancy.clone_(entry.getValue(), tree);
			map.put(key, value);
		}
		return map;
	}

	@Nullable
	@Override
	public <T extends Map<?, ?>> Map<?, ?> merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (Map<?, ?>) tree.get(trObj);
		}
		Map<Object, Object> map = createContainer(trObj);
		tree.put(trObj, map);

		for (Map.Entry<?, ?> entry : trObj.entrySet()) {
			Object key = dormancy.merge_(entry.getKey(), tree);
			Object value = dormancy.merge_(entry.getValue(), tree);
			map.put(key, value);
		}
		return map;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Map<?, ?>> Map<?, ?> merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (Map<?, ?>) tree.get(trObj);
		}
		Map container = createContainer(dbObj);
		tree.put(trObj, container);

		// Create a modifiable copy of the persistent map
		Map<Object, Object> dbCopy = createContainer(dbObj);
		dbCopy.putAll(dbObj);

		for (Map.Entry<?, ?> trEntry : trObj.entrySet()) {
			// For every transient key, find a persistent element and the associated value
			Object dbKey = trEntry.getKey() != null ? dormancy.getUtils().findPendant(trEntry.getKey(), dbCopy.keySet()) : null;
			Object dbValue = dbKey != null ? dbObj.get(dbKey) : null;

			// Merge the retrieved keys and values (if possible)
			Object mKey = null, mValue = null;
			if (trEntry.getKey() != null) {
				mKey = dbKey != null ? dormancy.merge_(trEntry.getKey(), dbKey, tree) : dormancy.merge_(trEntry.getKey(), tree);
			}
			if (trEntry.getValue() != null) {
				mValue = dbValue != null ? dormancy.merge_(trEntry.getValue(), dbValue, tree) : dormancy.merge_(trEntry.getValue(), tree);
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
	protected Map<Object, Object> createContainer(@Nonnull Map<?, ?> container) {
		return CollectionFactory.createApproximateMap(container, container.size());
	}

	@Override
	public Class<?>[] getSupportedTypes() {
		return supportedTypes;
	}
}
