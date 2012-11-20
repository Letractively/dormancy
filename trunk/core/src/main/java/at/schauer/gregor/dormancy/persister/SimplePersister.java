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
import org.hibernate.PropertyNotFoundException;
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
public class SimplePersister<C> extends FieldFilterPersister<C> implements DynamicEntityPersister<C> {
	protected List<String> packageNames = new ArrayList<String>();

	@Inject
	public SimplePersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
		setFieldFilters(new ReflectionUtils.FieldFilter() {
			@Override
			public boolean matches(@Nonnull Field field) {
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

		for (Field field : fields) {
			try {
				Object dbVal = dormancy.getUtils().getPropertyValue(null, dbObj, field.getName());
				Object trVal = dormancy.clone_(dbVal, tree);
				dormancy.getUtils().setPropertyValue(null, trObj, field.getName(), trVal);
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
		for (Field field : fields) {
			Object trVal = dormancy.getUtils().getPropertyValue(null, trObj, field.getName());
			Object dbVal = dormancy.merge_(trVal, tree);
			dormancy.getUtils().setPropertyValue(null, dbObj, field.getName(), dbVal);
		}
		return dbObj;
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
	public boolean supports(@Nonnull Class<?> clazz) {
		for (String packageName : packageNames) {
			if (clazz.getName().startsWith(packageName)) {
				return true;
			}
		}
		return false;
	}
}
