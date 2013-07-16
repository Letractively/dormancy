/*
 * Copyright 2013 Gregor Schauer
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
import org.apache.commons.lang.reflect.FieldUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Processes all named properties by retrieving the value, invoking the desired {@link Dormancy} operation and writing
 * the result back to the bean.
 *
 * @author Gregor Schauer
 * @since 1.0.1
 */
public class BeanPropertiesPersister<C> extends GenericEntityPersister<C> {
	protected List<String> propertyNames = new ArrayList<String>();

	@Inject
	public BeanPropertiesPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (C) tree.get(dbObj);
		}
		try {
			T trObj = createObject(dbObj);
			for (String propertyName : propertyNames) {
				Object dbVal = FieldUtils.readField(dbObj, propertyName, true);
				Object trVal = dormancy.clone_(dbVal, tree);
				FieldUtils.writeField(trObj, propertyName, trVal, true);
			}
			return trObj;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		return merge_(trObj, createObject(trObj), tree);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		try {
			for (String propertyName : propertyNames) {
				Object trVal = FieldUtils.readField(trObj, propertyName, true);
				Object dbVal = dormancy.merge_(trVal, tree);
				FieldUtils.writeField(dbObj, propertyName, dbVal, true);
			}
			return dbObj;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the names of the properties to process.<p/>
	 * Note that the returned list is neither a copy nor unmodifiable.<br/>
	 * Thus modifications will immediately take effect.
	 *
	 * @return the property names
	 */
	public List<String> getPropertyNames() {
		return propertyNames;
	}

	/**
	 * Sets the names of the properties to process.
	 *
	 * @param propertyNames the property names
	 */
	public void setPropertyNames(List<String> propertyNames) {
		this.propertyNames = propertyNames;
	}
}
