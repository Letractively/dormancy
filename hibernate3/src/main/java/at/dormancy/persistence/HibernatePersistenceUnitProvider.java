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
package at.dormancy.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Provides access to the {@link SessionFactory} and {@link ClassMetadata} of managed Hibernate entities.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class HibernatePersistenceUnitProvider implements
		PersistenceUnitProvider<SessionFactory, Session, ClassMetadata> {
	protected HibernatePersistenceContextProvider persistenceContextProvider;

	@Inject
	public HibernatePersistenceUnitProvider(@Nonnull SessionFactory sessionFactory) {
		this.persistenceContextProvider = new HibernatePersistenceContextProvider(sessionFactory);
	}

	@Nonnull
	@Override
	public SessionFactory getPersistenceUnit() {
		return getPersistenceContextProvider().sessionFactory;
	}

	@Nonnull
	@Override
	public HibernatePersistenceContextProvider getPersistenceContextProvider() {
		return persistenceContextProvider;
	}

	@Nullable
	@Override
	public ClassMetadata getMetadata(@Nonnull Class<?> clazz) {
		return getPersistenceUnit().getClassMetadata(clazz);
	}
}
