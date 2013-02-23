/*
 * Copyright 2012 Gregor Schauer
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
package at.schauer.gregor.dormancy.util;

import at.schauer.gregor.dormancy.access.HibernatePropertyAccessStrategy;
import at.schauer.gregor.dormancy.access.PropertyAccessStrategy;
import org.apache.commons.lang.ArrayUtils;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.internal.AbstractSessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 */
public class DormancyUtils extends AbstractDormancyUtils {
	public DormancyUtils(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Nullable
	@Override
	public ClassMetadata getClassMetadata(@Nullable Class<?> clazz) {
		return clazz != null ? sessionFactory.getClassMetadata(clazz) : null;
	}

	@Nonnull
	@Override
	public String getEntityName(@Nonnull Class<?> clazz) {
		ClassMetadata metadata = getClassMetadata(clazz);
		return metadata != null ? metadata.getEntityName() : clazz.getSimpleName();
	}

	@Nullable
	@Override
	protected String getIdentifierPropertyName(@Nonnull Class<?> clazz) {
		ClassMetadata metadata = getClassMetadata(clazz);
		return metadata != null ? metadata.getIdentifierPropertyName() : null;
	}

	@Override
	@Nullable
	public <T> Serializable getIdentifier(@Nonnull ClassMetadata metadata, @Nonnull T bean) {
		Serializable identifier = metadata.getIdentifier(bean, AbstractSessionImpl.class.cast(getSession()));
		if (identifier == null) {
			identifier = Serializable.class.cast(getPropertyValue(metadata, bean, metadata.getIdentifierPropertyName()));
		}
		return identifier;
	}

	@Override
	@Nullable
	public Object getPropertyValue(@Nullable ClassMetadata metadata, @Nonnull Object bean, @Nonnull String propertyName) {
		return IntrospectorUtils.getValue(bean, propertyName);
	}

	@Override
	public void setPropertyValue(@Nullable ClassMetadata metadata, @Nonnull Object bean, @Nonnull String propertyName, @Nullable Object value) {
		IntrospectorUtils.setValue(bean, propertyName, value);
	}

	/**
	 * @inheritDoc
	 * @see org.hibernate.metadata.ClassMetadata#getMappedClass()
	 */
	@Override
	public Class<?> getMappedClass(@Nonnull ClassMetadata metadata) {
		return metadata.getMappedClass();
	}

	@Override
	public boolean isPersistentCollection(Object obj) {
		return obj instanceof PersistentCollection;
	}

	/**
	 * @inheritDoc
	 * @see org.hibernate.collection.spi.PersistentCollection#wasInitialized()
	 */
	@Override
	public boolean isInitializedPersistentCollection(Object obj) {
		return isPersistentCollection(obj) && PersistentCollection.class.cast(obj).wasInitialized();
	}

	@Nonnull
	@Override
	public Class<?> getPropertyType(@Nonnull Class<?> clazz, @Nonnull String propertyName) {
		ClassMetadata metadata = getClassMetadata(clazz);
		if (metadata != null) {
			try {
				return metadata.getPropertyType(propertyName).getReturnedClass();
			} catch (QueryException e) {
				if (ArrayUtils.contains(metadata.getPropertyNames(), propertyName)) {
					throw e;
				}
			}
		}
		return ReflectionUtils.findField(clazz, propertyName).getType();
	}

	@Nonnull
	@Override
	protected PropertyAccessStrategy createStrategy(@Nonnull Class<?> clazz) {
		return new HibernatePropertyAccessStrategy(clazz);
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

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T find(@Nonnull Class<T> clazz, @Nonnull Serializable id) {
		return (T) getSession().get(clazz, id);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <K> K persist(@Nonnull Object obj) {
		return (K) getSession().save(obj);
	}

	@Nonnull
	@Override
	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}
}
