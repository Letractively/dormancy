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

import at.schauer.gregor.commons.test.BeanTester;
import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.EntityPersisterConfiguration;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.Employee;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.Query;
import org.hibernate.TransientObjectException;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.PostConstruct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gregor Schauer
 */
public class DormancyConfigTest extends AbstractDormancyTest {
	EntityPersisterConfiguration config;

	@After
	public void after() {
		try {
			dormancy.setConfig((EntityPersisterConfiguration) BeanUtils.cloneBean(config));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		try {
			config = (EntityPersisterConfiguration) BeanUtils.cloneBean(dormancy.getConfig());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testConfigWithoutParent() throws Exception {
		EntityPersisterConfiguration config = new EntityPersisterConfiguration();
		assertNotNull(config);
		BeanTester.getInstance().test(config);
	}

	@Test
	public void testConfigWithParent() throws Exception {
		EntityPersisterConfiguration config = new EntityPersisterConfiguration(new EntityPersisterConfiguration());
		assertNotNull(config);
		BeanTester.getInstance().test(config);
	}

	@Test(expected = TransientObjectException.class)
	public void testNewInstance() {
		dormancy.getConfig().setSaveNewEntities(false);

		Long id = (Long) service.save(new Book("new"));
		Book load = service.load(Book.class, id);
		assertEquals(false, isManaged(load, sessionFactory.getCurrentSession()));
		assertEquals("new", load.getTitle());
	}

	@Test
	public void testSaveTransientCollection() {
		dormancy.getConfig().setSaveNewEntities(true);
		dormancy.getConfig().setCloneObjects(true);

		Query query = sessionFactory.getCurrentSession().createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		c.getEmployees().add(new Employee("D", c));
		dormancy.merge(c, query.uniqueResult());

		Employee d = service.load(Employee.class, 4L);
		assertNotNull(d);
	}

	@Ignore
	@Test
	public void testSaveTransientAssociation() {
		dormancy.getConfig().setSaveNewEntities(true);

		Query query = sessionFactory.getCurrentSession().createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		c.setBoss(new Employee("D", c));
		dormancy.merge(c, query.uniqueResult());

		Employee d = service.load(Employee.class, 4L);
		assertNotNull(d);
	}

	@Test(expected = TransientObjectException.class)
	public void testNotSaveTransientAssociation() {
		dormancy.getConfig().setSaveNewEntities(false);

		Query query = sessionFactory.getCurrentSession().createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		c.setBoss(new Employee("D", c));
		dormancy.merge(c, query.uniqueResult());
	}

	@Test
	public void testUpdateAssociation() {
		Query query = sessionFactory.getCurrentSession().createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee a = (Employee) dormancy.clone(query.setParameter("id", 1L).uniqueResult());
		Employee c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		c.setBoss(a);
		dormancy.merge(c, query.uniqueResult());

		c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		assertEquals(a, c.getBoss());
	}
}
