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
package at.dormancy.access;

import org.springframework.beans.*;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static at.dormancy.access.AbstractPropertyAccessStrategy.AccessType.FIELD;

/**
 * Uses the provided {@link AbstractPropertyAccessStrategy} to determine how to access an object´s properties.
 * <p/>
 * If a property should be accessed, this implementation performs a lookup of the appropriate strategy for
 * the requested property. Afterwards, another {@link PropertyAccessor} is used for accessing it.<br/>
 * If the property should be accessed via the getter and setter methods, a {@link BeanWrapperImpl} will be used.<br/>
 * Otherwise, a {@link DirectFieldAccessor} is used to access the properties directly without possible side-effects
 * caused by invoking getter or setter methods.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class StrategyPropertyAccessor extends AbstractPropertyAccessor {
	protected AbstractPropertyAccessStrategy strategy;
	protected ConfigurablePropertyAccessor fieldAccessor;
	protected BeanWrapper propertyAccessor;
	protected Object target;

	public StrategyPropertyAccessor(@Nonnull Object target, @Nonnull AbstractPropertyAccessStrategy strategy) {
		this.target = target;
		this.strategy = strategy;
	}

	@Override
	public boolean isReadableProperty(@Nonnull String propertyName) {
		return strategy.propertyAccessTypeMap.containsKey(propertyName);
	}

	@Override
	public boolean isWritableProperty(@Nonnull String propertyName) {
		return strategy.propertyAccessTypeMap.containsKey(propertyName);
	}

	@Nonnull
	@Override
	public Class<?> getPropertyType(@Nonnull String propertyPath) {
		return getPropertyAccessor(propertyPath).getPropertyType(propertyPath);
	}

	@Nonnull
	@Override
	public TypeDescriptor getPropertyTypeDescriptor(@Nonnull String propertyName) {
		return getPropertyAccessor(propertyName).getPropertyTypeDescriptor(propertyName);
	}

	@Nullable
	@Override
	public Object getPropertyValue(@Nonnull String propertyName) {
		return getPropertyAccessor(propertyName).getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(@Nonnull String propertyName, @Nullable Object value) {
		getPropertyAccessor(propertyName).setPropertyValue(propertyName, value);
	}

	@Override
	public void setPropertyValue(@Nonnull PropertyValue pv) {
		getPropertyAccessor(pv.getName()).setPropertyValue(pv);
	}

	/**
	 * Returns the appropriate {@link PropertyAccessor} to use for accessing the given named property.<br/>
	 * The decision is made by using the provided {@link AbstractPropertyAccessStrategy}.
	 *
	 * @param propertyName the name of the property to access
	 * @return the property accessor to use
	 * @see #getFieldAccessor()
	 * @see #getPropertyAccessor()
	 */
	@Nonnull
	public PropertyAccessor getPropertyAccessor(@Nonnull String propertyName) {
		AbstractPropertyAccessStrategy.AccessType accessType = strategy.getAccessType(propertyName);
		return accessType == FIELD ? getFieldAccessor() : getPropertyAccessor();
	}

	/**
	 * Returns the {@link PropertyAccessor} to use for accessing the properties directly.<br/>
	 * If the {@link PropertyAccessor} is not available, a new instance is created on-demand.
	 *
	 * @return the property accessor to use
	 */
	@Nonnull
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
	@Nonnull
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
