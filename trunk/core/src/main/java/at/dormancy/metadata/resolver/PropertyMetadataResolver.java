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
package at.dormancy.metadata.resolver;

import at.dormancy.access.AccessType;
import at.dormancy.metadata.ObjectMetadata;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import static at.dormancy.access.AccessType.FIELD;
import static at.dormancy.access.AccessType.PROPERTY;
import static java.beans.Introspector.getBeanInfo;

/**
 * Creates {@link ObjectMetadata} information based on the Java bean properties of a class.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class PropertyMetadataResolver implements MetadataResolver {
	private static final Logger logger = Logger.getLogger(FieldMetadataResolver.class);

	@Nonnull
	@Override
	public ObjectMetadata getMetadata(@Nonnull Class<?> clazz) {
		Map<String, AccessType> propertyAccessTypeMap = new HashMap<String, AccessType>();
		try {
			for (PropertyDescriptor descriptor : getBeanInfo(clazz, Object.class).getPropertyDescriptors()) {
				propertyAccessTypeMap.put(descriptor.getName(),
						descriptor.getReadMethod() != null && descriptor.getWriteMethod() != null ? PROPERTY : FIELD);
			}
		} catch (IntrospectionException e) {
			throw Throwables.propagate(e);
		}

		logger.info(String.format("Type %s has the following properties: %s",
				clazz.getName(), Joiner.on(", ").join(propertyAccessTypeMap.keySet())));
		return new ObjectMetadata(clazz, propertyAccessTypeMap);
	}
}
