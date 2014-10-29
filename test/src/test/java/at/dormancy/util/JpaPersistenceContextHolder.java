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

import at.dormancy.Dormancy;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
public class JpaPersistenceContextHolder implements PersistenceContextHolder<EntityManager> {
	@Inject
	Dormancy<Object, Object, Object> dormancy;
	@PersistenceUnit
	EntityManagerFactory emf;
	EntityManager em;

	@Override
	public EntityManager open() {
		EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		if (em == null) {
			em = getCurrent();
		}
		return em;
	}

	@Override
	public EntityManager getCurrent() {
		if (this.em != null && this.em.isOpen()) {
			return this.em;
		}
		EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		if (em == null) {
			em = emf.createEntityManager();
			em.getTransaction().begin();
		}
		this.em = em;
		return em;
	}

	@Override
	public void close() {
		if (this.em != null) {
			if (this.em.getTransaction().isActive()) {
				this.em.getTransaction().commit();
			}
			this.em.close();
		} else {
			EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
			if (em != null) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().commit();
				}
				em.close();
			}
		}
		this.em = null;
	}

	@Override
	public <T> T get(Class<T> type, Serializable id) {
		return getCurrent().find(type, id);
	}

	@Override
	public Serializable save(Object object) {
		if (getIdentifier(object) == null) {
			getCurrent().persist(object);
//			getCurrent().refresh(object);
		} else {
			getCurrent().merge(object);
		}
		getCurrent().flush();
		return getIdentifier(object);
	}

	private Serializable getIdentifier(Object object) {
		return dormancy.getUtils().getIdentifier(dormancy.getUtils().getMetadata(object.getClass()), object);
	}

	@Override
	public void clear() {
		getCurrent().clear();
	}

	@Override
	public void flush() {
		getCurrent().flush();
	}
}
