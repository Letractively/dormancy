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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Uses {@link org.springframework.util.ReflectionUtils.FieldFilter}s for determining the properties to process.<br/>
 *
 * @author Gregor Schauer
 * @since 1.0.1
 */
public class FieldFilterEntityPersister<C> extends GenericEntityPersister<C> {
	protected List<ReflectionUtils.FieldFilter> fieldFilters;

	@Inject
	public FieldFilterEntityPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (C) tree.get(dbObj);
		}
		try {
			Set<Field> fields = filter(dbObj);
			T trObj = createObject(dbObj);
			tree.put(dbObj, trObj);

			for (Field field : fields) {
				Object dbVal = FieldUtils.readField(field, dbObj, true);
				Object trVal = dormancy.clone_(dbVal, tree);
				FieldUtils.writeField(field, trObj, trVal, true);
			}
			return trObj;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		return merge_(trObj, createObject(trObj), tree);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		tree.put(trObj, dbObj);

		try {
			Set<Field> fields = filter(dbObj);
			for (Field field : fields) {
				Object trVal = FieldUtils.readField(field, trObj, true);
				Object dbVal = dormancy.merge_(trVal, tree);
				FieldUtils.writeField(field, dbObj, dbVal, true);
			}
			return dbObj;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns all fields matching at least one provided {@link org.springframework.util.ReflectionUtils.FieldFilter}.
	 *
	 * @param obj the object to process
	 * @param <T> the type of the object
	 * @return the fields
	 */
	@Nonnull
	protected <T extends C> Set<Field> filter(@Nonnull T obj) {
		final Set<Field> fields = new LinkedHashSet<Field>();
		for (ReflectionUtils.FieldFilter filter : getFieldFilters()) {
			ReflectionUtils.doWithFields(obj.getClass(), new ReflectionUtils.FieldCallback() {
				@Override
				public void doWith(Field field) {
					field.setAccessible(true);
					fields.add(field);
				}
			}, filter);
		}
		return fields;
	}

	/**
	 * Returns the field filters used for determining the properties to process.<p/>
	 * Note that the returned list is neither a copy nor unmodifiable.<br/>
	 * Thus modifications will immediately take effect.
	 *
	 * @return the field filters
	 */
	@Nonnull
	public List<ReflectionUtils.FieldFilter> getFieldFilters() {
		if (fieldFilters == null) {
			fieldFilters = new ArrayList<ReflectionUtils.FieldFilter>();
		}
		return fieldFilters;
	}

	/**
	 * Sets the field filters used for determining the properties to process.
	 *
	 * @param filters the field filters
	 */
	public void setFieldFilters(@Nullable ReflectionUtils.FieldFilter... filters) {
		getFieldFilters().clear();
		if (ArrayUtils.isNotEmpty(filters)) {
			CollectionUtils.addAll(getFieldFilters(), filters);
		}
	}
}
