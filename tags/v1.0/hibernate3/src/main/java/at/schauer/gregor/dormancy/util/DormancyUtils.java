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

import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.PropertyAccessor;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 */
public class DormancyUtils extends AbstractDormancyUtils {
	protected DormancyUtils() {
	}

	@Nullable
	@Override
	public <T> Serializable getIdentifier(@Nonnull ClassMetadata metadata, @Nullable PropertyAccessor propertyAccessor, @Nonnull T bean, @Nonnull Session session) {
		Serializable identifier = metadata.getIdentifier(bean, EntityMode.POJO);
		if (identifier == null && propertyAccessor != null) {
			identifier = Serializable.class.cast(propertyAccessor.getPropertyValue(metadata.getIdentifierPropertyName()));
		}
		return identifier;
	}

	@Nullable
	@Override
	public Object getPropertyValue(@Nullable ClassMetadata metadata, @Nullable PropertyAccessor propertyAccessor, @Nonnull Object bean, @Nonnull String propertyName) {
		Assert.isTrue(metadata != null || propertyAccessor != null, "ClassMetadata and PropertyAccessor cannot both be null");
		return propertyAccessor != null ? propertyAccessor.getPropertyValue(propertyName) : metadata.getPropertyValue(bean, propertyName, EntityMode.POJO);
	}

	@Override
	public void setPropertyValue(@Nullable ClassMetadata metadata, @Nullable PropertyAccessor propertyAccessor, @Nonnull Object bean, @Nonnull String propertyName, @Nullable Object value) {
		Assert.isTrue(metadata != null || propertyAccessor != null, "ClassMetadata and PropertyAccessor cannot both be null");
		if (propertyAccessor != null) {
			// Use Spring's PropertyAccessor to set the value directly
			propertyAccessor.setPropertyValue(propertyName, value);
		} else {
			// If no PropertyAccessor is available, use Hibernate's ClassMetadata to set the value
			metadata.setPropertyValue(bean, propertyName, value, EntityMode.POJO);
		}
	}

	/**
	 * @inheritDoc
	 * @see ClassMetadata#getMappedClass(org.hibernate.EntityMode)
	 */
	@Override
	public Class getMappedClass(@Nonnull ClassMetadata metadata) {
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
}
