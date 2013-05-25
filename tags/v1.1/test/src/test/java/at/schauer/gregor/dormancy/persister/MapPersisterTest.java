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
import at.schauer.gregor.dormancy.entity.CollectionEntity;
import org.hibernate.Session;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
public class MapPersisterTest extends PersisterTest<MapPersister> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new MapPersister(dormancy);
		persister.setSessionFactory(sessionFactory);
	}

	@Test
	public void testEntity() {
		Session session = sessionFactory.getCurrentSession();
		CollectionEntity a = (CollectionEntity) session.get(CollectionEntity.class, 1L);
		assertEquals(true, AbstractDormancyTest.isManaged(a.getLongMap(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(a.getLongMap().keySet().iterator().next(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(a.getLongMap().values().iterator().next(), session));

		Map clone = (Map) persister.clone(a.getLongMap());
		assertEquals(false, AbstractDormancyTest.isManaged(clone, session));
		assertEquals(false, AbstractDormancyTest.isManaged(clone.keySet().iterator().next(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(clone.values().iterator().next(), session));

		Map merge = (Map) persister.merge(clone);
		assertEquals(false, AbstractDormancyTest.isManaged(merge, session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.keySet().iterator().next(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.values().iterator().next(), session));

		merge = (Map) persister.merge(clone, a.getLongMap());
		assertEquals(true, AbstractDormancyTest.isManaged(merge, session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.keySet().iterator().next(), session));
		assertEquals(false, AbstractDormancyTest.isManaged(merge.values().iterator().next(), session));
	}
}
