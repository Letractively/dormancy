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
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Processes all types of arrays by traversing them and invoking the desired operation of the appropriate
 * {@link EntityPersister} for all elements.
 *
 * @author Gregor Schauer
 * @since 1.1.0
 */
public class ArrayPersister<C> extends AbstractContainerPersister<C> implements DynamicEntityPersister<C> {
	@Inject
	public ArrayPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null) {
			return null;
		}
		List list = dormancy.clone_(CollectionUtils.arrayToList(dbObj), tree);
		C container = createContainer(dbObj);
		for (int i = 0; i < list.size(); i++) {
			Array.set(container, i, list.get(i));
		}
		return container;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null) {
			return null;
		}
		List<?> list = dormancy.merge_(arrayToList(trObj), tree);
		C container = createContainer(trObj);
		for (int i = 0; i < list.size(); i++) {
			Array.set(container, i, list.get(i));
		}
		return container;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null) {
			return null;
		}
		List<?> list = dormancy.merge_(arrayToList(trObj), arrayToList(dbObj), tree);
		C container = createContainer(dbObj);
		for (int i = 0; i < list.size(); i++) {
			Array.set(container, i, list.get(i));
		}
		return container;
	}

	@Nonnull
	protected <T extends C> List<?> arrayToList(@Nonnull T array) {
		ArrayList<Object> list = new ArrayList<Object>(ObjectUtils.toObjectArray(array).length);
		Collections.addAll(list, ObjectUtils.toObjectArray(array));
		return list;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	protected C createContainer(@Nonnull C container) {
		Class<?> componentType = container.getClass().getComponentType();
		return (C) Array.newInstance(componentType, ArrayUtils.getLength(container));
	}

	@Override
	public boolean supports(@Nonnull Class<?> clazz) {
		return clazz.isArray();
	}
}
