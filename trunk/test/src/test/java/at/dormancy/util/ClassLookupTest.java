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
package at.dormancy.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class ClassLookupTest {
	@Test
	public void testNull() throws Exception {
		assertEquals(null, ClassLookup.forName(null));
		assertEquals(null, ClassLookup.forName(""));

		assertEquals(null, ClassLookup.find().get());
		assertEquals(null, ClassLookup.find(ArrayUtils.EMPTY_STRING_ARRAY).get());
		assertEquals(null, ClassLookup.find((String) null).get());
		assertEquals(null, ClassLookup.find((String[]) null).get());
		assertEquals(null, ClassLookup.find(new String[]{null}).get());

		ClassLookup lookup = ClassLookup.find(null, Byte.class.getName());
		assertEquals(Byte.class, lookup.or((Class<?>) null).or((String) null).orThrow(null).orThrow(null, null).get());
	}

	@Test
	public void testException() throws Exception {
		try {
			ClassLookup.find().orThrow(new NullPointerException()).get();
			fail(NullPointerException.class.getSimpleName() + " expected");
		} catch (NullPointerException e) {
			// expected
		}

		try {
			ClassLookup.find().orThrow(new IOException()).get();
			fail(RuntimeException.class.getSimpleName() + " expected");
		} catch (RuntimeException e) {
			assertSame(IOException.class, e.getCause().getClass());
		}

		String msg = "I knew it!";
		try {
			ClassLookup.find().orThrow(msg).get();
			fail(RuntimeException.class.getSimpleName() + " expected");
		} catch (RuntimeException e) {
			assertEquals(true, e.toString().endsWith(ExceptionUtils.getMessage(e)));
		}

		try {
			ClassLookup.find().orThrow(msg).list();
			fail(RuntimeException.class.getSimpleName() + " expected");
		} catch (Exception e) {
			assertEquals(true, e.toString().endsWith(ExceptionUtils.getMessage(e)));
		}
	}

	@Test
	public void testList() throws Exception {
		assertEquals(0, ClassLookup.find().list().size());
		assertEquals(0, ClassLookup.find("").list().size());
		assertEquals(1, ClassLookup.find("", Object.class.getName()).list().size());
	}
}
