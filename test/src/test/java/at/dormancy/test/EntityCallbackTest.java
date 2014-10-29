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
import at.dormancy.entity.Employee;
import at.dormancy.handler.callback.EntityCallback;
import at.dormancy.persistence.PersistenceUnitProvider;
import at.dormancy.util.PersistenceContextHolder;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;

/**
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class EntityCallbackTest extends AbstractDormancyTest {
	@Inject
	PersistenceContextHolder<?> persistenceContextHolder;

	@Test
	public void testEntityCallback() throws SQLException {
		EntityCallback<Employee, Object, Object, Object> callback =
				new EntityCallback<Employee, Object, Object, Object>() {
					@Override
					public Employee work(@Nonnull PersistenceUnitProvider<Object, Object, Object>
												 persistenceUnitProvider) {
						Object persistenceContext = persistenceUnitProvider.getPersistenceContextProvider()
								.getPersistenceContext();
						String qlString = "SELECT e FROM Employee e JOIN FETCH e.employees WHERE e.id = :id";
						Object query = invokeMethod(persistenceContext, "createQuery", qlString);
						Employee employee = invokeMethod(invokeMethod(query, "setParameter", "id", refB.getId()),
								isJpa() ? "getSingleResult" : "uniqueResult");
						assertEquals(true, isManaged(employee, persistenceContext));
						assertEquals(1, employee.getEmployees().size());
						return employee;
					}
				};

		Object session = persistenceContextHolder.getCurrent();
		Employee c = persistenceContextHolder.get(Employee.class, refC.getId());
		assertEquals(true, isManaged(c, session));
		c = dormancy.disconnect(c);
		assertEquals(false, isProxy(c, session));
		persistenceContextHolder.clear();

		Employee b = persistenceContextHolder.get(Employee.class, refB.getId());
		assertEquals(true, isManaged(b, session));
		b = dormancy.disconnect(b);
		assertEquals(false, isProxy(b, session));
		assertEquals(Collections.<Employee>emptySet(), b.getEmployees());
		persistenceContextHolder.clear();

		b = callback.work(persistenceUnitProvider);
		assertEquals(true, isManaged(b, session));
		b = dormancy.disconnect(b);
		assertEquals(false, isProxy(b, session));
		assertNotNull(b.getEmployees());
		assertEquals(1, b.getEmployees().size());
		assertEquals(true, b.getEmployees().contains(c));

		b.setName("Leader");
		b.getEmployees().iterator().next().setName("Overseer");
		persistenceContextHolder.clear();

		b = dormancy.apply(b, callback);
		persistenceContextHolder.flush();
		assertNotNull(b);
		assertEquals(true, isManaged(b, session));
		assertEquals(1, b.getEmployees().size());
		assertEquals(b.getName(), b.getName());
		assertEquals("Overseer", b.getEmployees().iterator().next().getName());
	}

	@Test
	public void testNotMergingCollection() throws SQLException {
		EntityCallback<Employee, Object, Object, Object> callback =
				new EntityCallback<Employee, Object, Object, Object>() {
					@Override
					public Employee work(@Nonnull PersistenceUnitProvider<Object, Object, Object>
												 persistenceUnitProvider) {
						Object persistenceContext = persistenceUnitProvider.getPersistenceContextProvider()
								.getPersistenceContext();
						String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.employees "
								+ "LEFT JOIN FETCH e.colleagues WHERE e.id = :id";
						Object query = invokeMethod(persistenceContext, "createQuery", qlString);
						Employee employee = invokeMethod(invokeMethod(query, "setParameter", "id", refA.getId()),
								isJpa() ? "getSingleResult" : "uniqueResult");
						assertEquals(true, isManaged(employee, persistenceContext));
						assertEquals(1, employee.getEmployees().size());
						return employee;
					}
				};
		Employee ap = callback.work(persistenceUnitProvider);
		Employee at = dormancy.disconnect(ap);
		assertEquals(1, ap.getEmployees().size());
		assertEquals(0, at.getColleagues().size());

		at.setColleagues(at.getEmployees());
		assertEquals(1, at.getColleagues().size());

		Employee merged = dormancy.apply(at, callback);
		assertEquals(1, merged.getEmployees().size());
		assertEquals(1, merged.getColleagues().size());
	}
}
