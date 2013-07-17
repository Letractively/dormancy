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
package at.dormancy.persister.predicate;

import at.dormancy.entity.Application;
import at.dormancy.entity.Book;
import at.dormancy.entity.Employee;
import at.dormancy.persister.AbstractEntityPersister;
import at.dormancy.persister.AbstractPersisterTest;
import at.dormancy.persister.DirectFieldAccessorPersister;
import at.dormancy.persister.PredicatePersister;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static at.dormancy.AbstractDormancyTest.describe;
import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class AssignablePredicateTest extends AbstractPersisterTest<PredicatePersister<Object, AssignablePredicate>> {
	Application app = new Application("app", null, Collections.<Employee>emptySet(), "secret");
	Book book = new Book("Title");

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		AbstractEntityPersister<Object> delegate = new DirectFieldAccessorPersister<Object>(dormancy);
		AssignablePredicate predicate = new AssignablePredicate(Application.class);
		persister = new PredicatePersister<Object, AssignablePredicate>(delegate, predicate);
	}

	@Test
	public void testGetTypes() {
		AssignablePredicate predicate = (AssignablePredicate) ReflectionTestUtils.getField(persister, "predicate");
		predicate.setTypes();
		assertArrayEquals(ArrayUtils.EMPTY_CLASS_ARRAY, predicate.getTypes());
		predicate.setTypes(Application.class);
		assertArrayEquals(new Class[]{Application.class}, predicate.getTypes());
	}

	@Test
	public void testMatch() {
		Application clone = (Application) persister.clone(app);
		assertNotSame(app, clone);
		assertEquals(describe(app), describe(clone));

		Application merge = (Application) persister.merge(app);
		assertNotSame(app, merge);
		assertEquals(describe(app), describe(merge));
	}

	@Test
	public void testNotMatch() {
		Book clone = (Book) persister.clone(book);
		assertSame(book, clone);

		Book merge = (Book) persister.merge(book);
		assertSame(book, merge);
	}
}
