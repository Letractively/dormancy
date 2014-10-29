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
import at.dormancy.persistence.PersistenceUnitProvider;
import at.dormancy.util.AbstractDormancyUtils;
import com.google.common.base.Joiner;
import org.apache.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static at.dormancy.access.AccessType.*;
import static org.apache.commons.lang.StringUtils.upperCase;
import static org.springframework.beans.BeanUtils.findPropertyForMethod;
import static org.springframework.core.annotation.AnnotationUtils.getValue;
import static org.springframework.util.ReflectionUtils.*;

/**
 * Determines how to access entity properties based on annotations located on them.
 * <p/>
 *
 * @param <PMD> the type of the persistence metadata
 * @author Gregor Schauer
 * @since 2.0.0
 */
public abstract class AnnotationMetadataResolver<PMD> implements MetadataResolver {
	private static final Logger logger = Logger.getLogger(FieldMetadataResolver.class);

	@SuppressWarnings("unchecked")
	protected Class<? extends Annotation>[] accessAnnotations = new Class[0];
	@SuppressWarnings("unchecked")
	protected Class<? extends Annotation>[] idAnnotations = new Class[0];

	protected AbstractDormancyUtils<?, ?, PMD, ? extends PersistenceUnitProvider<?, ?, PMD>> utils;
	protected AccessType defaultAccessType;

	protected AnnotationMetadataResolver(@Nonnull AbstractDormancyUtils<?, ?, PMD,
			? extends PersistenceUnitProvider<?, ?, PMD>> utils) {
		this.utils = utils;
	}

	/**
	 * Resolves the metadata for the given entity type by scanning it for annotations.
	 *
	 * @param entityType the entity type
	 * @return the metadata of the object
	 */
	@Nonnull
	@Override
	public ObjectMetadata getMetadata(@Nonnull Class<?> entityType) {
		final Map<String, AccessType> propertyAccessTypeMap = new LinkedHashMap<String, AccessType>();
		PMD metadata = utils.getMetadata(entityType);

		detectAccessType(entityType, metadata);
		final Set<String> propertyNames = findProperties(entityType, metadata);

		/**
		 * If the determination of the default access type was not successful so far i.e., because there is a field
		 * annotated with the ID annotation, use field access by default
		 */
		if (getDefaultAccessType() == null) {
			Method[] methods = getAllDeclaredMethods(entityType);
			AccessType entityAccessType = FIELD;
			for (Method method : methods) {
				// If a method is annotated with a ID annotation, property access is used by default
				if (getAnnotation(method, getIdAnnotations()) != null) {
					entityAccessType = PROPERTY;
					break;
				}
			}
			setDefaultAccessType(entityAccessType);
		}

		// Determine whether some properties must be accessed differently
		if (getDefaultAccessType() == FIELD) {
			// If field access is used by default, scan for fields annotated with an access annotation
			doWithFields(entityType, new FieldCallback() {
				@Override
				public void doWith(@Nonnull Field field) {
					// Retrieve the access annotation from the field (if possible)
					Annotation annotation = getAnnotation(field, getAccessAnnotations());

					// If a annotation was found, map the access type to the appropriate mode.
					// Otherwise, use the default mode for the type.
					AccessType accessType = annotation == null ? getDefaultAccessType()
							: valueOf(upperCase(String.valueOf(getValue(annotation))));
					propertyAccessTypeMap.put(field.getName(), accessType);
				}
			}, new ReflectionUtils.FieldFilter() {
				@Override
				public boolean matches(@Nonnull Field field) {
					return COPYABLE_FIELDS.matches(field)
							&& (propertyNames.isEmpty() || propertyNames.contains(field.getName()));
				}
			});
		} else {
			// If property access is used by default, scan for methods annotated with an access annotation
			doWithMethods(entityType, new MethodCallback() {
				@Override
				public void doWith(@Nonnull Method method) {
					// Check whether the given method is an ordinary getter method
					PropertyDescriptor descriptor = findPropertyForMethod(method);

					// Retrieve the access annotation from the method (if possible)
					Annotation annotation = getAnnotation(method, getAccessAnnotations());

					// If a annotation was found, map the access type to the appropriate mode.
					// Otherwise, use the default mode for the type.
					AccessType accessType = annotation == null ? getDefaultAccessType()
							: valueOf(upperCase(String.valueOf(getValue(annotation))));
					propertyAccessTypeMap.put(descriptor.getName(), accessType);
				}
			}, new ReflectionUtils.MethodFilter() {
				@Override
				public boolean matches(@Nonnull Method method) {
					PropertyDescriptor descriptor = findPropertyForMethod(method);
					return descriptor != null && !ReflectionUtils.isObjectMethod(method)
							&& (propertyNames.isEmpty() || propertyNames.contains(descriptor.getName()));
				}
			});
		}

		logger.info(String.format("Type %s has the following properties: %s",
				entityType.getName(), Joiner.on(", ").join(propertyAccessTypeMap.keySet())));
		return new ObjectMetadata(entityType, propertyAccessTypeMap);
	}

	protected void detectAccessType(@Nonnull Class<?> entityType, @Nullable PMD metadata) {
		// Retrieve the default access type for the entity type by looking up access annotations on type level
		Annotation accessType = getAnnotation(entityType, getAccessAnnotations());
		AccessType entityAccessType = accessType == null ? null
				: valueOf(upperCase(String.valueOf(getValue(accessType))));
		setDefaultAccessType(entityAccessType);
	}

	@Nonnull
	protected abstract Set<String> findProperties(@Nonnull Class<?> entityType, @Nullable PMD metadata);

	/**
	 * Returns the annotation types used for specifying or overriding the access type.
	 *
	 * @return the access annotation type
	 */
	@Nonnull
	public Class<? extends Annotation>[] getAccessAnnotations() {
		return accessAnnotations;
	}

	/**
	 * Returns the annotation types used for specifying identifier properties.
	 *
	 * @return the identifier annotation types
	 */
	@Nonnull
	public Class<? extends Annotation>[] getIdAnnotations() {
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
	protected Annotation getAnnotation(@Nonnull AnnotatedElement element,
									   @Nonnull Class<? extends Annotation>... annotationTypes) {
		for (Class<? extends Annotation> annotationType : annotationTypes) {
			Annotation annotation = element.getAnnotation(annotationType);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}


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
	public void setDefaultAccessType(@Nullable AccessType defaultAccessType) {
		this.defaultAccessType = defaultAccessType;
	}
}
