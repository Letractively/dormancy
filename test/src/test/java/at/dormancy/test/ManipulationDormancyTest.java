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
import at.dormancy.entity.Application;
import at.dormancy.entity.Employee;
import at.dormancy.util.ClassLookup;
import at.dormancy.util.PersistenceProviderUtils;
import org.junit.Test;

import java.sql.SQLException;

import static org.apache.commons.lang.reflect.FieldUtils.readDeclaredStaticField;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;

/**
 * @author Gregor Schauer
 */
public class ManipulationDormancyTest extends AbstractDormancyTest {
	@Test
	public void testUpdateReferencedEntity() {
		Employee c = dormancy.disconnect(genericService.singleResult(Employee.class,
				"SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?1", refC.getId()));
		c.getBoss().setName("Master");
		genericService.save(dormancy.apply(c, genericService.singleResult(Employee.class,
				"SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?1", refC.getId())));

		Employee z = dormancy.disconnect(genericService.singleResult(Employee.class,
				"SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = ?1", refC.getId()));
		assertEquals("Master", z.getBoss().getName());
	}

	@SuppressWarnings({"null", "unchecked"})
	@Test
	public void testModifyCollection() throws SQLException, IllegalAccessException {
		Object persistenceContext = persistenceContextHolder.getCurrent();
		if (isJpa()) {
			Class<? extends Enum> flushMode = ClassLookup.forName("javax.persistence.FlushModeType")
					.asSubclass(Enum.class);
			invokeMethod(persistenceContext, "setFlushMode", Enum.valueOf(flushMode, "COMMIT"));
		} else if (PersistenceProviderUtils.isHibernate3()) {
			Class<Object> flushMode = ClassLookup.forName("org.hibernate.FlushMode");
			invokeMethod(persistenceContext, "setFlushMode", readDeclaredStaticField(flushMode, "MANUAL"));
		} else {
			Class<? extends Enum> flushMode = ClassLookup.forName("org.hibernate.FlushMode").asSubclass(Enum.class);
			invokeMethod(persistenceContext, "setFlushMode", Enum.valueOf(flushMode, "MANUAL"));
		}

		String qlString = "SELECT e FROM Employee e LEFT JOIN FETCH e.colleagues "
				+ "LEFT JOIN FETCH e.employees WHERE e.id = ?1";
		Employee employee = genericService.singleResult(Employee.class, qlString, refA.getId());
		assertEquals(1, employee.getEmployees().size());
		Employee a = dormancy.disconnect(employee);
		Employee b = dormancy.disconnect(genericService.singleResult(Employee.class, qlString, refB.getId()));
		Employee c = dormancy.disconnect(genericService.singleResult(Employee.class, qlString, refC.getId()));
		assertEquals(1, a.getEmployees().size());
		assertEquals(true, a.getEmployees().contains(b));

		a.getEmployees().remove(b);
		b.getEmployees().remove(c);
		b.setBoss(b);
		invokeMethod(persistenceContext, "merge", b);

		a.getEmployees().add(c);
		c.setBoss(a);

		Employee merged = dormancy.apply(a, genericService.singleResult(Employee.class, qlString, refA.getId()));
		persistenceContextHolder.save(merged);
		persistenceContextHolder.flush();
	}

	@Test
	public void testModifyCollectionNew() throws SQLException {
		dormancy.getConfig().setCloneObjects(true);

		Application app = genericService.singleResult(Application.class,
				"SELECT a FROM Application a JOIN FETCH a.employees WHERE a.id = ?1", refApp.getId());
		assertEquals(1, app.getEmployees().size());

		Application disconnected = dormancy.disconnect(app);
		disconnected.getEmployees().clear();
		disconnected.getEmployees().add(service.get(Employee.class, refA.getId()));
		Application merged = dormancy.apply(disconnected, app);

		assertEquals(describe(disconnected), describe(merged));

		assertEquals(false, isManaged(disconnected.getEmployees(), persistenceUnitProvider));
		assertEquals(true, isManaged(merged.getEmployees(), persistenceUnitProvider));

		assertEquals("A", disconnected.getEmployees().iterator().next().getName());
		assertEquals("A", merged.getEmployees().iterator().next().getName());

		assertEquals("A", service.get(Employee.class, refA.getId()).getName());
		assertEquals("B", service.get(Employee.class, refB.getId()).getName());
		assertEquals("C", service.get(Employee.class, refC.getId()).getName());
	}
}
