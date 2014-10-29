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

import at.dormancy.entity.Book;
import at.dormancy.entity.CollectionEntity;
import at.dormancy.util.DormancyContext;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.Map;

import static at.dormancy.AbstractDormancyTest.isManaged;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author Gregor Schauer
 */
public class MapHandlerTest extends AbstractObjectHandlerTest<MapHandler> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		handler = new MapHandler(dormancy);
	}

	@Test
	public void testEntity() {
		CollectionEntity a = genericService.get(CollectionEntity.class, refCollectionEntity.getId());
		assertEquals(true, isManaged(a.getLongMap(), persistenceUnitProvider));
		assertEquals(false, isManaged(a.getLongMap().keySet().iterator().next(), persistenceUnitProvider));
		assertEquals(false, isManaged(a.getLongMap().values().iterator().next(), persistenceUnitProvider));

		Map disconnected = (Map) handler.disconnect(a.getLongMap(), new DormancyContext());
		assertEquals(false, isManaged(disconnected, persistenceUnitProvider));
		assertEquals(false, isManaged(disconnected.keySet().iterator().next(), persistenceUnitProvider));
		assertEquals(false, isManaged(disconnected.values().iterator().next(), persistenceUnitProvider));

		Map merged = (Map) handler.apply(disconnected, a.getLongMap(), new DormancyContext());
		assertEquals(true, isManaged(merged, persistenceUnitProvider));
		assertEquals(false, isManaged(merged.keySet().iterator().next(), persistenceUnitProvider));
		assertEquals(false, isManaged(merged.values().iterator().next(), persistenceUnitProvider));
	}

	@Test(expected = RuntimeException.class)
	public void testLazyInitialization() throws Exception {
		CollectionEntity a = genericService.get(CollectionEntity.class, refCollectionEntity.getId());

		Map<Long, Book> disconnected = handler.disconnect(a.getBookMap(), new DormancyContext());
		assertNotSame(a.getBookMap(), disconnected);

		assertEquals(true, isManaged(a, persistenceUnitProvider));
		assertEquals(false, isManaged(disconnected, persistenceUnitProvider));

		persistenceContextHolder.clear();
		a = genericService.get(CollectionEntity.class, refCollectionEntity.getId());
		handler.apply(disconnected, a.getBookMap(), new DormancyContext());
	}
}
