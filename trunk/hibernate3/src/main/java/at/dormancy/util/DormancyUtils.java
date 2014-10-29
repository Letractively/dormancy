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
import at.dormancy.metadata.resolver.HibernateMetadataResolver;
import at.dormancy.metadata.resolver.MetadataResolver;
import at.dormancy.persistence.HibernatePersistenceUnitProvider;
import org.hibernate.*;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.metadata.ClassMetadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;

import static org.springframework.util.ObjectUtils.identityToString;

/**
 * Utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 */
public class DormancyUtils extends AbstractDormancyUtils
		<SessionFactory, Session, ClassMetadata, HibernatePersistenceUnitProvider> {

	MetadataResolver metadataResolver = new HibernateMetadataResolver(this);
	Exceptions exceptions = new Exceptions();

	@Inject
	public DormancyUtils(@Nonnull HibernatePersistenceUnitProvider persistenceUnitProvider) {
		super(persistenceUnitProvider);
	}

	@Nullable
	@Override
	public ClassMetadata getMetadata(@Nullable Class<?> clazz) {
		return clazz != null && persistenceUnitProvider != null ? persistenceUnitProvider.getMetadata(clazz) : null;
	}

	@Nonnull
	@Override
	public String getEntityName(@Nonnull Class<?> clazz) {
		ClassMetadata metadata = getMetadata(clazz);
		return metadata != null ? metadata.getEntityName() : clazz.getSimpleName();
	}

	@Nullable
	@Override
	public <T> Serializable getIdentifier(@Nonnull ClassMetadata metadata, @Nonnull T bean) {
		return metadata.getIdentifier(bean, EntityMode.POJO);
	}

	@Override
	public boolean isVersioned(@Nonnull ClassMetadata metadata) {
		return metadata.isVersioned();
	}

	@Nullable
	@Override
	public String getVersionPropertyName(@Nonnull ClassMetadata metadata) {
		int index = metadata.getVersionProperty();
		return index >= 0 ? metadata.getPropertyNames()[index] : null;
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
		return (T) getPersistenceContext().get(clazz, id);
	}

	@Override
	public void flush() {
		getPersistenceContext().flush();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <I> I persist(@Nonnull Object obj) {
		return (I) getPersistenceContext().save(obj);
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
	public Session getPersistenceContext() {
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
			throw new PropertyValueException(message, getEntityName(bean.getClass()),
					getMetadata(bean).getIdentifierPropertyName());
		}
	}
}
