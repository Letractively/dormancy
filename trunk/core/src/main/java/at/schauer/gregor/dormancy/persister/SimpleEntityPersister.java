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
import at.schauer.gregor.dormancy.beans.HibernatePropertyAccessor;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.Setter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Processes all non-static non-final fields that are declared in the transient as well as the persistent object.<br/>
 * Additionally, the source type must be located within any of the given packages.
 *
 * @author Gregor Schauer
 * @since 1.0.3
 */
public class SimpleEntityPersister<C> extends FieldFilterEntityPersister<C> implements DynamicEntityPersister<C> {
	protected List<String> packageNames = new ArrayList<String>();
	protected HibernatePropertyAccessor propertyAccessor;

	@Inject
	public SimpleEntityPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
		setFieldFilters(new ReflectionUtils.FieldFilter() {
			@Override
			public boolean matches(Field field) {
				return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
			}
		});
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (C) tree.get(dbObj);
		}

		Set<Field> fields = filter(dbObj);
		T trObj = createObject(dbObj);
		tree.put(dbObj, trObj);

		PropertyAccessor propertyAccessor = getPropertyAccessor();
		for (Field field : fields) {
			try {
				Getter getter = propertyAccessor.getGetter(dbObj.getClass(), field.getName());
				Setter setter = propertyAccessor.getSetter(trObj.getClass(), field.getName());
				Object dbVal = getter.get(dbObj);
				Object trVal = dormancy.clone_(dbVal, tree);
				setter.set(trObj, trVal, null);
			} catch (PropertyNotFoundException e) {
				// Ignore properties that cannot be copied i.e., because they are added at runtime by Javassist
			}
		}
		return trObj;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		tree.put(trObj, dbObj);

		Set<Field> fields = filter(dbObj);
		PropertyAccessor propertyAccessor = getPropertyAccessor();
		for (Field field : fields) {
			Getter getter = propertyAccessor.getGetter(trObj.getClass(), field.getName());
			Setter setter = propertyAccessor.getSetter(dbObj.getClass(), field.getName());
			Object trVal = getter.get(trObj);
			Object dbVal = dormancy.merge_(trVal, tree);
			setter.set(dbObj, dbVal, null);
		}
		return dbObj;
	}

	/**
	 * Returns the PropertyAccessor used for reading and writing fields.<br/>
	 * If no PropertyAccessor is set, a HibernatePropertyAccessor is returned.
	 *
	 * @return the PropertyAccessor
	 */
	@Nonnull
	public PropertyAccessor getPropertyAccessor() {
		if (propertyAccessor == null) {
			propertyAccessor = new HibernatePropertyAccessor();
		}
		return propertyAccessor;
	}

	/**
	 * Sets the PropertyAccessor to use for reading and writing fields.
	 *
	 * @param propertyAccessor the PropertyAccessor to use
	 */
	public void setPropertyAccessor(@Nullable HibernatePropertyAccessor propertyAccessor) {
		this.propertyAccessor = propertyAccessor;
	}

	/**
	 * Sets the package prefixes of the object types that are supported by this instance.
	 *
	 * @param packageNames the package names
	 */
	public void setPackageNames(@Nonnull String... packageNames) {
		Assert.notEmpty(packageNames, "'packageNames' must not be null or empty");
		CollectionUtils.addAll(this.packageNames, packageNames);
	}

	@Override
	public boolean supports(@Nonnull Class clazz) {
		for (String packageName : packageNames) {
			if (clazz.getName().startsWith(packageName)) {
				return true;
			}
		}
		return false;
	}
}
