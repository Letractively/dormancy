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

import org.hibernate.Session;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.internal.AbstractSessionImpl;
import org.hibernate.metadata.ClassMetadata;

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

	@Override
	@Nullable
	public <T> Serializable getIdentifier(@Nonnull ClassMetadata metadata, @Nonnull T bean, @Nonnull Session session) {
		Serializable identifier = metadata.getIdentifier(bean, AbstractSessionImpl.class.cast(session));
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
}
