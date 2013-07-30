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

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author Gregor Schauer
 */
public class NullPersisterTest {
	@Test
	public void test() throws Exception {
		NullPersister<Object> persister = new NullPersister<Object>();
		assertNotSame(NullPersister.getInstance(), persister);

		persister.getSupportedTypes().clear();
		assertEquals(0, persister.getSupportedTypes().size());

		persister.setSupportedTypes(Collections.<Class<?>>singleton(Object.class));
		assertEquals(1, persister.getSupportedTypes().size());

		assertEquals(null, persister.clone(null));
		assertEquals(null, persister.clone(""));

		assertEquals(null, persister.merge(null));
		assertEquals(null, persister.merge(""));

		assertEquals(null, persister.merge(null, null));
		assertEquals(null, persister.merge("", ""));
	}
}
