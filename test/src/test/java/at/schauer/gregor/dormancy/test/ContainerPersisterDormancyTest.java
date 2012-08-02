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
import at.schauer.gregor.dormancy.container.Box;
import at.schauer.gregor.dormancy.container.Team;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.Employee;
import at.schauer.gregor.dormancy.persister.CollectionPersister;
import at.schauer.gregor.dormancy.persister.TeamPersister;
import org.hibernate.collection.PersistentSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class ContainerPersisterDormancyTest extends AbstractDormancyTest {
	@Test
	@SuppressWarnings("unchecked")
	public void testListPersister() {
		List<Employee> list = sessionFactory.getCurrentSession().createCriteria(Employee.class).list();
		assertSame(PersistentSet.class, list.get(0).getEmployees().getClass());
		assertEquals(false, list.get(0).getColleagues() == null);

		List<Employee> clone = dormancy.clone(new ArrayList<Employee>(list));
		assertNotSame(list.get(0), clone.get(0));
		assertSame(LinkedHashSet.class, clone.get(0).getEmployees().getClass());
		assertEquals(null, clone.get(0).getColleagues());

		List<Employee> merge = dormancy.merge(new ArrayList<Employee>(clone));
		assertNotSame(clone.get(0), merge.get(0));
		assertSame(PersistentSet.class, merge.get(0).getEmployees().getClass());
		assertEquals(false, merge.get(0).getColleagues() == null);

		merge = dormancy.merge(clone, list);
		assertNotSame(clone.get(0), merge.get(0));
		assertSame(PersistentSet.class, merge.get(0).getEmployees().getClass());
		assertEquals(false, merge.get(0).getColleagues() == null);
	}

	@Test
	public void testCustomPersister() {
		dormancy.getPersisterMap().clear();
		dormancy.addEntityPersister(new TeamPersister(dormancy), Team.class);
		CollectionPersister<List> collectionPersister = new CollectionPersister<List>(dormancy);
		collectionPersister.setSessionFactory(sessionFactory);
		dormancy.addEntityPersister(collectionPersister);

		Employee a = service.load(Employee.class, 1L);
		Team custom = new Team(a);
		assertSame(Employee.class, custom.getEmployees().get(0).getClass());

		Team merge = dormancy.merge(custom);
		assertSame(custom, merge);
		assertEquals(true, isManaged(merge.getEmployees().get(0)));

		Team clone = dormancy.clone(merge);
		assertSame(custom, clone);
		assertSame(Employee.class, clone.getEmployees().get(0).getClass());
		assertEquals(false, isManaged(merge.getEmployees().get(0)));
	}

	@Test
	public void testContainerWithoutCustomPersister() {
		Book book = service.load(Book.class, 1L);
		Box custom = new Box(book);
		assertSame(Book.class, custom.getBook().getClass());

		Box merge = dormancy.merge(custom);
		assertSame(custom, merge);
		assertSame(Book.class, merge.getBook().getClass());
		assertSame(book, merge.getBook());

		Box clone = dormancy.clone(merge);
		assertSame(custom, clone);
		assertSame(Book.class, clone.getBook().getClass());
		assertSame(book, clone.getBook());
	}
}
