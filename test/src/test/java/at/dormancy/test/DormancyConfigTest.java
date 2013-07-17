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
import at.dormancy.EntityPersisterConfiguration;
import at.dormancy.entity.Book;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.TransientObjectException;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
@Transactional
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
		assertNull(ReflectionTestUtils.getField(new EntityPersisterConfiguration(), "parent"));
	}

	@Test
	public void testConfigWithParent() throws Exception {
		EntityPersisterConfiguration config = new EntityPersisterConfiguration(new EntityPersisterConfiguration());
		assertNotNull(ReflectionTestUtils.getField(config, "parent"));
	}

	@Test(expected = TransientObjectException.class)
	public void testNewInstance() {
		if (isJpa()) {
			throw new TransientObjectException("Test not supported by JPA");
		}

		dormancy.getConfig().setSaveNewEntities(false);

		Long id = (Long) service.save(new Book("new"));
		Book load = service.get(Book.class, id);
		assertEquals(false, isManaged(load, persistenceUnitProvider));
		assertEquals("new", load.getTitle());
	}

	@Test
	public void testSaveTransientCollection() {
		if (isJpa()) {
			return;
		}

		dormancy.getConfig().setSaveNewEntities(true);
		dormancy.getConfig().setCloneObjects(true);

		String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?";
		Employee c = dormancy.clone(genericService.singleResult(Employee.class, qlString, refC.getId()));
		Employee refD = new Employee("D", c);
		c.getEmployees().add(refD);
		dormancy.merge(c, genericService.singleResult(Employee.class, qlString, refC.getId()));

		Employee d = service.get(Employee.class, refD.getId());
		assertNotNull(d);
	}

	@Ignore
	@Test
	public void testSaveTransientAssociation() {
		dormancy.getConfig().setSaveNewEntities(true);

		String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?";
		Employee c = dormancy.clone(genericService.singleResult(Employee.class, qlString, refC.getId()));
		Employee refD = new Employee("D", c);
		c.setBoss(refD);
		dormancy.merge(c, genericService.singleResult(Employee.class, qlString, refC.getId()));

		Employee d = service.get(Employee.class, refD.getId());
		assertNotNull(d);
	}

	@Test(expected = TransientObjectException.class)
	public void testNotSaveTransientAssociation() {
		if (isJpa()) {
			throw new TransientObjectException("Test not supported by JPA");
		}

		dormancy.getConfig().setSaveNewEntities(false);

		String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?";
		Employee c = dormancy.clone(genericService.singleResult(Employee.class, qlString, refC.getId()));
		c.setBoss(new Employee("D", c));
		dormancy.merge(c, genericService.singleResult(Employee.class, qlString, refC.getId()));
	}

	@Test
	public void testUpdateAssociation() {
		String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?1";
		Employee a = dormancy.clone(genericService.singleResult(Employee.class, qlString, refA.getId()));
		Employee c = dormancy.clone(genericService.singleResult(Employee.class, qlString, refC.getId()));
		c.setBoss(a);

		dormancy.merge(c, genericService.singleResult(Employee.class, qlString, refC.getId()));

		c = dormancy.clone(genericService.singleResult(Employee.class, qlString, refC.getId()));
		assertEquals(a, c.getBoss());
	}
}
