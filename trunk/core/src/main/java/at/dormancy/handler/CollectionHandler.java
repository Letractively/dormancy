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
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.core.CollectionFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.size;

/**
 * Processes all types of {@link Collection}s by traversing them and invoking the desired operation of the appropriate
 * {@link ObjectHandler} for all elements.
 *
 * @author Gregor Schauer
 */
public class CollectionHandler<C extends Collection> implements StaticObjectHandler<C> {
	final Set<Class<?>> supportedTypes = Sets.<Class<?>>newHashSet(List.class, Set.class);
	Dormancy<?, ?, ?> dormancy;
	Dormancy.DormancyObjectHandler handler;

	@Inject
	public CollectionHandler(@Nonnull Dormancy<?, ?, ?> dormancy) {
		this.dormancy = dormancy;
		this.handler = dormancy.asObjectHandler();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <R extends C> R createObject(@Nonnull R obj) {
		return (R) CollectionFactory.createApproximateCollection(obj, obj.size());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends C, O extends R> R disconnect(@Nullable O dbObj, @Nonnull DormancyContext ctx) {
		Map<Object, Object> adjacencyMap = ctx.getAdjacencyMap();
		if (dbObj == null) {
			return null;
		} else if (adjacencyMap.containsKey(dbObj)) {
			return (R) adjacencyMap.get(dbObj);
		}

		O trObj = createObject(dbObj);
		adjacencyMap.put(dbObj, trObj);

		for (Object dbElement : dbObj) {
			trObj.add(handler.disconnect(dbElement, ctx));
		}
		return trObj;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <O extends C, R extends O> R apply(@Nullable O trObj, @Nonnull R dbObj, @Nonnull DormancyContext ctx) {
		Map<Object, Object> adjacencyMap = ctx.getAdjacencyMap();
		if (trObj == null) {
			return null;
		} else if (adjacencyMap.containsKey(dbObj)) {
			return (R) adjacencyMap.get(dbObj);
		}

		if (dormancy.getUtils().isPersistentCollection(dbObj)
				&& !dormancy.getUtils().isInitializedPersistentCollection(dbObj)) {
			// If property is loaded lazily, the value of the given object must be null or empty
			if (trObj != dbObj && size(trObj) > 0) {
				throw dormancy.getUtils().exceptions().throwLazyInitializationException(dbObj);
			}
			return dbObj;
		}

		dbObj = (R) Objects.firstNonNull(dbObj, Lists.newArrayList());
		O container = createObject(trObj);
		adjacencyMap.put(trObj, dbObj);

		if (dbObj.isEmpty()) {
			for (Object trElement : trObj) {
				container.add(dormancy.asObjectHandler().apply(trElement, ctx));
			}
		} else {
			O dbCopy = createObject(trObj);
			dbCopy.addAll(dbObj);
			for (Object trElement : trObj) {
				Object dbElement = dormancy.getUtils().findPendant(trElement, dbCopy);
				if (dbElement == null) {
					container.add(handler.apply(trElement, ctx));
				} else {
					container.add(handler.apply(trElement, dbElement, ctx));
				}
			}
		}

		dbObj.clear();
		dbObj.addAll(container);
		return dbObj;
	}

	@Nonnull
	@Override
	public Set<Class<?>> getSupportedTypes() {
		return supportedTypes;
	}
}
