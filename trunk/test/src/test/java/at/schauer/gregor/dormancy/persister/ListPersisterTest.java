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
package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.CollectionEntity;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.Session;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class ListPersisterTest extends PersisterTest<CollectionPersister<List>> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new CollectionPersister<List>(dormancy);
		persister.setSessionFactory(persistenceUnitProvider);
		persister.setConfig(dormancy.getConfig());
	}

	@Test
	public void testSame() {
		List<String> singleton = new ArrayList<String>();

		List clone = persister.clone(singleton);
		assertEquals(singleton, clone);
		assertNotSame(singleton, clone);

		List merge = persister.merge(singleton);
		assertEquals(singleton, merge);
		assertNotSame(singleton, merge);

		merge = persister.merge(clone, singleton);
		assertEquals(singleton, merge);
		assertSame(singleton, merge);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testEntity() throws Exception {
		Session session = sessionFactory.getCurrentSession();
		CollectionEntity a = (CollectionEntity) session.get(CollectionEntity.class, 1L);
		assertEquals(false, a.getBooks().isEmpty());
		assertEquals(true, AbstractDormancyTest.isManaged(a.getBooks(), session));
		assertEquals(true, AbstractDormancyTest.isManaged(a.getBooks().get(0), session));

		List<Book> clone = persister.clone(a.getBooks());
		assertEquals(false, AbstractDormancyTest.isProxy(clone, session));
		assertEquals(false, AbstractDormancyTest.isProxy(clone.get(0), session));
		List<Book> copy = Arrays.<Book>asList((Book) BeanUtils.cloneBean(clone.get(0)));

		List<Book> merge = persister.merge(clone);
		assertEquals(true, AbstractDormancyTest.isManaged(merge.get(0), session));

		merge = persister.merge(copy, a.getBooks());
		assertEquals(true, AbstractDormancyTest.isManaged(merge.get(0), session));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testNonEntity() {
		Session session = sessionFactory.getCurrentSession();
		CollectionEntity a = (CollectionEntity) session.get(CollectionEntity.class, 1L);
		assertEquals(false, a.getIntegers().isEmpty());
		assertEquals(true, AbstractDormancyTest.isManaged(a.getIntegers(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(a.getIntegers().get(0), session));

		List<Integer> clone = persister.clone(a.getIntegers());
		assertEquals(false, AbstractDormancyTest.isManaged(clone, session));
		assertEquals(false, AbstractDormancyTest.isManaged(clone.get(0), session));

		List<Integer> merge = persister.merge(clone);
		assertEquals(false, AbstractDormancyTest.isManaged(merge, session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.get(0), session));

		merge = persister.merge(clone, a.getIntegers());
		assertEquals(true, AbstractDormancyTest.isManaged(merge, session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.get(0), session));
	}
}
