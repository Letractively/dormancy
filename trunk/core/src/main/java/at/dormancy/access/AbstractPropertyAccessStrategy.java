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

import org.apache.commons.collections.MapUtils;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Determines how to access entity properties based on various criteria defined by the persistence provider.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public abstract class AbstractPropertyAccessStrategy {
	protected Map<String, AccessType> propertyAccessTypeMap = new HashMap<String, AccessType>();

	/**
	 * The access type.
	 */
	public enum AccessType {
		/** Property-based access is used. */
		PROPERTY,
		/** Field-based access is used. */
		FIELD
	}

	protected AccessType defaultAccessType;

	/**
	 * Returns the default access mode for the entity type.
	 *
	 * @return the access mode
	 */
	@Nullable
	public AccessType getDefaultAccessType() {
		return defaultAccessType;
	}

	/**
	 * Sets the default access mode for the entity type.
	 *
	 * @param defaultAccessType the mode to set
	 */
	protected void setDefaultAccessType(@Nullable AccessType defaultAccessType) {
		this.defaultAccessType = defaultAccessType;
	}

	/**
	 * Returns the {@link at.dormancy.access.AbstractPropertyAccessStrategy.AccessType} for the named property.
	 *
	 * @param propertyName the name of the property
	 * @return the access mode to use
	 */
	@Nonnull
	public AccessType getAccessType(@Nonnull String propertyName) {
		AccessType accessType = (AccessType) MapUtils.getObject(propertyAccessTypeMap, propertyName, null);
		Assert.notNull(accessType, "Cannot find property named '" + propertyName + "'");
		return accessType;
	}
}
