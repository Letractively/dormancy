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
import com.google.common.base.Predicate;
import org.apache.commons.lang.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.util.ObjectUtils.toObjectArray;

/**
 * Processes all types of arrays by traversing them and invoking the desired operation of the appropriate
 * {@link ObjectHandler} for all elements.
 *
 * @param <T> the type of the array
 * @author Gregor Schauer
 * @since 1.1.0
 */
public class ArrayHandler<T> implements ObjectHandler<T>, DynamicObjectHandler<T> {
	Dormancy<?, ?, ?> dormancy;

	@Inject
	public ArrayHandler(@Nonnull Dormancy<?, ?, ?> dormancy) {
		this.dormancy = dormancy;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <R extends T> R createObject(@Nonnull R obj) {
		Class<?> componentType = obj.getClass().getComponentType();
		return (R) Array.newInstance(componentType, Array.getLength(obj));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends T, O extends R> R disconnect(@Nullable O dbObj, @Nonnull DormancyContext ctx) {
		Map<Object, Object> adjacencyMap = ctx.getAdjacencyMap();
		if (dbObj == null) {
			return null;
		} else if (adjacencyMap.containsKey(dbObj)) {
			return (R) adjacencyMap.get(dbObj);
		}

		R container = createObject(dbObj);
		adjacencyMap.put(dbObj, container);

		List list = dormancy.asObjectHandler().disconnect(arrayToList(dbObj), ctx);
		for (int i = 0; i < list.size(); i++) {
			Array.set(container, i, list.get(i));
		}
		return container;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <O extends T, R extends O> R apply(@Nullable O trObj, @Nonnull R dbObj, @Nonnull DormancyContext ctx) {
		Map<Object, Object> adjacencyMap = ctx.getAdjacencyMap();
		if (trObj == null) {
			return dbObj;
		} else if (adjacencyMap.containsKey(trObj)) {
			return (R) adjacencyMap.get(trObj);
		}

		R container = createObject(dbObj);
		adjacencyMap.put(trObj, dbObj);

		ArrayList<Object> trList = newArrayList(arrayToList(trObj));
		ArrayList<Object> dbList = newArrayList(arrayToList(dbObj));
		List list = dormancy.asObjectHandler().apply(trList, dbList, ctx);
		for (int i = 0; i < list.size(); i++) {
			Array.set(container, i, list.get(i));
		}
		return container;
	}

	/**
	 * Creates a new {@link List} and copies all elements from the given array into it.
	 *
	 * @param array the array
	 * @param <A>   the type of the array
	 * @return the newly created list
	 */
	@Nonnull
	protected <A extends T> List<?> arrayToList(@Nonnull A array) {
		// Just for efficiency
		ArrayList<Object> list = new ArrayList<Object>(ArrayUtils.getLength(array));
		Collections.addAll(list, toObjectArray(array));
		return list;
	}

	@Nonnull
	@Override
	public Predicate<Class<?>> getPredicate() {
		return new Predicate<Class<?>>() {
			@Override
			public boolean apply(@Nullable Class<?> input) {
				return input != null && input.isArray();
			}
		};
	}
}
