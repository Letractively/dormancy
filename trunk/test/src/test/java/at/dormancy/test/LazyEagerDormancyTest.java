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
import org.hibernate.HibernateException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Gregor Schauer
 */
public class LazyEagerDormancyTest extends AbstractDormancyTest {
	@Ignore
	@Test(expected = HibernateException.class)
	public void testLazyOneToOne() {
		Application app = service.get(Application.class, refApp.getId());
		assertNull(app.getResponsibleUser());

		app.setResponsibleUser(service.get(Employee.class, refA.getId()));
		service.save(app);
	}

	@Test(expected = HibernateException.class)
	public void testOverwriteLazyNullProperty() {
		Employee b = service.get(Employee.class, refB.getId());
		assertEquals(Collections.<Employee>emptySet(), b.getEmployees());
		b.setEmployees(Collections.singleton(service.get(Employee.class, refC.getId())));
		service.save(b);
	}

	@Test(expected = HibernateException.class)
	public void testOverwriteLazyInitializedProperty() {
		Employee b = service.get(Employee.class, refB.getId());
		assertEquals(Collections.<Employee>emptySet(), b.getColleagues());
		b.setColleagues(Collections.singleton(service.get(Employee.class, refC.getId())));
		service.save(b);
	}
}
