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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
public class HibernatePersistenceContextHolder implements PersistenceContextHolder<Session> {
	@Inject
	SessionFactory sessionFactory;
	Session session;

	@Override
	public Session open() {
		this.session = sessionFactory.openSession();
		Transaction transaction = session.getTransaction();
		if (!transaction.isActive()) {
			transaction.begin();
		}
		return session;
	}

	@Override
	public Session getCurrent() {
		if (this.session != null) {
			return this.session;
		}
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction = session.getTransaction();
		if (!transaction.isActive()) {
			transaction.begin();
		}
		return session;
	}

	@Override
	public void close() {
		Transaction transaction = getCurrent().getTransaction();
		if (transaction.isActive()) {
			transaction.commit();
		}
		getCurrent().close();
		this.session = null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> type, Serializable id) {
		return (T) getCurrent().get(type, id);
	}

	@Override
	public Serializable save(Object object) {
		return getCurrent().save(object);
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
