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
import org.hibernate.EntityMode;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.PersistentCollection;
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

	@Nullable
	@Override
	public <T> Serializable getIdentifier(@Nonnull ClassMetadata metadata, @Nonnull T bean) {
		Serializable identifier = metadata.getIdentifier(bean, EntityMode.POJO);
		if (identifier == null) {
			identifier = Serializable.class.cast(getPropertyValue(metadata, bean, metadata.getIdentifierPropertyName()));
		}
		return identifier;
	}

	@Nullable
	@Override
	public Object getPropertyValue(@Nullable ClassMetadata metadata, @Nonnull Object bean, @Nonnull String propertyName) {
		IntrospectorUtils.getDescriptorMap(getClass(bean));
		return IntrospectorUtils.getValue(bean, propertyName);
	}

	@Override
	public void setPropertyValue(@Nullable ClassMetadata metadata, @Nonnull Object bean, @Nonnull String propertyName, @Nullable Object value) {
		IntrospectorUtils.getDescriptorMap(getClass(bean));
		IntrospectorUtils.setValue(bean, propertyName, value);
	}

	/**
	 * @inheritDoc
	 * @see ClassMetadata#getMappedClass(org.hibernate.EntityMode)
	 */
	@Override
	public Class<?> getMappedClass(@Nonnull ClassMetadata metadata) {
		return metadata.getMappedClass(EntityMode.POJO);
	}

	@Override
	public boolean isPersistentCollection(Object obj) {
		return obj instanceof PersistentCollection;
	}

	/**
	 * @inheritDoc
	 * @see org.hibernate.collection.PersistentCollection#wasInitialized()
	 */
	@Override
	public boolean isInitializedPersistentCollection(Object obj) {
		return isPersistentCollection(obj) && PersistentCollection.class.cast(obj).wasInitialized();
	}

	@Nonnull
	@Override
	public String getEntityName(@Nonnull Class<?> clazz) {
		ClassMetadata metadata = getClassMetadata(clazz);
		return metadata != null ? metadata.getEntityName() : clazz.getSimpleName();
	}

	@Override
	protected String getIdentifierPropertyName(@Nonnull Class<?> clazz) {
		ClassMetadata metadata = getClassMetadata(clazz);
		return metadata != null ? metadata.getIdentifierPropertyName() : null;
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
	public <K> K persist(@Nonnull Object obj) {
		return (K) getSession().save(obj);
	}

	@Nonnull
	@Override
	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}
}
