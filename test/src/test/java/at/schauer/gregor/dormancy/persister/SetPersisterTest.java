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
import at.schauer.gregor.dormancy.entity.Employee;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class SetPersisterTest extends AbstractPersisterTest<CollectionPersister<Set>> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new CollectionPersister<Set>(dormancy);
		persister.setPersistentUnitProvider(persistenceUnitProvider);
		persister.setConfig(dormancy.getConfig());
	}

	@Test
	public void testSame() {
		HashSet<String> singleton = new HashSet<String>();

		Set clone = persister.clone(singleton);
		assertEquals(singleton, clone);
		assertNotSame(singleton, clone);

		Set merge = persister.merge(singleton);
		assertEquals(singleton, merge);
		assertNotSame(singleton, merge);

		merge = persister.merge(clone, singleton);
		assertEquals(singleton, merge);
		assertSame(singleton, merge);
	}

	@Test
	public void testEntity() {
		Employee a = genericService.get(Employee.class, refBoss.getId());
		assertEquals(true, AbstractDormancyTest.isManaged(a.getEmployees(), persistenceUnitProvider));
		assertEquals(true, AbstractDormancyTest.isManaged(a.getEmployees().iterator().next(), persistenceUnitProvider));

		Set clone = persister.clone(a.getEmployees());
		assertEquals(false, AbstractDormancyTest.isManaged(clone, persistenceUnitProvider));
		assertEquals(false, AbstractDormancyTest.isProxy(clone.iterator().next(), persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext()));

		Set merge = persister.merge(clone);
		assertEquals(true, AbstractDormancyTest.isManaged(merge.iterator().next(), persistenceUnitProvider));

		merge = persister.merge(clone, a.getEmployees());
		assertEquals(true, AbstractDormancyTest.isManaged(merge.iterator().next(), persistenceUnitProvider));
	}
}
