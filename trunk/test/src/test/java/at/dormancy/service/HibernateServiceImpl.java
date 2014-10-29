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
package at.dormancy.service;

import at.dormancy.Dormancy;
import at.dormancy.container.Team;
import at.dormancy.entity.Application;
import at.dormancy.entity.Employee;
import at.dormancy.util.PersistenceProviderUtils;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gregor Schauer
 */
@Transactional
public class HibernateServiceImpl implements GenericService {
	@Inject
	SessionFactory sessionFactory;
	@Inject
	Dormancy dormancy;

	@Override
	public void doNothing() {
	}

	@Override
	public Serializable save(Serializable obj) {
		sessionFactory.getCurrentSession().saveOrUpdate(obj);
		sessionFactory.getCurrentSession().flush();
		return sessionFactory.getCurrentSession().getIdentifier(obj);
	}

	@Override
	public Application loadApp(Long id) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Application.class);
		criteria.setFetchMode("employees", FetchMode.JOIN);
		criteria.add(Restrictions.eq("id", id));
		return (Application) criteria.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T get(Class<T> type, Long id) {
		return (T) sessionFactory.getCurrentSession().get(type, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T load(Class<T> type, Long id) {
		return (T) sessionFactory.getCurrentSession().load(type, id);
	}

	@Override
	public Team next(Team team) {
		Employee employee = get(Employee.class, team.getEmployees().iterator().next().getId() + 1);
		team.setEmployees(new ArrayList<Employee>(Collections.singletonList(employee)));
		return team;
	}

	@Override
	public Team prev(Team team) {
		Employee employee = get(Employee.class, team.getEmployees().iterator().next().getId() - 1);
		team.setEmployees(new ArrayList<Employee>(Collections.singletonList(employee)));
		return team;
	}

	@Override
	public Team pass(Team team) {
		Employee employee = get(Employee.class, team.getEmployees().iterator().next().getId());
		team.setEmployees(new ArrayList<Employee>(Collections.singletonList(employee)));
		return team;
	}

	@Override
	public void throwException() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public <T extends Serializable> List<T> list(Class<T> type) {
		return sessionFactory.getCurrentSession().createCriteria(type).list();
	}

	@Override
	public <T extends Serializable> T singleResult(Class<T> type, String qlString, Object... args) {
		List<T> list = list(type, qlString, args);
		if (list.size() != 1) {
			throw new NonUniqueResultException(list.size());
		}
		return list.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Serializable> List<T> list(Class<T> type, String qlString, Object... args) {
		if (!PersistenceProviderUtils.isEclipseLink()) {
			qlString = qlString.replaceFirst("\\?\\d", "?");
		}
		Query query = sessionFactory.getCurrentSession().createQuery(qlString);
		for (int i = 0; i < args.length; i++) {
			query.setParameter(i, args[i]);
		}
		return query.list();
	}
}
