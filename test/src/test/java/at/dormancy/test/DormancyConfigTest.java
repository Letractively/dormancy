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
package at.dormancy.test;

import at.dormancy.AbstractDormancyTest;
import at.dormancy.DormancyConfiguration;
import at.dormancy.entity.Book;
import at.dormancy.entity.Employee;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
@Transactional
public class DormancyConfigTest extends AbstractDormancyTest {
	DormancyConfiguration config;

	@After
	public void after() {
		try {
			ReflectionTestUtils.setField(dormancy, "config", BeanUtils.cloneBean(config));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		try {
			config = (DormancyConfiguration) BeanUtils.cloneBean(dormancy.getConfig());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test(expected = RuntimeException.class)
	public void testNewInstance() {
		if (isJpa()) {
			throw new RuntimeException("Test not supported by JPA");
		}

		Long id = (Long) service.save(new Book("new"));
		Book load = service.get(Book.class, id);
		assertEquals(false, isManaged(load, persistenceUnitProvider));
		assertEquals("new", load.getTitle());
	}

	@Test(expected = RuntimeException.class)
	public void testSaveTransientCollection() {
		if (isJpa()) {
			throw new RuntimeException("Test not supported by JPA");
		}

		dormancy.getConfig().setCloneObjects(true);
		String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?1";
		Employee c = dormancy.disconnect(genericService.singleResult(Employee.class, qlString, refC.getId()));
		Employee refD = new Employee("D", c);
		c.getEmployees().add(refD);
		dormancy.apply(c, genericService.singleResult(Employee.class, qlString, refC.getId()));
	}

	@Test(expected = RuntimeException.class)
	public void testSaveTransientAssociation() {
		String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?1";
		Employee c = dormancy.disconnect(genericService.singleResult(Employee.class, qlString, refC.getId()));
		Employee refD = new Employee("D", c);
		c.setBoss(refD);
		dormancy.apply(c, genericService.singleResult(Employee.class, qlString, refC.getId()));
	}

	@Test(expected = RuntimeException.class)
	public void testNotSaveTransientAssociation() {
		if (isJpa()) {
			throw new RuntimeException("Test not supported by JPA");
		}

		String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?1";
		Employee c = dormancy.disconnect(genericService.singleResult(Employee.class, qlString, refC.getId()));
		c.setBoss(new Employee("D", c));
		dormancy.apply(c, genericService.singleResult(Employee.class, qlString, refC.getId()));
	}

	@Test
	public void testUpdateAssociation() {
		String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?1";
		Employee a = dormancy.disconnect(genericService.singleResult(Employee.class, qlString, refA.getId()));
		Employee c = dormancy.disconnect(genericService.singleResult(Employee.class, qlString, refC.getId()));
		c.setBoss(a);

		dormancy.apply(c, genericService.singleResult(Employee.class, qlString, refC.getId()));

		c = dormancy.disconnect(genericService.singleResult(Employee.class, qlString, refC.getId()));
		assertEquals(a, c.getBoss());
	}
}
