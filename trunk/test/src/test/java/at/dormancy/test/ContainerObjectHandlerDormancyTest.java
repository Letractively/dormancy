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
import at.dormancy.container.Box;
import at.dormancy.container.Team;
import at.dormancy.entity.Book;
import at.dormancy.entity.Employee;
import at.dormancy.handler.*;
import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static at.dormancy.util.PersistenceProviderUtils.getPersistentCollectionClass;
import static at.dormancy.util.PersistenceProviderUtils.getPersistentSetClass;
import static org.junit.Assert.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;

/**
 * @author Gregor Schauer
 */
public class ContainerObjectHandlerDormancyTest extends AbstractDormancyTest {
	@Test
	@SuppressWarnings("unchecked")
	public void testListHandler() {
		List<Employee> list = genericService.list(Employee.class);
		assertTrue(getPersistentCollectionClass().isAssignableFrom(list.get(0).getEmployees().getClass()));
		assertEquals(false, list.get(0).getColleagues() == null);

		List<Employee> disconnected = dormancy.disconnect(new ArrayList<Employee>(list));
		assertEquals(list.get(0), disconnected.get(0));
		assertEquals(Collections.<Employee>emptySet(), disconnected.get(0).getEmployees());
		assertEquals(Collections.<Employee>emptySet(), disconnected.get(0).getColleagues());

		persistenceContextHolder.clear();
		List<Employee> merged = dormancy.apply(new ArrayList<Employee>(disconnected));
		assertEquals(disconnected.get(0), merged.get(0));
		assertSame(getPersistentSetClass(), merged.get(0).getEmployees().getClass());
		assertEquals(false, merged.get(0).getColleagues() == null);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCustomObjectHandler() {
		dormancy.getConfig().setCloneObjects(true);
		((Map<Class<?>, ObjectHandler<?>>) getField(dormancy.getRegistry(), "handlerMap")).clear();
		dormancy.getRegistry().addObjectHandler(new TeamHandler(dormancy), Team.class);
		dormancy.getRegistry().addObjectHandler(new BasicTypeHandler<Object>());
		dormancy.getRegistry().addObjectHandler(new NullObjectHandler<Object>());
		CollectionHandler<List> collectionHandler = new CollectionHandler<List>(dormancy);
		dormancy.getRegistry().addObjectHandler(collectionHandler);

		Employee a = service.get(Employee.class, refA.getId());
		Team custom = new Team(a);
		assertSame(Employee.class, custom.getEmployees().get(0).getClass());

		persistenceContextHolder.clear();

		Team merged = dormancy.apply(custom);
		assertSame(custom, merged);
		assertEquals(true, isManaged(merged.getEmployees().get(0), persistenceUnitProvider));

		Team disconnected = dormancy.disconnect(merged);
		assertSame(custom, disconnected);
		assertSame(Employee.class, disconnected.getEmployees().get(0).getClass());
		assertEquals(false, isManaged(merged.getEmployees().get(0), persistenceUnitProvider));
	}

	@Test
	public void testContainerWithoutCustomObjectHandler() {
		Book book = service.get(Book.class, refBook.getId());
		Box custom = new Box(book);
		assertSame(Book.class, custom.getBook().getClass());

		Box merged = dormancy.apply(custom);
		assertSame(custom, merged);
		assertSame(Book.class, merged.getBook().getClass());
		assertSame(book, merged.getBook());

		Box disconnected = dormancy.disconnect(merged);

		try {
			dormancy.getConfig().setCloneObjects(true);
			disconnected = dormancy.disconnect(merged);
			fail(BeanInstantiationException.class.getSimpleName() + " expected");
		} catch (BeanInstantiationException e) {
			// expected
		} finally {
			dormancy.getConfig().setCloneObjects(false);
		}
	}
}
