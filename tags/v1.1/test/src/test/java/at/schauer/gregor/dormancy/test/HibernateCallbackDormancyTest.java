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
package at.schauer.gregor.dormancy.test;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Employee;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gregor Schauer
 */
public class HibernateCallbackDormancyTest extends AbstractDormancyTest {
	@Test
	public void testHibernateCallback() throws SQLException {
		HibernateCallback<Employee> callback = new HibernateCallback<Employee>() {
			@Override
			@Transactional
			public Employee doInHibernate(Session session) {
				Criteria criteria = session.createCriteria(Employee.class);
				criteria.add(Restrictions.eq("id", 2L));
				criteria.setFetchMode("employees", FetchMode.JOIN);
				Employee employee = (Employee) criteria.uniqueResult();
				assertEquals(true, isManaged(employee, session));
				return employee;
			}
		};

		Session session = sessionFactory.getCurrentSession();
		Employee c = (Employee) session.get(Employee.class, 3L);
		assertEquals(true, isManaged(c, session));
		c = dormancy.clone(c);
		assertEquals(false, isProxy(c, session));
		session.clear();

		Employee b = (Employee) session.get(Employee.class, 2L);
		assertEquals(true, isManaged(b, session));
		b = dormancy.clone(b);
		assertEquals(false, isProxy(b, session));
		assertEquals(null, b.getEmployees());
		session.clear();

		b = callback.doInHibernate(session);
		assertEquals(true, isManaged(b, session));
		b = dormancy.clone(b);
		assertEquals(false, isProxy(b, session));
		assertNotNull(b.getEmployees());
		assertEquals(1, b.getEmployees().size());
		assertEquals(true, b.getEmployees().contains(c));

		b.setName("Leader");
		b.getEmployees().iterator().next().setName("Overseer");
		session.clear();

		b = dormancy.merge(b, callback);
		session.flush();
		assertNotNull(b);
		assertEquals(true, isManaged(b, session));
		assertEquals(1, b.getEmployees().size());
		assertEquals(b.getName(), b.getName());
		assertEquals("Overseer", b.getEmployees().iterator().next().getName());
	}

	@Test
	public void testNotMergingCollection() throws SQLException {
		HibernateCallback<Employee> callback = new HibernateCallback<Employee>() {
			@Override
			@Transactional
			public Employee doInHibernate(Session session) {
				return (Employee) session.createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees LEFT JOIN FETCH e.colleagues WHERE e.id = 1").uniqueResult();
			}
		};
		Employee ap = callback.doInHibernate(sessionFactory.getCurrentSession());
		Employee at = dormancy.clone(ap);
		assertEquals(1, ap.getEmployees().size());
		assertEquals(0, at.getColleagues().size());

		at.setColleagues(at.getEmployees());
		assertEquals(1, at.getColleagues().size());

		Employee merge = dormancy.merge(at, callback);
		assertEquals(1, merge.getEmployees().size());
		assertEquals(1, merge.getColleagues().size());
	}
}
