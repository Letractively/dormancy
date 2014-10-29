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
package at.dormancy.util;

import at.dormancy.metadata.ObjectMetadata;
import at.dormancy.metadata.resolver.JpaMetadataResolver;
import at.dormancy.metadata.resolver.MetadataResolver;
import at.dormancy.persistence.JpaPersistenceUnitProvider;
import com.google.common.base.Preconditions;
import org.hibernate.*;
import org.hibernate.collection.spi.PersistentCollection;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static com.google.common.collect.Iterables.getFirst;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.util.ObjectUtils.identityToString;
import static org.springframework.util.ReflectionUtils.*;

/**
 * Utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 */
public class DormancyUtils extends AbstractDormancyUtils
		<EntityManagerFactory, EntityManager, EntityType<?>, JpaPersistenceUnitProvider> {

	MetadataResolver metadataResolver = new JpaMetadataResolver(this);
	Exceptions exceptions = new Exceptions();

	@Inject
	public DormancyUtils(@Nonnull JpaPersistenceUnitProvider persistenceUnitProvider) {
		super(persistenceUnitProvider);
	}

	@Nullable
	@Override
	public EntityType<?> getMetadata(@Nullable Class<?> clazz) {
		return clazz != null && persistenceUnitProvider != null ? persistenceUnitProvider.getMetadata(clazz) : null;
	}

	@Nonnull
	@Override
	public String getEntityName(@Nonnull Class<?> clazz) {
		EntityType<?> metadata = getMetadata(clazz);
		return metadata != null ? metadata.getName() : clazz.getSimpleName();
	}

	@Nullable
	@Override
	public <T> Serializable getIdentifier(@Nonnull EntityType<?> metadata, @Nonnull T bean) {
		if (metadata.hasSingleIdAttribute()) {
			return getValue(bean, metadata.getId(metadata.getIdType().getJavaType()));
		} else {
			Class<?> idClassType = getFirst(metadata.getIdClassAttributes(), null).getJavaMember().getDeclaringClass();
			Serializable idClass = (Serializable) BeanUtils.instantiateClass(idClassType);
			for (SingularAttribute<?, ?> idClassAttribute : metadata.getIdClassAttributes()) {
				Object value = getValue(bean, idClassAttribute);
				setField(idClass, idClassAttribute.getName(), value);
			}
			return idClass;
		}
	}

	@Nullable
	protected <T> Serializable getValue(@Nonnull T bean, @Nonnull SingularAttribute<?, ?> attr) {
		Member member = attr.getJavaMember();
		if (member instanceof Method) {
			return (Serializable) invokeMethod((Method) member, bean);
		} else if (member instanceof Field) {
			if (!member.getDeclaringClass().isAssignableFrom(bean.getClass())) {
				member = findField(bean.getClass(), member.getName());
			}
			makeAccessible((Field) member);
			return (Serializable) getField((Field) member, bean);
		} else {
			// Not possible
			throw new UnsupportedOperationException(String.format("Retrieving %s.%s via %s not possible",
					bean.getClass().getName(), attr.getName(), member.getClass()));
		}
	}

	@Override
	public boolean isVersioned(@Nonnull EntityType<?> metadata) {
		return metadata.hasVersionAttribute();
	}

	@Nullable
	@Override
	public String getVersionPropertyName(@Nonnull EntityType<?> metadata) {
		if (metadata.hasVersionAttribute()) {
			SingularAttribute<?, Long> version = metadata.getVersion(Long.class);
			if (version != null) {
				return version.getName();
			}
		}
		return null;
	}

	@Nonnull
	@Override
	protected ObjectMetadata createObjectMetadata(@Nonnull Class<?> clazz) {
		return metadataResolver.getMetadata(clazz);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T find(@Nonnull Class<T> clazz, @Nonnull Serializable id) {
		return getPersistenceContext().find(clazz, id);
	}

	@Override
	public void flush() {
		getPersistenceContext().flush();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <I> I persist(@Nonnull Object obj) {
		getPersistenceContext().persist(obj);
		return (I) getIdentifier(getMetadata(obj), obj);
	}

	/**
	 * @see Hibernate#isInitialized(Object)
	 */
	@Override
	public boolean isInitialized(@Nullable Object obj) {
		return Hibernate.isInitialized(obj);
	}

	@Override
	public boolean isPersistentCollection(@Nullable Object obj) {
		return obj instanceof PersistentCollection;
	}

	/**
	 * @see PersistentCollection#wasInitialized()
	 */
	@Override
	public boolean isInitializedPersistentCollection(@Nullable Object obj) {
		return obj != null && PersistentCollection.class.cast(obj).wasInitialized();
	}

	@Nonnull
	@Override
	public EntityManager getPersistenceContext() {
		return persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();
	}

	@Nonnull
	@Override
	public Exceptions exceptions() {
		return exceptions;
	}

	public class Exceptions extends AbstractExceptions {
		/**
		 * Throws a {@link TransientObjectException} indicating that the object has no valid identifier.
		 *
		 * @param object the object
		 * @throws TransientObjectException the exception
		 */
		@Nonnull
		@Override
		public TransientObjectException throwNullIdentifierException(@Nonnull Object object) {
			throw new TransientObjectException("The given object has a null identifier: "
					+ getEntityName(DormancyUtils.this.getClass(object)));
		}

		/**
		 * Throws a {@link TransientObjectException} indicating that the object must be saved manually before
		 * continuing.
		 *
		 * @throws TransientObjectException the exception
		 */
		@Nonnull
		@Override
		public TransientObjectException throwUnsavedTransientInstanceException(@Nonnull Object object) {
			throw new TransientObjectException(
					"object references an unsaved transient instance - save the transient instance before flushing: "
							+ getEntityName(DormancyUtils.this.getClass(object))
			);
		}

		/**
		 * Throws a {@link LazyInitializationException} when an uninitialized property was accessed.
		 *
		 * @throws LazyInitializationException the exception
		 */
		@Nonnull
		@Override
		public LazyInitializationException throwLazyInitializationException(@Nonnull Object object) {
			return new LazyInitializationException("Object is is uninitialized: " + identityToString(object));
		}

		/**
		 * Throws a {@link PropertyValueException} when a lazy property holds a value while {@code null} was expected.
		 *
		 * @throws PropertyValueException the exception
		 */
		@Nonnull
		@Override
		public PropertyValueException throwLazyPropertyNotNullException(@Nonnull Object trValue, @Nonnull Object dbObj,
																  @Nonnull String propertyName) {
			throw new PropertyValueException(String.format(
					"Property is loaded lazily. Therefore it must be null but was '%s'", trValue),
					getEntityName(DormancyUtils.this.getClass(dbObj)), propertyName);
		}

		/**
		 * Throws a {@link StaleObjectStateException} when an optimistic locking conflict occurs.
		 *
		 * @throws StaleObjectStateException the exception
		 */
		@Nonnull
		@Override
		public StaleObjectStateException throwOptimisticLockException(@Nonnull Object dbValue,
															 @Nonnull Serializable identifier) {
			throw new StaleObjectStateException(getEntityName(DormancyUtils.this.getClass(dbValue)), identifier);
		}

		/**
		 * Throws an {@link ObjectNotFoundException} when an entity reference is accessed but the entity does not
		 * exist.
		 *
		 * @throws ObjectNotFoundException the exception
		 */
		@Nonnull
		@Override
		public ObjectNotFoundException throwEntityNotFoundException(@Nonnull Serializable identifier, @Nonnull Object trObj) {
			throw new ObjectNotFoundException(identifier, DormancyUtils.this.getClass(trObj).getSimpleName());
		}

		/**
		 * Throws a {@link PropertyValueException} when a property is not accessible or holds an unexpected value.
		 *
		 * @throws PropertyValueException the exception
		 */
		@Nonnull
		@Override
		public PropertyValueException throwPropertyValueException(@Nonnull String message, @Nonnull Object bean) {
			EntityType<?> metadata = Preconditions.checkNotNull(getMetadata(bean));
			String identifierPropertyName = metadata.getId(metadata.getIdType().getJavaType()).getName();
			throw new PropertyValueException(message, getEntityName(bean.getClass()), identifierPropertyName);
		}
	}
}
