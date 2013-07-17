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
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * Uses {@link ConfigurablePropertyAccessor}s for reading and writing bean properties.
 *
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class DirectFieldAccessorPersister<C> extends AbstractPropertyAccessorPersister<C, ConfigurablePropertyAccessor> {
	@Inject
	public DirectFieldAccessorPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	/**
	 * @param target the object to create a BeanWrapper for
	 * @return the BeanWrapper
	 * @see PropertyAccessorFactory#forDirectFieldAccess(Object)
	 */
	@Nonnull
	@Override
	protected ConfigurablePropertyAccessor createPropertyAccessor(@Nonnull Object target) {
		return PropertyAccessorFactory.forDirectFieldAccess(target);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	protected String[] getPropertyNames(ConfigurablePropertyAccessor propertyAccessor) {
		Map<String, Field> fieldMap = (Map<String, Field>) ReflectionTestUtils.getField(propertyAccessor, "fieldMap");
		Set<String> names = fieldMap.keySet();
		return names.toArray(new String[names.size()]);
	}
}
