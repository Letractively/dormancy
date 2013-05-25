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

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gregor Schauer
 */
public class NoOpPersisterTest {
	@Test
	@SuppressWarnings("unchecked")
	public final void testNull() {
		NoOpPersister persister = NoOpPersister.getInstance();
		Map<Object, Object> tree = Collections.emptyMap();
		assertEquals(null, persister.clone(null));
		assertEquals(null, persister.merge(null));
		assertEquals(null, persister.merge(null, null));
		assertEquals(null, persister.clone_(null, tree));
		assertEquals(null, persister.merge_(null, tree));
		assertEquals(null, persister.merge_(null, null, tree));
	}

	@Test
	public void testString() {
		NoOpPersister<String> persister = NoOpPersister.getInstance();
		Map<Object, Object> tree = Collections.emptyMap();
		assertEquals("", persister.clone(""));
		assertEquals("", persister.merge(""));
		assertEquals("", persister.merge("", " "));
		assertEquals("", persister.clone_("", tree));
		assertEquals("", persister.merge_("", tree));
		assertEquals("", persister.merge_("", " ", tree));
	}

	@Test
	public void testGetConfig() {
		NoOpPersister<?> persister = new NoOpPersister<Object>();
		persister.setConfig(null);
		assertNotNull(persister.getConfig());
	}
}
