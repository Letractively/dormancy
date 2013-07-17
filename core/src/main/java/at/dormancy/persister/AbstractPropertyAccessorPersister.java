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
package at.dormancy.persister;

import at.dormancy.Dormancy;
import org.springframework.beans.PropertyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Map;

/**
 * Uses {@link PropertyAccessor}s for reading and writing bean properties.
 *
 * @author Gregor Schauer
 * @since 1.0.2
 */
public abstract class AbstractPropertyAccessorPersister<C, PA extends PropertyAccessor> extends GenericEntityPersister<C> {
	@Inject
	protected AbstractPropertyAccessorPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (C) tree.get(dbObj);
		}
		T trObj = createObject(dbObj);
		PA dbPropertyAccessor = createPropertyAccessor(dbObj);
		PA trPopertyAccessor = createPropertyAccessor(trObj);
		for (String name : getPropertyNames(dbPropertyAccessor)) {
			if (dbPropertyAccessor.isReadableProperty(name) && trPopertyAccessor.isWritableProperty(name)) {
				Object dbValue = dbPropertyAccessor.getPropertyValue(name);
				Object trValue = dormancy.clone_(dbValue, tree);
				trPopertyAccessor.setPropertyValue(name, trValue);
			}
		}
		return trObj;
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
		PA dbPropertyAccessor = createPropertyAccessor(dbObj);
		PA trPropertyAccessor = createPropertyAccessor(trObj);
		for (String name : getPropertyNames(trPropertyAccessor)) {
			if (trPropertyAccessor.isReadableProperty(name) && dbPropertyAccessor.isWritableProperty(name)) {
				Object trValue = trPropertyAccessor.getPropertyValue(name);
				Object dbValue = dormancy.merge_(trValue, tree);
				dbPropertyAccessor.setPropertyValue(name, dbValue);
			}
		}
		return dbObj;
	}

	/**
	 * Creates a new {@link PropertyAccessor} for the given object.
	 *
	 * @param target the object to create a PropertyAccessor for
	 * @return the PropertyAccessor
	 */
	@Nonnull
	protected abstract PA createPropertyAccessor(@Nonnull Object target);

	/**
	 * Returns the names of all properties that can be accessed by the given {@link PropertyAccessor}.
	 *
	 * @param propertyAccessor the PropertyAccessor
	 * @return the property names
	 */
	@Nonnull
	protected abstract String[] getPropertyNames(PA propertyAccessor);
}
