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
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gregor Schauer
 */
@Transactional
public class JpaServiceImpl implements GenericService {
	@PersistenceContext
	EntityManager em;
	@Inject
	Dormancy<Object, Object, Object> dormancy;

	@Override
	public void doNothing() {
	}

	@Override
	public Serializable save(Serializable obj) {
		em.persist(obj);
		em.flush();
		return dormancy.getUtils().getIdentifier(dormancy.getUtils().getMetadata(obj), obj);
	}

	@Override
	public Application loadApp(Long id) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Application> cq = cb.createQuery(Application.class);
		Root<Application> app = cq.from(Application.class);
		cq.select(app).where(cb.equal(app.get("id"), id));
		TypedQuery<Application> q = em.createQuery(cq);
		return q.getSingleResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T get(Class<T> type, Long id) {
		return em.find(type, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T load(Class<T> type, Long id) {
		return em.getReference(type, id);
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
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(type);
		Root<T> app = cq.from(type);
		cq.select(app);
		TypedQuery<T> q = em.createQuery(cq);
		return q.getResultList();
	}

	@Override
	public <T extends Serializable> T singleResult(Class<T> type, String qlString, Object... args) {
		List<T> list = list(type, qlString, args);
		if (list.size() != 1) {
			throw new NonUniqueResultException(String.valueOf(list.size()));
		}
		return list.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Serializable> List<T> list(Class<T> type, String qlString, Object... args) {
		if (!PersistenceProviderUtils.isEclipseLink()) {
			qlString = qlString.replaceAll("\\?\\d+", "?");
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(type);
		Root<T> app = cq.from(type);
		cq.select(app);
		TypedQuery<T> q = em.createQuery(qlString, type);
		for (int i = 0; i < args.length; i++) {
			q.setParameter(i + 1, args[i]);
		}
		return q.getResultList();
	}
}
