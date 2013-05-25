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
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.beans.PropertyDescriptor;

/**
 * Uses {@link BeanWrapper}s for reading and writing bean properties.
 *
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class BeanPropertyAccessorPersister<C> extends AbstractPropertyAccessorPersister<C, BeanWrapper> {
	@Inject
	public BeanPropertyAccessorPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	/**
	 * @param target the object to create a BeanWrapper for
	 * @return the BeanWrapper
	 * @inheritDoc
	 * @see PropertyAccessorFactory#forBeanPropertyAccess(Object)
	 */
	@Nonnull
	@Override
	protected BeanWrapper createPropertyAccessor(@Nonnull Object target) {
		return PropertyAccessorFactory.forBeanPropertyAccess(target);
	}

	/**
	 * @param propertyAccessor the PropertyAccessor
	 * @return the property names
	 * @inheritDoc
	 * @see org.springframework.beans.BeanWrapper#getPropertyDescriptors()
	 */
	@Nonnull
	@Override
	protected String[] getPropertyNames(BeanWrapper propertyAccessor) {
		PropertyDescriptor[] descriptors = propertyAccessor.getPropertyDescriptors();
		String[] names = new String[descriptors.length];
		for (int i = 0; i < descriptors.length; i++) {
			PropertyDescriptor descriptor = descriptors[i];
			names[i] = descriptor.getName();
		}
		return names;
	}
}
