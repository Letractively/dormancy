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
package at.schauer.gregor.dormancy.access;

import org.springframework.beans.*;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;

import javax.annotation.Nullable;

import static at.schauer.gregor.dormancy.access.PropertyAccessStrategy.AccessMode.FIELD;

/**
 * Uses the provided {@link PropertyAccessStrategy} to determine how to access the properties of a certain object.
 * <p/>
 * If a property should be accessed, this implementation performs a lookup of the appropriate strategy for
 * the requested property. Afterwards, another {@link PropertyAccessor} is used for accessing it.<br/>
 * If the property should be accessed via the getter and setter methods, a {@link BeanWrapperImpl} will be used.<br/>
 * Otherwise, a {@link DirectFieldAccessor} is used to access the properties directly without possible side-effects
 * caused by invoking getter or setter methods.
 *
 * @author Gregor Schauer
 * @since 1.1.2
 */
public class StrategyPropertyAccessor extends AbstractPropertyAccessor {
	protected PropertyAccessStrategy strategy;
	protected ConfigurablePropertyAccessor fieldAccessor;
	protected BeanWrapper propertyAccessor;
	protected Object target;

	public StrategyPropertyAccessor(Object target, PropertyAccessStrategy strategy) {
		this.target = target;
		this.strategy = strategy;
	}

	@Override
	public boolean isReadableProperty(String propertyName) {
		return strategy.propertyAccessTypeMap.containsKey(propertyName);
	}

	@Override
	public boolean isWritableProperty(String propertyName) {
		return strategy.propertyAccessTypeMap.containsKey(propertyName);
	}

	@Override
	public Class<?> getPropertyType(String propertyPath) {
		return getPropertyAccessor(propertyPath).getPropertyType(propertyPath);
	}

	@Override
	public TypeDescriptor getPropertyTypeDescriptor(String propertyName) {
		return getPropertyAccessor(propertyName).getPropertyTypeDescriptor(propertyName);
	}

	@Override
	public Object getPropertyValue(String propertyName) {
		return getPropertyAccessor(propertyName).getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		getPropertyAccessor(propertyName).setPropertyValue(propertyName, value);
	}

	@Override
	public void setPropertyValue(PropertyValue pv) {
		getPropertyAccessor(pv.getName()).setPropertyValue(pv);
	}

	/**
	 * Returns the appropriate {@link PropertyAccessor} to use for accessing the given named property.<br/>
	 * The decision is made by using the provided {@link PropertyAccessStrategy}.
	 *
	 * @param propertyName the name of the property to access
	 * @return the property accessor to use
	 * @see #getFieldAccessor()
	 * @see #getPropertyAccessor()
	 */
	public PropertyAccessor getPropertyAccessor(String propertyName) {
		PropertyAccessStrategy.AccessMode accessMode = strategy.getAccessMode(propertyName);
		return accessMode == FIELD ? getFieldAccessor() : getPropertyAccessor();
	}

	/**
	 * Returns the {@link PropertyAccessor} to use for accessing the properties directly.<br/>
	 * If the {@link PropertyAccessor} is not available, a new instance is created on-demand.
	 *
	 * @return the property accessor to use
	 */
	public ConfigurablePropertyAccessor getFieldAccessor() {
		if (fieldAccessor == null) {
			fieldAccessor = PropertyAccessorFactory.forDirectFieldAccess(target);
		}
		return fieldAccessor;
	}

	/**
	 * Returns the {@link PropertyAccessor} to use for accessing the properties via getter / setter methods.<br/>
	 * If the {@link PropertyAccessor} is not available, a new instance is created on-demand.
	 *
	 * @return the property accessor to use
	 */
	public BeanWrapper getPropertyAccessor() {
		if (propertyAccessor == null) {
			propertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(target);
		}
		return propertyAccessor;
	}

	/**
	 * Casts the value to the required type.
	 * <p/>
	 * This method can be overridden to enable the {@link PropertyAccessor} to perform advanced property conversion
	 * e.g., by using a {@link org.springframework.core.convert.ConversionService ConversionService}.
	 *
	 * @param value        the value to convert
	 * @param requiredType the type to convert to (or {@code null} if not known, for example in case of a collection element)
	 * @param methodParam  the method parameter that is the target of the conversion (for analysis of generic types; may be {@code null})
	 * @return the new value, possibly the result of type conversion
	 */
	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType, @Nullable MethodParameter methodParam) {
		return requiredType == null ? (T) value : requiredType.cast(value);
	}
}