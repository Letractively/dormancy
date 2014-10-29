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
package at.dormancy.handler;

import at.dormancy.AbstractDormancyTest;
import at.dormancy.entity.Book;
import at.dormancy.entity.CollectionEntity;
import at.dormancy.util.DormancyContext;
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
public class ListHandlerTest extends AbstractObjectHandlerTest<CollectionHandler<List>> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		handler = new CollectionHandler<List>(dormancy);
	}

	@Test
	public void testSame() {
		List<String> singleton = new ArrayList<String>();

		List disconnected = handler.disconnect(singleton, new DormancyContext());
		assertEquals(singleton, disconnected);
		assertNotSame(singleton, disconnected);

		List merged = handler.apply(disconnected, singleton, new DormancyContext());
		assertEquals(singleton, merged);
		assertSame(singleton, merged);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testEntity() throws Exception {
		Object persistenceContext = persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();

		CollectionEntity a = genericService.get(CollectionEntity.class, refCollectionEntity.getId());
		assertEquals(false, a.getBooks().isEmpty());
		assertEquals(true, AbstractDormancyTest.isManaged(a.getBooks(), persistenceUnitProvider));
		assertEquals(true, AbstractDormancyTest.isManaged(a.getBooks().get(0), persistenceUnitProvider));

		List<Book> disconnected = handler.disconnect(a.getBooks(), new DormancyContext());
		assertEquals(false, AbstractDormancyTest.isProxy(disconnected, persistenceContext));
		assertEquals(false, AbstractDormancyTest.isProxy(disconnected.get(0), persistenceContext));
		List<Book> copy = Arrays.asList((Book) BeanUtils.cloneBean(disconnected.get(0)));

		List<Book> merged = handler.apply(copy, a.getBooks(), new DormancyContext());
		assertEquals(true, AbstractDormancyTest.isManaged(merged.get(0), persistenceUnitProvider));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testNonEntity() {
		CollectionEntity a = genericService.get(CollectionEntity.class, refCollectionEntity.getId());
		assertEquals(false, a.getIntegers().isEmpty());
		assertEquals(true, AbstractDormancyTest.isManaged(a.getIntegers(), persistenceUnitProvider));
		assertEquals(false, AbstractDormancyTest.isManaged(a.getIntegers().get(0), persistenceUnitProvider));

		List<Integer> disconnected = handler.disconnect(a.getIntegers(), new DormancyContext());
		assertEquals(false, AbstractDormancyTest.isManaged(disconnected, persistenceUnitProvider));
		assertEquals(false, AbstractDormancyTest.isManaged(disconnected.get(0), persistenceUnitProvider));

		List<Integer> merged = handler.apply(disconnected, a.getIntegers(), new DormancyContext());
		assertEquals(true, AbstractDormancyTest.isManaged(merged, persistenceUnitProvider));
		assertEquals(false, AbstractDormancyTest.isManaged(merged.get(0), persistenceUnitProvider));
	}
}
