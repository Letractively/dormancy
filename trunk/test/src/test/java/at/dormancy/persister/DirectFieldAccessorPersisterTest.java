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
package at.dormancy.persister;

import at.dormancy.entity.Application;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static at.dormancy.AbstractDormancyTest.describe;
import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class DirectFieldAccessorPersisterTest extends AbstractPersisterTest<DirectFieldAccessorPersister<Application>> {
	Application app = new Application("app", null, Collections.<Employee>emptySet(), "secret");

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new DirectFieldAccessorPersister<Application>(dormancy);
	}

	@Test
	public void test() {
		Application clone = persister.clone(app);
		assertEquals(describe(app), describe(clone));

		Application merge = persister.merge(app);
		assertEquals(describe(app), describe(merge));
	}

	@Test
	public void testReuse() {
		Application clone = persister.clone(app);
		assertNotSame(app, clone);
		persister.setReuseObject(true);
		clone = persister.clone(app);
		assertSame(app, clone);
	}
}
