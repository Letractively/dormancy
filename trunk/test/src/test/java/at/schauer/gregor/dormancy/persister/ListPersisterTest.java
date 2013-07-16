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
package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.CollectionEntity;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class ListPersisterTest extends AbstractPersisterTest<CollectionPersister<List>> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new CollectionPersister<List>(dormancy);
		persister.setPersistentUnitProvider(persistenceUnitProvider);
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
		CollectionEntity a = genericService.get(CollectionEntity.class, refCollectionEntity.getId());
		assertEquals(false, a.getBooks().isEmpty());
		assertEquals(true, AbstractDormancyTest.isManaged(a.getBooks(), persistenceUnitProvider));
		assertEquals(true, AbstractDormancyTest.isManaged(a.getBooks().get(0), persistenceUnitProvider));

		List<Book> clone = persister.clone(a.getBooks());
		assertEquals(false, AbstractDormancyTest.isProxy(clone, persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext()));
		assertEquals(false, AbstractDormancyTest.isProxy(clone.get(0), persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext()));
		List<Book> copy = Arrays.<Book>asList((Book) BeanUtils.cloneBean(clone.get(0)));

		List<Book> merge = persister.merge(clone);
		assertEquals(true, AbstractDormancyTest.isManaged(merge.get(0), persistenceUnitProvider));

		merge = persister.merge(copy, a.getBooks());
		assertEquals(true, AbstractDormancyTest.isManaged(merge.get(0), persistenceUnitProvider));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testNonEntity() {
		CollectionEntity a = genericService.get(CollectionEntity.class, refCollectionEntity.getId());
		assertEquals(false, a.getIntegers().isEmpty());
		assertEquals(true, AbstractDormancyTest.isManaged(a.getIntegers(), persistenceUnitProvider));
		assertEquals(false, AbstractDormancyTest.isManaged(a.getIntegers().get(0), persistenceUnitProvider));

		List<Integer> clone = persister.clone(a.getIntegers());
		assertEquals(false, AbstractDormancyTest.isManaged(clone, persistenceUnitProvider));
		assertEquals(false, AbstractDormancyTest.isManaged(clone.get(0), persistenceUnitProvider));

		List<Integer> merge = persister.merge(clone);
		assertEquals(false, AbstractDormancyTest.isManaged(merge, persistenceUnitProvider));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.get(0), persistenceUnitProvider));

		merge = persister.merge(clone, a.getIntegers());
		assertEquals(true, AbstractDormancyTest.isManaged(merge, persistenceUnitProvider));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.get(0), persistenceUnitProvider));
	}
}
