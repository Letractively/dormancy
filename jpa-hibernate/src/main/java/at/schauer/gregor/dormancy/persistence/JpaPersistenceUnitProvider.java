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
package at.schauer.gregor.dormancy.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.metamodel.EntityType;

/**
 * Provides access to the {@link EntityManagerFactory} and {@link EntityType} of managed JPA entities.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class JpaPersistenceUnitProvider implements PersistenceUnitProvider<EntityManagerFactory, EntityManager, EntityType<?>> {
	protected JpaPersistenceContext persistenceContext;
	protected EntityManagerFactory emf;
	protected EntityManager em;

	@Override
	public EntityManagerFactory getPersistenceUnit() {
		return emf;
	}

	@Override
	public JpaPersistenceContext getPersistenceContextProvider() {
		if (persistenceContext == null) {
			persistenceContext = new JpaPersistenceContext();
			persistenceContext.setEntityManager(em);
		}
		return persistenceContext;
	}

	@Override
	public EntityType getMetadata(Class<?> clazz) {
		try {
			return emf.getMetamodel().entity(clazz);
		} catch (Exception e) {
			return null;
		}
	}

	@PersistenceUnit
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}

	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.em = em;
		getPersistenceContextProvider().setEntityManager(em);
	}
}