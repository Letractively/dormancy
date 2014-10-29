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
import org.eclipse.persistence.indirection.IndirectCollection;
import org.eclipse.persistence.indirection.ValueHolder;
import org.eclipse.persistence.internal.jpa.metamodel.EntityTypeImpl;
import org.eclipse.persistence.internal.jpa.metamodel.ManagedTypeImpl;
import org.eclipse.persistence.internal.jpa.metamodel.SingularAttributeImpl;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
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
			SingularAttributeImpl attribute = (SingularAttributeImpl) getFirst(metadata.getIdClassAttributes(), null);
			assert attribute != null;
			ManagedTypeImpl managedType = attribute.getManagedTypeImpl();
			Class<?> idClassType = ((EntityTypeImpl) managedType).getIdType().getJavaType();
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
	 * @see ValueHolder#isInstantiated()
	 */
	@Override
	public boolean isInitialized(@Nullable Object obj) {
		return obj instanceof ValueHolder ? ((ValueHolder) obj).isInstantiated()
				: !(obj instanceof IndirectCollection) || ((IndirectCollection) obj).isInstantiated();
	}

	@Override
	public boolean isPersistentCollection(@Nullable Object obj) {
		return obj instanceof IndirectCollection;
	}

	/**
	 * @see IndirectCollection#isInstantiated()
	 */
	@Override
	public boolean isInitializedPersistentCollection(@Nullable Object obj) {
		return obj != null && IndirectCollection.class.cast(obj).isInstantiated();
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
		 * Throws a {@link RuntimeException} indicating that the object has no valid identifier.
		 *
		 * @param object the object
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		@Override
		public RuntimeException throwNullIdentifierException(@Nonnull Object object) {
			throw new RuntimeException("The given object has a null identifier: "
					+ getEntityName(DormancyUtils.this.getClass(object)));
		}

		/**
		 * Throws a {@link RuntimeException} indicating that the object must be saved manually before continuing.
		 *
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		@Override
		public RuntimeException throwUnsavedTransientInstanceException(@Nonnull Object object) {
			throw new RuntimeException(
					"object references an unsaved transient instance - save the transient instance before flushing: "
							+ getEntityName(DormancyUtils.this.getClass(object))
			);
		}

		/**
		 * Throws a {@link RuntimeException} when an uninitialized property was accessed.
		 *
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		@Override
		public RuntimeException throwLazyInitializationException(@Nonnull Object object) {
			return new RuntimeException("Object is is uninitialized: " + identityToString(object));
		}

		/**
		 * Throws a {@link RuntimeException} when a lazy property holds a value while {@code null} was expected.
		 *
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		@Override
		public RuntimeException throwLazyPropertyNotNullException(@Nonnull Object trValue, @Nonnull Object dbObj,
																  @Nonnull String propertyName) {
			throw new RuntimeException(String.format(
					"Property is loaded lazily. Therefore it must be null but was %s: %s.%s",
					trValue, getEntityName(DormancyUtils.this.getClass(dbObj)), propertyName));
		}

		/**
		 * Throws a {@link OptimisticLockException} when an optimistic locking conflict occurs.
		 *
		 * @throws OptimisticLockException the exception
		 */
		@Nonnull
		@Override
		public OptimisticLockException throwOptimisticLockException(@Nonnull Object dbValue,
																	@Nonnull Serializable identifier) {
			throw new OptimisticLockException(String.format("[%s#%s]",
					getEntityName(DormancyUtils.this.getClass(dbValue)), identifier));
		}

		/**
		 * Throws an {@link EntityNotFoundException} when an entity reference is accessed but the entity does not
		 * exist.
		 *
		 * @throws EntityNotFoundException the exception
		 */
		@Nonnull
		@Override
		public EntityNotFoundException throwEntityNotFoundException(@Nonnull Serializable identifier,
																	@Nonnull Object trObj) {
			throw new EntityNotFoundException(String.format("[%s#%s]",
					DormancyUtils.this.getClass(trObj).getSimpleName(), identifier));
		}

		/**
		 * Throws a {@link RuntimeException} when a property is not accessible or holds an unexpected value.
		 *
		 * @throws RuntimeException the exception
		 */
		@Nonnull
		@Override
		public RuntimeException throwPropertyValueException(@Nonnull String message, @Nonnull Object bean) {
			EntityType<?> metadata = Preconditions.checkNotNull(getMetadata(bean));
			String identifierPropertyName = metadata.getId(metadata.getIdType().getJavaType()).getName();
			throw new RuntimeException(String.format("%s: %s.%s",
					message, getEntityName(bean.getClass()), identifierPropertyName));
		}
	}
}
