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
import at.dormancy.persistence.PersistenceUnitProvider;
import at.dormancy.util.AbstractDormancyUtils;
import com.google.common.collect.ImmutableSet;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import static org.springframework.util.ReflectionUtils.findField;

/**
 * Uses Hibernate 3 specific annotations for determining how to access properties of Hibernate entities.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class HibernateMetadataResolver extends AnnotationMetadataResolver<ClassMetadata> {
	@Inject
	@SuppressWarnings("unchecked")
	public HibernateMetadataResolver(@Nonnull AbstractDormancyUtils<?, ?, ClassMetadata,
			? extends PersistenceUnitProvider<?, ?, ClassMetadata>> utils) {
		super(utils);

		accessAnnotations = new Class[]{org.hibernate.annotations.AccessType.class};
		idAnnotations = new Class[]{javax.persistence.Id.class, javax.persistence.EmbeddedId.class};
	}

	@Override
	protected void detectAccessType(@Nonnull Class<?> entityType, @Nullable ClassMetadata metadata) {
		super.detectAccessType(entityType, metadata);

		// If the entity type does not define a default access type, scan for methods annotated with an ID annotation
		if (getDefaultAccessType() == null && metadata != null) {
			String identifierPropertyName = metadata.getIdentifierPropertyName();
			if (identifierPropertyName != null) {
				// If there is a single identifier, determine the access type
				Field field = findField(entityType, identifierPropertyName);
				Annotation idAnnotation = getAnnotation(field, getIdAnnotations());
				setDefaultAccessType(idAnnotation != null ? AccessType.FIELD : AccessType.PROPERTY);
			} else {
				// If there are multiple identifiers, check each of them instead
				for (String ids : findIdentifierProperties(entityType, metadata)) {
					Annotation idAnnotation = getAnnotation(findField(entityType, ids), getIdAnnotations());
					if (idAnnotation != null) {
						setDefaultAccessType(AccessType.FIELD);
						break;
					}
				}
				if (getDefaultAccessType() == null) {
					setDefaultAccessType(AccessType.PROPERTY);
				}
			}
		}
	}

	@Nonnull
	@Override
	protected Set<String> findProperties(@Nonnull Class<?> entityType, @Nullable ClassMetadata metadata) {
		return metadata == null ? ImmutableSet.<String>of()
				: ImmutableSet.<String>builder()
				.addAll(findIdentifierProperties(entityType, metadata))
				.add(metadata.getPropertyNames())
				.build();
	}

	@Nonnull
	protected Set<String> findIdentifierProperties(@Nonnull Class<?> entityType, @Nonnull ClassMetadata metadata) {
		if (metadata.getIdentifierPropertyName() != null) {
			return ImmutableSet.of(metadata.getIdentifierPropertyName());
		}
		Type identifierType = metadata.getIdentifierType();
		if (identifierType.isComponentType()) {
			ComponentType componentType = (ComponentType) identifierType;
			return ImmutableSet.copyOf(componentType.getPropertyNames());
		}
		throw new UnsupportedOperationException("Identifier type " + identifierType.getName() + " not supported.");
	}
}
