/*
 * Copyright 2014 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.access;

import at.dormancy.metadata.ObjectMetadata;
import at.dormancy.util.ClassLookup;
import org.apache.log4j.Logger;
import org.springframework.beans.*;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

import static at.dormancy.access.AccessType.FIELD;

/**
 * Uses the provided {@link ObjectMetadata} to determine how to access an object´s properties.
 * <p/>
 * If a property should be accessed, this implementation performs a lookup of the appropriate metadata for the
 * requested property. Afterwards, another {@link PropertyAccessor} is used for accessing it.<br/>
 * If the property should be accessed via the getter and setter methods, a
 * {@link org.springframework.beans.BeanWrapperImpl} will be used.<br/>
 * Otherwise, a {@link org.springframework.beans.DirectFieldAccessor} is used to access the properties directly without
 * possible side-effects caused by invoking getter or setter methods.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class MetadataPropertyAccessor extends AbstractPropertyAccessor {
	private static final Logger logger = Logger.getLogger(MetadataPropertyAccessor.class);
	protected static Field typeConverterDelegateField;
	protected ObjectMetadata metadata;
	protected ConfigurablePropertyAccessor fieldAccessor;
	protected BeanWrapper propertyAccessor;
	protected Object target;

	static {
		if (ClassLookup.forName("org.springframework.beans.TypeConverterSupport") != null) {
			String typeConverterDelegateClassName = "org.springframework.beans.TypeConverterDelegate";
			Class<Object> typeConverterDelegateClass = ClassLookup.find(typeConverterDelegateClassName)
					.orThrow(typeConverterDelegateClassName).get();
			typeConverterDelegateField = ReflectionUtils.findField(MetadataPropertyAccessor.class,
					"typeConverterDelegate", typeConverterDelegateClass);
			ReflectionUtils.makeAccessible(typeConverterDelegateField);
		}
	}

	public MetadataPropertyAccessor(@Nonnull Object target, @Nonnull ObjectMetadata metadata) {
		this.target = target;
		this.metadata = metadata;
	}

	@Override
	public boolean isReadableProperty(@Nonnull String propertyName) {
		return metadata.isProperty(propertyName);
	}

	@Override
	public boolean isWritableProperty(@Nonnull String propertyName) {
		return metadata.isProperty(propertyName);
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
		Object value = getPropertyAccessor(propertyName).getPropertyValue(propertyName);
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Getting property '%s' of '%s': '%s'", propertyName, target, value));
		}
		return value;
	}

	@Override
	public void setPropertyValue(@Nonnull String propertyName, @Nullable Object value) {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Setting property '%s' of '%s' to '%s'", propertyName, target, value));
		}
		getPropertyAccessor(propertyName).setPropertyValue(propertyName, value);
	}

	@Override
	public void setPropertyValue(@Nonnull PropertyValue pv) {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Setting property '%s' of '%s' to '%s'", pv.getName(), target, pv.getValue()));
		}
		getPropertyAccessor(pv.getName()).setPropertyValue(pv);
	}

	/**
	 * Returns the appropriate {@link PropertyAccessor} to use for accessing the given named property.<br/>
	 * The decision is made by using the provided {@link at.dormancy.metadata.resolver.MetadataResolver}.
	 *
	 * @param propertyName the name of the property to access
	 * @return the property accessor to use
	 * @see #getFieldAccessor()
	 * @see #getPropertyAccessor()
	 */
	@Nonnull
	public PropertyAccessor getPropertyAccessor(@Nonnull String propertyName) {
		AccessType accessType = metadata.getAccessType(propertyName);
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
			if (typeConverterDelegateField != null) {
				Object delegate = ReflectionUtils.getField(typeConverterDelegateField, fieldAccessor);
				ReflectionUtils.setField(typeConverterDelegateField, this, delegate);
			}
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
			if (typeConverterDelegateField != null) {
				Object delegate = ReflectionUtils.getField(typeConverterDelegateField, propertyAccessor);
				ReflectionUtils.setField(typeConverterDelegateField, this, delegate);
			}
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
	 * @param requiredType the type to convert to (or {@code null} if not known
	 *                     (for example in case of a collection element)
	 * @param methodParam  the method parameter that is the target of the conversion
	 *                     (for analysis of generic types; may be {@code null})
	 * @return the new value, possibly the result of type conversion
	 */
	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
									@Nullable MethodParameter methodParam) {
		return requiredType == null ? (T) value : requiredType.cast(value);
	}
}
