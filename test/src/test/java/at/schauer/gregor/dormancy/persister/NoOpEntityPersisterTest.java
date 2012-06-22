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

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
public class NoOpEntityPersisterTest {
	private NoOpEntityPersister persister = new NoOpEntityPersister();

	@Test
	public final void testNull() {
		assertEquals(null, persister.clone(null));
		assertEquals(null, persister.merge(null));
		assertEquals(null, persister.merge(null, null));
	}

	@Test
	public void testString() {
		assertEquals("", persister.clone(""));
		assertEquals("", persister.merge(""));
		assertEquals("", persister.merge("", " "));
	}
}
