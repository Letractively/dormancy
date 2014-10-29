package at.dormancy.metadata.resolver;

import at.dormancy.access.AccessType;
import at.dormancy.persistence.PersistenceUnitProvider;
import at.dormancy.util.AbstractDormancyUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static org.springframework.util.ReflectionUtils.findField;

/**
 * Uses JPA specific annotations for determining how to access properties of JPA entities.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class JpaMetadataResolver extends AnnotationMetadataResolver<EntityType<?>> {
	@SuppressWarnings("unchecked")
	public JpaMetadataResolver(@Nonnull AbstractDormancyUtils<?, ?, EntityType<?>,
			? extends PersistenceUnitProvider<?, ?, EntityType<?>>> utils) {
		super(utils);

		accessAnnotations = new Class[]{javax.persistence.Access.class};
		idAnnotations = new Class[]{javax.persistence.Id.class, javax.persistence.EmbeddedId.class};
	}

	@Override
	protected void detectAccessType(@Nonnull Class<?> entityType, @Nullable EntityType<?> metadata) {
		super.detectAccessType(entityType, metadata);

		// If the entity type does not define a default access type, scan for methods annotated with an ID annotation
		if (getDefaultAccessType() == null && metadata != null) {
			if (metadata.hasSingleIdAttribute()) {
				// If there is a single identifier, determine the access type
				String identifierPropertyName = metadata.getId(metadata.getIdType().getJavaType()).getName();
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
	protected Set<String> findProperties(@Nonnull Class<?> entityType, @Nullable EntityType<?> metadata) {
		return metadata == null ? ImmutableSet.<String>of()
				: ImmutableSet.<String>builder()
				.addAll(findIdentifierProperties(entityType, metadata))
				.addAll(transform(metadata.getAttributes(), new Function<Attribute<?, ?>, String>() {
					@Override
					public String apply(@Nonnull Attribute<?, ?> input) {
						return input.getName();
					}
				})).build();
	}

	@Nonnull
	protected Set<String> findIdentifierProperties(Class<?> entityType, @Nonnull EntityType<?> metadata) {
		return FluentIterable.from(metadata.getAttributes())
				.filter(SingularAttribute.class)
				.filter(new Predicate<SingularAttribute>() {
					@Override
					public boolean apply(@Nonnull SingularAttribute input) {
						return input.isId();
					}
				})
				.transform(new Function<SingularAttribute, String>() {
					@Override
					public String apply(@Nonnull SingularAttribute input) {
						return input.getName();
					}
				}).toSet();
		/*
		if (metadata.getIdentifierPropertyName() != null) {
			return ImmutableSet.of(metadata.getIdentifierPropertyName());
		}
		Type identifierType = metadata.getIdentifierType();
		if (identifierType.isComponentType()) {
			ComponentType componentType = (ComponentType) identifierType;
			return ImmutableSet.copyOf(componentType.getPropertyNames());
		}
		throw new UnsupportedOperationException("Identifier type " + identifierType.getName() + " not supported.");
		*/
	}
}
