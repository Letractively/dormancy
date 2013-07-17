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
import at.dormancy.container.Box;
import at.dormancy.container.Team;
import at.dormancy.entity.Book;
import at.dormancy.persister.CollectionPersister;
import at.dormancy.persister.NoOpPersister;
import at.dormancy.persister.NullPersister;
import at.dormancy.persister.TeamPersister;
import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static at.dormancy.util.JpaProviderUtils.getPersistentSetClass;
import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class ContainerPersisterDormancyTest extends AbstractDormancyTest {
	@Test
	@SuppressWarnings("unchecked")
	public void testListPersister() {
		List<Employee> list = genericService.list(Employee.class);
		assertSame(getPersistentSetClass(), list.get(0).getEmployees().getClass());
		assertEquals(false, list.get(0).getColleagues() == null);

		List<Employee> clone = dormancy.clone(new ArrayList<Employee>(list));
		assertEquals(list.get(0), clone.get(0));
		assertEquals(Collections.<Employee>emptySet(), clone.get(0).getEmployees());
		assertEquals(Collections.<Employee>emptySet(), clone.get(0).getColleagues());

		persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext().clear();
		List<Employee> merge = dormancy.merge(new ArrayList<Employee>(clone));
		assertEquals(clone.get(0), merge.get(0));
		assertSame(getPersistentSetClass(), merge.get(0).getEmployees().getClass());
		assertEquals(false, merge.get(0).getColleagues() == null);
	}

	@Test
	public void testCustomPersister() {
		dormancy.getConfig().setCloneObjects(true);
		dormancy.getPersisterMap().clear();
		dormancy.addEntityPersister(new TeamPersister(dormancy), Team.class);
		dormancy.addEntityPersister(NoOpPersister.getInstance());
		dormancy.addEntityPersister(NullPersister.getInstance());
		CollectionPersister<List> collectionPersister = new CollectionPersister<List>(dormancy);
		collectionPersister.setPersistentUnitProvider(persistenceUnitProvider);
		dormancy.addEntityPersister(collectionPersister);

		Employee a = service.get(Employee.class, refA.getId());
		Team custom = new Team(a);
		assertSame(Employee.class, custom.getEmployees().get(0).getClass());

		Team merge = dormancy.merge(custom);
		assertSame(custom, merge);
		assertEquals(true, isManaged(merge.getEmployees().get(0), persistenceUnitProvider));

		Team clone = dormancy.clone(merge);
		assertSame(custom, clone);
		assertSame(Employee.class, clone.getEmployees().get(0).getClass());
		assertEquals(false, isManaged(merge.getEmployees().get(0), persistenceUnitProvider));
	}

	@Test
	public void testContainerWithoutCustomPersister() {
		Book book = service.get(Book.class, refBook.getId());
		Box custom = new Box(book);
		assertSame(Book.class, custom.getBook().getClass());

		Box merge = dormancy.merge(custom);
		assertSame(custom, merge);
		assertSame(Book.class, merge.getBook().getClass());
		assertSame(book, merge.getBook());

		Box clone = dormancy.clone(merge);

		try {
			dormancy.getConfig().setCloneObjects(true);
			clone = dormancy.clone(merge);
			fail(BeanInstantiationException.class.getSimpleName() + " expected");
		} catch (BeanInstantiationException e) {
			// expected
		} finally {
			dormancy.getConfig().setCloneObjects(false);
		}
	}
}
