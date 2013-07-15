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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.PostConstruct;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 * @since 1.1.0
 */
public class ArrayPersisterTest extends AbstractPersisterTest<ArrayPersister<Object>> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new ArrayPersister<Object>(dormancy);
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

		Object[] clone = (Object[]) persister.clone(array);
		assertNotSame(array, clone);
		assertSame(array[0], clone[0]);
		assertSame(array[1], clone[1]);
		assertNotSame(array[2], clone[2]);

		assertEquals(true, AbstractDormancyTest.isManaged(array[2], persistenceUnitProvider));
		assertEquals(false, AbstractDormancyTest.isManaged(clone[2], persistenceUnitProvider));

		Object[] merge = (Object[]) persister.merge(clone);
		assertNotSame(array, merge);
		assertNotSame(clone, merge);
		assertSame(clone[0], merge[0]);
		assertSame(clone[1], merge[1]);
		assertNotSame(clone[2], merge[2]);

		assertEquals(false, AbstractDormancyTest.isManaged(clone[2], persistenceUnitProvider));
		assertEquals(true, AbstractDormancyTest.isManaged(merge[2], persistenceUnitProvider));

		merge = (Object[]) persister.merge(clone, array);
		assertNotSame(array, merge);
		assertNotSame(clone, merge);
		assertSame(clone[0], merge[0]);
		assertSame(clone[1], merge[1]);
		assertNotSame(clone[2], merge[2]);

		assertEquals(false, AbstractDormancyTest.isManaged(clone[2], persistenceUnitProvider));
		assertEquals(true, AbstractDormancyTest.isManaged(merge[2], persistenceUnitProvider));
	}
}
