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

import at.dormancy.entity.Employee;
import at.dormancy.util.DormancyContext;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

import static at.dormancy.AbstractDormancyTest.isManaged;
import static at.dormancy.AbstractDormancyTest.isProxy;
import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class SetHandlerTest extends AbstractObjectHandlerTest<CollectionHandler<Set>> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		handler = new CollectionHandler<Set>(dormancy);
	}

	@Test
	public void testSame() {
		HashSet<String> singleton = new HashSet<String>();

		Set<String> disconnected = handler.disconnect(singleton, new DormancyContext());
		assertEquals(singleton, disconnected);
		assertNotSame(singleton, disconnected);

		Set<String> merged = handler.apply(disconnected, singleton, new DormancyContext());
		assertEquals(singleton, merged);
		assertSame(singleton, merged);
	}

	@Test
	public void testEntity() {
		Object persistenceContext = persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();
		Employee a = genericService.get(Employee.class, refBoss.getId());
		assertEquals(true, isManaged(a.getEmployees(), persistenceUnitProvider));
		assertEquals(true, isManaged(a.getEmployees().iterator().next(), persistenceUnitProvider));

		Set disconnected = handler.disconnect(a.getEmployees(), new DormancyContext());
		assertEquals(false, isManaged(disconnected, persistenceUnitProvider));
		assertEquals(false, isProxy(disconnected.iterator().next(), persistenceContext));

		Set merged = handler.apply(disconnected, a.getEmployees(), new DormancyContext());
		assertEquals(true, isManaged(merged.iterator().next(), persistenceUnitProvider));
	}
}
