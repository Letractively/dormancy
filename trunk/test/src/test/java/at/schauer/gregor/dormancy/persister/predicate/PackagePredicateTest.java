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
package at.schauer.gregor.dormancy.persister.predicate;

import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Employee;
import at.schauer.gregor.dormancy.persister.AbstractEntityPersister;
import at.schauer.gregor.dormancy.persister.AbstractPersisterTest;
import at.schauer.gregor.dormancy.persister.DirectFieldAccessorPersister;
import at.schauer.gregor.dormancy.persister.PredicatePersister;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;

import static at.schauer.gregor.dormancy.AbstractDormancyTest.describe;
import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class PackagePredicateTest extends AbstractPersisterTest<PredicatePersister<Object, PackagePredicate>> {
	Application app = new Application("app", null, Collections.<Employee>emptySet(), "secret");

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		AbstractEntityPersister<Object> delegate = new DirectFieldAccessorPersister<Object>(dormancy);
		PackagePredicate predicate = new PackagePredicate(Application.class.getPackage().getName());
		persister = new PredicatePersister<Object, PackagePredicate>(delegate, predicate);
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
		Date date = new Date();
		Date clone = (Date) persister.clone(date);
		assertSame(date, clone);

		Date merge = (Date) persister.merge(date);
		assertSame(date, merge);
	}
}
