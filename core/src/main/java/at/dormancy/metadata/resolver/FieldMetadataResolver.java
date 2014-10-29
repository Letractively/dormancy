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
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Maps.toMap;

/**
 * Creates {@link ObjectMetadata} information based on the fields of a class.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class FieldMetadataResolver implements MetadataResolver {
	private static final Logger logger = Logger.getLogger(FieldMetadataResolver.class);

	private final Function<Field, String> NAME_FUNCTION = new Function<Field, String>() {
		@Nonnull
		@Override
		public String apply(@Nullable Field input) {
			return input != null ? input.getName() : "";
		}
	};
	private final Function<String, AccessType> FIELD_ACCESS_TYPE_FUNCTION = new Function<String, AccessType>() {
		@Nonnull
		@Override
		public AccessType apply(@Nullable String input) {
			return AccessType.FIELD;
		}
	};

	@Nonnull
	@Override
	public ObjectMetadata getMetadata(@Nonnull Class<?> clazz) {
		List<Field> fields = getAllFieldsList(clazz);
		logger.info(String.format("Type %s has the following properties: %s",
				clazz.getName(), Joiner.on(", ").join(transform(fields, NAME_FUNCTION))));
		return new ObjectMetadata(clazz, toMap(transform(fields, NAME_FUNCTION), FIELD_ACCESS_TYPE_FUNCTION));
	}

	/**
	 * Gets all fields of the given class and its parents (if any).
	 *
	 * @param clazz the class to get the fields for
	 * @return an array of fields
	 */
	@Nonnull
	protected List<Field> getAllFieldsList(@Nonnull Class<?> clazz) {
		List<Field> allFields = new ArrayList<Field>();
		Class<?> currentClass = clazz;
		while (currentClass != null) {
			final Field[] declaredFields = currentClass.getDeclaredFields();
			Collections.addAll(allFields, declaredFields);
			currentClass = currentClass.getSuperclass();
		}
		return allFields;
	}
}
