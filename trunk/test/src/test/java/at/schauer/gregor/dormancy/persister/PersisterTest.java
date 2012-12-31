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

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.DormancySpringConfig;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.CollectionEntity;
import at.schauer.gregor.dormancy.entity.Employee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DormancySpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public abstract class PersisterTest<T extends AbstractEntityPersister> {
	@Inject
	protected Dormancy dormancy;
	@Inject
	protected SessionFactory sessionFactory;
	protected T persister;

	@PostConstruct
	public void postConstruct() {
		Session session = sessionFactory.openSession();

		Book book = new Book("Book");
		CollectionEntity collectionEntity = new CollectionEntity();
		collectionEntity.setIntegers(Arrays.asList(1, 2, 3));
		collectionEntity.setBooks(Collections.singletonList(book));
		collectionEntity.setLongMap(Collections.singletonMap(1L, 2L));
		collectionEntity.setBookMap(Collections.singletonMap(1L, book));

		Employee manager = new Employee("Manager", null);
		Employee boss = new Employee("Boss", manager);
		Employee worker = new Employee("Worker", boss);

		session.save(book);
		session.save(collectionEntity);

		session.save(manager);
		session.save(boss);
		session.save(worker);

		manager.getEmployees().add(boss);
		boss.getEmployees().add(worker);
		session.flush();
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