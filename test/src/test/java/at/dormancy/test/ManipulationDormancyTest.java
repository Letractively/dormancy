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
package at.dormancy.test;

import at.dormancy.AbstractDormancyTest;
import at.dormancy.entity.Application;
import at.dormancy.entity.Employee;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
public class ManipulationDormancyTest extends AbstractDormancyTest {
	@Test
	public void testUpdateReferencedEntity() {
		Query query = sessionFactory.getCurrentSession().createQuery("FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee c = dormancy.clone((Employee) query.setParameter("id", refC.getId()).uniqueResult());
		c.getBoss().setName("Master");
		genericService.save(dormancy.merge(c, (Employee) query.setParameter("id", refC.getId()).uniqueResult()));

		Employee z = dormancy.clone((Employee) query.setParameter("id", refC.getId()).uniqueResult());
		assertEquals("Master", z.getBoss().getName());
	}

	@Ignore
	@Test
	public void testModifyCollection() throws SQLException {
		Session session = sessionFactory.getCurrentSession();
		session.setFlushMode(FlushMode.MANUAL);

		Query query = session.createQuery("FROM Employee e LEFT JOIN FETCH e.colleagues LEFT JOIN FETCH e.employees WHERE id = :id");
		Employee a = dormancy.clone((Employee) query.setParameter("id", refA.getId()).uniqueResult());
		Employee b = dormancy.clone((Employee) query.setParameter("id", refB.getId()).uniqueResult());
		Employee c = dormancy.clone((Employee) query.setParameter("id", refC.getId()).uniqueResult());
		Employee d = new Employee("Newbie", a);
		assertEquals(1, a.getEmployees().size());
		assertEquals(true, a.getEmployees().contains(b));

		a.getEmployees().remove(b);
		b.getEmployees().remove(c);
		b.setBoss(b);
		session.merge(b);

		a.getEmployees().add(c);
		a.getEmployees().add(d);
		c.setBoss(a);
		d.setBoss(a);

		Employee merge = dormancy.merge(a, (Employee) query.setParameter("id", refA.getId()).uniqueResult());
		session.save(merge);
		session.flush();
	}

	@Test
	public void testModifyCollectionNew() throws SQLException {
		dormancy.getConfig().setCloneObjects(true);

		Application app = genericService.singleResult(Application.class, "SELECT a FROM Application a JOIN FETCH a.employees WHERE a.id = ?", refApp.getId());

		Application clone = dormancy.clone(app);
		clone.getEmployees().clear();
		clone.getEmployees().add(service.get(Employee.class, refA.getId()));
		Application merge = dormancy.merge(clone, app);

		assertEquals(describe(clone), describe(merge));

		assertEquals(false, isManaged(clone.getEmployees(), persistenceUnitProvider));
		assertEquals(true, isManaged(merge.getEmployees(), persistenceUnitProvider));

		assertEquals("A", clone.getEmployees().iterator().next().getName());
		assertEquals("A", merge.getEmployees().iterator().next().getName());

		assertEquals("A", service.get(Employee.class, refA.getId()).getName());
		assertEquals("B", service.get(Employee.class, refB.getId()).getName());
		assertEquals("C", service.get(Employee.class, refC.getId()).getName());
	}
}
