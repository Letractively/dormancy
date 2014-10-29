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
package at.dormancy.metadata;

import at.dormancy.access.AccessType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds information about properties of a certain class.
 * <p/>
 * Note that the term <i>property</i> refers to field and/or Java bean properties.<br/>
 * In other words, a property
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class ObjectMetadata {
	protected final Class<?> type;
	protected final ImmutableMap<String, AccessType> propertyAccessTypeMap;

	public ObjectMetadata(@Nonnull Class<?> type) {
		this(type, ImmutableMap.<String, AccessType>of());
	}

	public ObjectMetadata(@Nonnull Class<?> type, @Nonnull Map<String, AccessType> propertyAccessTypeMap) {
		this.type = type;
		this.propertyAccessTypeMap = ImmutableMap.copyOf(propertyAccessTypeMap);
	}

	/**
	 * Returns a {@code ObjectMetadata} instance with the given named properties.
	 * <p/>
	 * Since {@code ObjectMetadata} is immutable, this instance may be returned if possible.
	 *
	 * @param accessType how the properties have to be accessed
	 * @param properties the name of the properties to add
	 * @return the metadata with the given properties
	 */
	@Nonnull
	public ObjectMetadata withProperties(@Nullable AccessType accessType, @Nullable String... properties) {
		properties = ArrayUtils.nullToEmpty(properties);
		LinkedHashMap<String, AccessType> map = new LinkedHashMap<String, AccessType>(propertyAccessTypeMap);
		boolean modified = false;
		for (String property : properties) {
			if (map.put(property, accessType) != accessType) {
				modified = true;
			}
		}
		return modified ? new ObjectMetadata(type, map) : this;
	}

	/**
	 * Returns a {@code ObjectMetadata} instance without the given named property.
	 * <p/>
	 * Since {@code ObjectMetadata} is immutable, this instance may be returned if possible.
	 *
	 * @param property the name of the property to remove
	 * @return the metadata without the property
	 */
	@Nonnull
	public ObjectMetadata withoutProperty(@Nonnull String property) {
		if (!propertyAccessTypeMap.containsKey(property)) {
			return this;
		}
		LinkedHashMap<String, AccessType> map = new LinkedHashMap<String, AccessType>(propertyAccessTypeMap);
		map.remove(property);
		return new ObjectMetadata(type, map);
	}

	/**
	 * Returns the names of the properties of this type.
	 *
	 * @return the property names
	 * @see #getType()
	 */
	@Nonnull
	public ImmutableSet<String> getProperties() {
		return propertyAccessTypeMap.keySet();
	}

	/**
	 * Checks whether there exists a property with the given name.
	 *
	 * @param name the name of the property
	 * @return {@code true} if it exists, {@code false} otherwise
	 */
	public boolean isProperty(@Nullable String name) {
		return propertyAccessTypeMap.containsKey(name);
	}

	/**
	 * Returns the {@link AccessType} for the named property.
	 *
	 * @param name the name of the property
	 * @return the access type
	 */
	@Nonnull
	public AccessType getAccessType(String name) {
		AccessType accessType = propertyAccessTypeMap.get(name);
		Preconditions.checkArgument(accessType != null, "'%s' is not a valid property of %s", name, type);
		return accessType;
	}

	@Nonnull
	public Class<?> getType() {
		return type;
	}
}
