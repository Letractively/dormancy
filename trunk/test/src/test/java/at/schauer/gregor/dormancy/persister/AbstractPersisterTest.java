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

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.DormancySpringConfig;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.CollectionEntity;
import at.schauer.gregor.dormancy.entity.Employee;
import at.schauer.gregor.dormancy.persistence.HibernatePersistenceUnitProvider;
import at.schauer.gregor.dormancy.service.GenericService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;
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
public abstract class AbstractPersisterTest<T extends AbstractEntityPersister> {
	@Inject
	protected Dormancy<SessionFactory, Session, ClassMetadata> dormancy;
	@Inject
	protected HibernatePersistenceUnitProvider persistenceUnitProvider;
	@Inject
	protected GenericService genericService;
	protected T persister;

	// Data
	protected final Book refBook = new Book("Book");
	protected final CollectionEntity refCollectionEntity = new CollectionEntity();
	protected final Employee refManager = new Employee("Manager", null);
	protected final Employee refBoss = new Employee("Boss", refManager);
	protected final Employee refWorker = new Employee("Worker", refBoss);

	@PostConstruct
	public void postConstruct() {
		Session session = persistenceUnitProvider.getPersistenceUnit().openSession();
		Transaction transaction = session.beginTransaction();

		refCollectionEntity.setIntegers(Arrays.asList(1, 2, 3));
		refCollectionEntity.setBooks(new ArrayList<Book>(Collections.singletonList(refBook)));
		refCollectionEntity.setLongMap(new LinkedHashMap<Long, Long>(Collections.singletonMap(1L, 2L)));
		refCollectionEntity.setBookMap(new LinkedHashMap<Long, Book>(Collections.singletonMap(1L, refBook)));

		session.persist(refBook);
		session.persist(refCollectionEntity);

		session.persist(refManager);
		session.persist(refBoss);
		session.persist(refWorker);

		refManager.getEmployees().add(refBoss);
		refBoss.getEmployees().add(refWorker);
		session.flush();

		transaction.commit();
		session.close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public final void testNull() {
		assertEquals(null, persister.clone(null));
		assertEquals(null, persister.merge(null));
		assertEquals(null, persister.merge(null, null));
	}
}
