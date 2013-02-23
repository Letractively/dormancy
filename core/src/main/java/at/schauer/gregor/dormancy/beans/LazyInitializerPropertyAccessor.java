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
package at.schauer.gregor.dormancy.beans;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.AbstractPropertyAccessor;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Uses {@link PropertyUtils} and a {@link LazyInitializer} for retrieving properties of Hibernate proxies.
 *
 * @author Gregor Schauer
 */
public class LazyInitializerPropertyAccessor extends AbstractPropertyAccessor {
	protected LazyInitializer lazyInitializer;
	protected ClassMetadata metadata;

	public LazyInitializerPropertyAccessor(@Nonnull LazyInitializer lazyInitializer) {
		this.lazyInitializer = lazyInitializer;
	}

	/**
	 * @param propertyName property name to be evaluated
	 * @return {@code true} if the property is readable, otherwise {@code false}
	 * @inheritDoc
	 * @see PropertyUtils#isReadable(Object, String)
	 */
	@Override
	public boolean isReadableProperty(@Nonnull String propertyName) {
		return isIdentifierName(propertyName) || PropertyUtils.isReadable(getTarget(), propertyName);
	}

	/**
	 * @param propertyName property name to be evaluated
	 * @return {@code true} if the property is writable, otherwise {@code false}
	 * @inheritDoc
	 * @see PropertyUtils#isWriteable(Object, String)
	 */
	@Override
	public boolean isWritableProperty(@Nonnull String propertyName) {
		return isIdentifierName(propertyName) || PropertyUtils.isWriteable(getTarget(), propertyName);
	}

	/**
	 * @param propertyName possibly indexed and/or nested name of the property
	 *                     for which a property descriptor is requested
	 * @return The property type
	 * @inheritDoc
	 * @see ClassMetadata#getIdentifierType()
	 * @see PropertyUtils#getPropertyType(Object, String)
	 */
	@Override
	@Nonnull
	public TypeDescriptor getPropertyTypeDescriptor(@Nonnull String propertyName) {
		try {
			if (isIdentifierName(propertyName)) {
				return TypeDescriptor.forObject(metadata.getIdentifierType());
			} else {
				return TypeDescriptor.valueOf(PropertyUtils.getPropertyType(getTarget(), propertyName));
			}
		} catch (Exception e) {
			throw throwException(propertyName, e);
		}
	}

	/**
	 * @param propertyName possibly indexed and/or nested name of the property  to be extracted
	 * @return the property value
	 * @inheritDoc
	 * @see LazyInitializer#getIdentifier()
	 * @see PropertyUtils#getProperty(Object, String)
	 * @see FieldUtils#readField(Object, String, boolean)
	 */
	@Nullable
	@Override
	public Object getPropertyValue(@Nonnull String propertyName) {
		try {
			if (isIdentifierName(propertyName)) {
				return lazyInitializer.getIdentifier();
			} else {
				return PropertyUtils.getProperty(getTarget(), propertyName);
			}
		} catch (Exception e) {
			try {
				return FieldUtils.readField(getTarget(), propertyName, true);
			} catch (IllegalAccessException ignored) {
				throw throwException(propertyName, e);
			}
		}
	}

	/**
	 * @param propertyName possibly indexed and/or nested name of the property to be modified
	 * @param value        value to which this property is to be set
	 * @inheritDoc
	 * @see LazyInitializer#setIdentifier(java.io.Serializable)
	 * @see PropertyUtils#setProperty(Object, String, Object)
	 * @see FieldUtils#writeField(Object, String, Object, boolean)
	 */
	@Override
	public void setPropertyValue(@Nonnull String propertyName, @Nullable Object value) {
		try {
			if (isIdentifierName(propertyName)) {
				lazyInitializer.setIdentifier((Serializable) value);
			} else {
				PropertyUtils.setProperty(getTarget(), propertyName, value);
			}
		} catch (Exception e) {
			try {
				FieldUtils.writeField(getTarget(), propertyName, value, true);
			} catch (IllegalAccessException ignored) {
				throw throwException(propertyName, e);
			}
		}
	}

	@Nullable
	@Override
	public <T> T convertIfNecessary(@Nullable Object value, @Nonnull Class<T> requiredType) throws TypeMismatchException {
		return convertIfNecessary(value, requiredType, (MethodParameter) null);
	}

	@Nullable
	@Override
	public <T> T convertIfNecessary(@Nullable Object value, @Nonnull Class<T> requiredType, @Nullable MethodParameter methodParam) {
		return requiredType.cast(value);
	}

	/**
	 * Creates an {@link InvalidPropertyException} instance indicating that the given property is invalid.
	 *
	 * @param propertyName the offending property
	 * @param e            the root cause
	 */
	@Nonnull
	protected InvalidPropertyException throwException(@Nonnull String propertyName, @Nullable Exception e) {
		return new InvalidPropertyException(lazyInitializer.getPersistentClass(), propertyName, e != null ? e.getMessage() : "", e);
	}

	/**
	 * Returns the underlying persistent object, initializing if necessary
	 *
	 * @return The underlying target entity.
	 * @see LazyInitializer#getImplementation()
	 */
	@Nonnull
	protected Object getTarget() {
		return lazyInitializer.getImplementation();
	}

	/**
	 * Get the {@link ClassMetadata} associated with the given entity class
	 */
	@Nonnull
	protected ClassMetadata getMetadata() {
		if (metadata == null) {
			metadata = lazyInitializer.getSession().getFactory().getClassMetadata(lazyInitializer.getPersistentClass());
		}
		return metadata;
	}

	/**
	 * Checks if the given property is the entities identifier.
	 *
	 * @param propertyName name of the identifier property
	 * @return {@code true} if the given property is the entities identifier, {@code false} otherwise
	 * @see ClassMetadata#getIdentifierPropertyName()
	 */
	protected boolean isIdentifierName(@Nonnull String propertyName) {
		return propertyName.equals(getMetadata().getIdentifierPropertyName());
	}
}
