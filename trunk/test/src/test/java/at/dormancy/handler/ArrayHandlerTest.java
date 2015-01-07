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

import at.dormancy.entity.CollectionEntity;
import at.dormancy.util.DormancyContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static at.dormancy.AbstractDormancyTest.isManaged;
import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 * @since 1.1.0
 */
public class ArrayHandlerTest extends AbstractObjectHandlerTest<ArrayHandler<Object>> {
	@Override
	protected void createHandler() {
		handler = new ArrayHandler<Object>(dormancy);
	}

	@Before
	public void before() {
		dormancy.getConfig().setCloneObjects(true);
	}

	@After
	public void after() {
		dormancy.getConfig().setCloneObjects(false);
	}

	@Test
	public void test() throws Exception {
		CollectionEntity a = genericService.get(CollectionEntity.class, refCollectionEntity.getId());
		Object[] array = {0, "", a};

		Object[] disconnected = handler.disconnect(array, new DormancyContext());
		assertNotSame(array, disconnected);
		assertSame(array[0], disconnected[0]);
		assertSame(array[1], disconnected[1]);
		assertNotSame(array[2], disconnected[2]);

		assertEquals(true, isManaged(array[2], persistenceUnitProvider));
		assertEquals(false, isManaged(disconnected[2], persistenceUnitProvider));

		// Ensure that the maps are initialized
		assertEquals(1, a.getBookMap().size());
		assertEquals(1, a.getLongMap().size());

		Object[] merged = handler.apply(disconnected, array, new DormancyContext());
		assertNotSame(array, merged);
		assertNotSame(disconnected, merged);
		assertSame(disconnected[0], merged[0]);
		assertSame(disconnected[1], merged[1]);
		assertNotSame(disconnected[2], merged[2]);

		assertEquals(false, isManaged(disconnected[2], persistenceUnitProvider));
		assertEquals(true, isManaged(merged[2], persistenceUnitProvider));
	}
}
