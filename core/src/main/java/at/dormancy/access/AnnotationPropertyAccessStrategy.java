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

import at.dormancy.persister.filter.NonTransientPropertyFilter;
import org.apache.commons.lang.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static at.dormancy.access.AbstractPropertyAccessStrategy.AccessType.*;
import static org.apache.commons.lang.StringUtils.upperCase;
import static org.springframework.beans.BeanUtils.findPropertyForMethod;
import static org.springframework.core.annotation.AnnotationUtils.getValue;
import static org.springframework.util.ReflectionUtils.*;

/**
 * Determines how to access entity properties based on annotations located on them.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public abstract class AnnotationPropertyAccessStrategy extends AbstractPropertyAccessStrategy {
	@SuppressWarnings("unchecked")
	protected static Class<? extends Annotation>[] accessAnnotations = ArrayUtils.EMPTY_CLASS_ARRAY;
	@SuppressWarnings("unchecked")
	protected static Class<? extends Annotation>[] idAnnotations = ArrayUtils.EMPTY_CLASS_ARRAY;

	/**
	 * Creates a new strategy instance.
	 *
	 * @param entityType the type of the entity
	 * @see #initialize(Class)
	 */
	protected AnnotationPropertyAccessStrategy(@Nonnull Class<?> entityType) {
		initialize(entityType);
	}

	/**
	 * Initializes this strategy by scanning the desired type for annotations.
	 *
	 * @param entityType the entity type
	 */
	protected void initialize(@Nonnull Class<?> entityType) {
		// Retrieve the default access type for the entity type by looking up access annotations on type level
		Annotation accessType = getAnnotation(entityType, getAccessAnnotations());
		AccessType entityAccessType = accessType != null ? valueOf(upperCase(String.valueOf(getValue(accessType)))) : null;

		// If the entity type does not define a default access type, scan for methods annotated with an ID annotation
		if (entityAccessType == null) {
			Method[] methods = getAllDeclaredMethods(entityType);
			for (Method method : methods) {
				// If a method is annotated with a ID annotation, property access is used by default
				if (getAnnotation(method, getIdAnnotations()) != null) {
					entityAccessType = PROPERTY;
					break;
				}
			}
		}

		/**
		 * If the determination of the default access type was not successful so far i.e., because there is a field
		 * annotated with the ID annotation, use field access by default
		 */
		if (entityAccessType == null) {
			entityAccessType = FIELD;
		}
		setDefaultAccessType(entityAccessType);
		final AccessType finalEntityAccessType = entityAccessType;

		// Determine whether some properties must be accessed differently
		if (getDefaultAccessType() == FIELD) {
			// If field access is used by default, scan for fields annotated with an access annotation
			doWithFields(entityType, new FieldCallback() {
				@Override
				public void doWith(@Nonnull Field field) {
					// Retrieve the access annotation from the field (if possible)
					Annotation annotation = getAnnotation(field, getAccessAnnotations());

					// If a annotation was found, map the access type to the appropriate mode. Otherwise, use the default mode for the type.
					AccessType accessType = annotation != null ? valueOf(upperCase(String.valueOf(getValue(annotation)))) : finalEntityAccessType;
					propertyAccessTypeMap.put(field.getName(), accessType);
				}
			}, NonTransientPropertyFilter.getInstance());
		} else {
			// If property access is used by default, scan for methods annotated with an access annotation
			doWithMethods(entityType, new MethodCallback() {
				@Override
				public void doWith(@Nonnull Method method) {
					// Check whether the given method is an ordinary getter method
					PropertyDescriptor descriptor = findPropertyForMethod(method);
					if (descriptor == null) {
						return;
					}
					// Retrieve the access annotation from the method (if possible)
					Annotation annotation = getAnnotation(method, getAccessAnnotations());

					// If a annotation was found, map the access type to the appropriate mode. Otherwise, use the default mode for the type.
					AccessType accessType = annotation != null ? valueOf(upperCase(String.valueOf(getValue(annotation)))) : finalEntityAccessType;
					propertyAccessTypeMap.put(descriptor.getName(), accessType);
				}
			}, NonTransientPropertyFilter.getInstance());
		}
	}

	/**
	 * Returns the annotation types used for specifying or overriding the access type.
	 *
	 * @return the access annotation type
	 */
	@Nonnull
	public static Class<? extends Annotation>[] getAccessAnnotations() {
		return accessAnnotations;
	}

	/**
	 * Returns the annotation types used for specifying identifier properties.
	 *
	 * @return the identifier annotation types
	 */
	@Nonnull
	public static Class<? extends Annotation>[] getIdAnnotations() {
		return idAnnotations;
	}

	/**
	 * Finds and returns the first annotation of the desired types present on the given element.
	 *
	 * @param element         the annotated element to check
	 * @param annotationTypes the types of annotations to find
	 * @return the first annotation of the desired types or {@code null} if the search was not successful
	 */
	@Nullable
	protected Annotation getAnnotation(@Nonnull AnnotatedElement element, @Nonnull Class<? extends Annotation>... annotationTypes) {
		for (Class<? extends Annotation> annotationType : annotationTypes) {
			Annotation annotation = element.getAnnotation(annotationType);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}
}
