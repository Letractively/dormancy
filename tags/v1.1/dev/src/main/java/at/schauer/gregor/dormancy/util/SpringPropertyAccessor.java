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
package at.schauer.gregor.dormancy.util;

import org.springframework.beans.*;
import org.springframework.core.convert.TypeDescriptor;

import java.util.Map;

/**
 * @author Gregor Schauer
 */
public class SpringPropertyAccessor implements PropertyAccessor {
	protected Object target;
	protected BeanWrapper propertyAccessor;
	protected ConfigurablePropertyAccessor directAccessor;

	public SpringPropertyAccessor(Object target) {
		this.target = target;
		propertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(target);
		directAccessor = PropertyAccessorFactory.forDirectFieldAccess(target);
	}

	@Override
	public boolean isReadableProperty(String propertyName) {
		boolean readableProperty = propertyAccessor.isReadableProperty(propertyName);
		return readableProperty ? readableProperty : directAccessor.isReadableProperty(propertyName);
	}

	@Override
	public boolean isWritableProperty(String propertyName) {
		boolean writableProperty = propertyAccessor.isWritableProperty(propertyName);
		return writableProperty ? writableProperty : directAccessor.isWritableProperty(propertyName);
	}

	@Override
	public Class getPropertyType(String propertyName) {
		Class propertyType = propertyAccessor.getPropertyType(propertyName);
		return propertyType != null ? propertyType : directAccessor.getPropertyType(propertyName);
	}

	@Override
	public TypeDescriptor getPropertyTypeDescriptor(String propertyName) {
		TypeDescriptor descriptor = propertyAccessor.getPropertyTypeDescriptor(propertyName);
		return descriptor != null ? descriptor : directAccessor.getPropertyTypeDescriptor(propertyName);
	}

	@Override
	public Object getPropertyValue(String propertyName) {
		Object value = propertyAccessor.getPropertyValue(propertyName);
		return value != null ? value : directAccessor.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		try {
			propertyAccessor.setPropertyValue(propertyName, value);
		} catch (BeansException ignored) {
			directAccessor.setPropertyValue(propertyName, value);
		}
	}

	@Override
	public void setPropertyValue(PropertyValue pv) {
		try {
			propertyAccessor.setPropertyValue(pv);
		} catch (BeansException ignored) {
			directAccessor.setPropertyValue(pv);
		}
	}

	@Override
	public void setPropertyValues(Map<?, ?> map) {
		try {
			propertyAccessor.setPropertyValues(map);
		} catch (BeansException ignored) {
			directAccessor.setPropertyValues(map);
		}
	}

	@Override
	public void setPropertyValues(PropertyValues pvs) {
		try {
			propertyAccessor.setPropertyValues(pvs);
		} catch (BeansException ignored) {
			directAccessor.setPropertyValues(pvs);
		}
	}

	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) {
		try {
			propertyAccessor.setPropertyValues(pvs, ignoreUnknown);
		} catch (BeansException ignored) {
			directAccessor.setPropertyValues(pvs, ignoreUnknown);
		}
	}

	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid) {
		try {
			propertyAccessor.setPropertyValues(pvs, ignoreUnknown, ignoreInvalid);
		} catch (BeansException ignored) {
			directAccessor.setPropertyValues(pvs, ignoreUnknown, ignoreInvalid);
		}
	}
}
