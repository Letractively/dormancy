/*
 * Copyright 2013 Gregor Schauer
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
package at.dormancy.service;

import at.dormancy.Dormancy;
import at.dormancy.container.Team;
import at.dormancy.entity.Application;
import at.dormancy.entity.Employee;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Gregor Schauer
 */
@Transactional
public class ServiceImpl implements GenericService {
	@PersistenceUnit
	EntityManagerFactory emf;
	@PersistenceContext
	EntityManager em;
	@Inject
	Dormancy dormancy;

	@Override
	public void doNothing() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public Serializable save(Serializable obj) {
		Class<?> clazz = obj.getClass();
		while (clazz.getSimpleName().contains("$$_javassist_")) {
			clazz = clazz.getSuperclass();
		}

		EntityType<?> entityType = emf.getMetamodel().entity(clazz);
		if (entityType.hasSingleIdAttribute()) {
			SingularAttribute<?, ?> idAttribute = entityType.getId(entityType.getIdType().getJavaType());
			Object id = ReflectionTestUtils.getField(obj, idAttribute.getName());
			if (id == null) {
				em.persist(obj);
			} else {
				em.merge(obj);
			}
			em.flush();
			return (Serializable) ReflectionTestUtils.getField(obj, idAttribute.getName());
		} else {
			Set<SingularAttribute<?, ?>> idClassAttributes = (Set<SingularAttribute<?, ?>>) entityType.getIdClassAttributes();
			boolean isNew = false;
			for (SingularAttribute<?, ?> idClassAttribute : idClassAttributes) {
				Object id = ReflectionTestUtils.getField(obj, idClassAttribute.getName());
				if (id == null) {
					isNew = true;
					break;
				}
			}
			if (isNew) {
				em.persist(obj);
			} else {
				em.merge(obj);
			}
			em.flush();
			return (Serializable) entityType.getIdClassAttributes();
		}
	}

	@Override
	public Application loadApp(Long id) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Application> criteria = cb.createQuery(Application.class);
		Root<Application> root = criteria.from(Application.class);
		root.fetch("employees");
		criteria.where(cb.equal(root.get("id"), id));
		return em.createQuery(criteria).getSingleResult();
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
	@SuppressWarnings("unchecked")
	public List<Application> list() {
		return em.createQuery(em.getCriteriaBuilder().createQuery(Application.class)).getResultList();
	}

	@Override
	public Team next(Team team) {
		team.setEmployees(new ArrayList<Employee>(Collections.singletonList(get(Employee.class, team.getEmployees().iterator().next().getId() + 1))));
		return team;
	}

	@Override
	public Team prev(Team team) {
		team.setEmployees(new ArrayList<Employee>(Collections.singletonList(get(Employee.class, team.getEmployees().iterator().next().getId() - 1))));
		return team;
	}

	@Override
	public Team pass(Team team) {
		team.setEmployees(new ArrayList<Employee>(Collections.singletonList(get(Employee.class, team.getEmployees().iterator().next().getId()))));
		return team;
	}

	@Override
	public <T extends Serializable> List<T> list(Class<T> type) {
		CriteriaQuery<T> query = em.getCriteriaBuilder().createQuery(type);
		Root<T> root = query.from(type);
		return em.createQuery(query).getResultList();
	}

	@Override
	public <T extends Serializable> T singleResult(Class<T> type, String qlString, Object... args) {
		List<T> list = list(type, qlString, args);
		if (list.size() != 1) {
			throw new NonUniqueResultException();
		}
		return list.get(0);
	}

	@Override
	public <T extends Serializable> List<T> list(Class<T> type, String qlString, Object... args) {
		TypedQuery<T> query = em.createQuery(qlString, type);
		for (int i = 0; i < args.length; i++) {
			query.setParameter(i + 1, args[i]);
		}
		return query.getResultList();
	}
}
