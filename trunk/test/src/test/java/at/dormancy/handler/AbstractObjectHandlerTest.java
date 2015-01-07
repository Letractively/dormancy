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

import at.dormancy.Dormancy;
import at.dormancy.DormancySpringConfig;
import at.dormancy.entity.Book;
import at.dormancy.entity.CollectionEntity;
import at.dormancy.entity.Employee;
import at.dormancy.persistence.PersistenceUnitProvider;
import at.dormancy.service.GenericService;
import at.dormancy.util.DormancyContext;
import at.dormancy.util.PersistenceContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DormancySpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public abstract class AbstractObjectHandlerTest<T extends ObjectHandler> {
	@Inject
	protected Dormancy<?, ?, ?> dormancy;
	@Inject
	protected PersistenceUnitProvider<?, ?, ?> persistenceUnitProvider;
	@Inject
	protected PersistenceContextHolder<?> persistenceContextHolder;
	@Inject
	protected GenericService genericService;
	protected T handler;

	// Data
	protected final Book refBook = new Book("Book");
	protected final CollectionEntity refCollectionEntity = new CollectionEntity();
	protected final Employee refManager = new Employee("Manager", null);
	protected final Employee refBoss = new Employee("Boss", refManager);
	protected final Employee refWorker = new Employee("Worker", refBoss);

	@PostConstruct
	public final void postConstruct() {
		persistenceContextHolder.open();

		refCollectionEntity.setIntegers(Arrays.asList(1, 2, 3));
		refCollectionEntity.setBooks(new ArrayList<Book>(Collections.singletonList(refBook)));
		refCollectionEntity.setLongMap(new LinkedHashMap<Long, Long>(Collections.singletonMap(1L, 2L)));
		refCollectionEntity.setBookMap(new LinkedHashMap<Long, Book>(Collections.singletonMap(1L, refBook)));

		persistenceContextHolder.save(refBook);
		persistenceContextHolder.save(refCollectionEntity);

		persistenceContextHolder.save(refManager);
		persistenceContextHolder.save(refBoss);
		persistenceContextHolder.save(refWorker);

		refManager.getEmployees().add(refBoss);
		refBoss.getEmployees().add(refWorker);

		persistenceContextHolder.flush();
		persistenceContextHolder.close();

		createHandler();
	}

	protected abstract void createHandler();

	@Test
	@SuppressWarnings("unchecked")
	public final void testNull() {
		assertEquals(null, handler.disconnect(null, new DormancyContext()));
		assertEquals(null, handler.apply(null, null, new DormancyContext()));
	}
}
