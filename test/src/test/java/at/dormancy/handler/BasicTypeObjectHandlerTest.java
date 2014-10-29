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
import at.dormancy.util.DormancyContext;
import org.junit.Test;

import static org.apache.commons.beanutils.PropertyUtils.describe;
import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class BasicTypeObjectHandlerTest {
	@Test
	@SuppressWarnings("unchecked")
	public final void testNull() {
		BasicTypeHandler handler = new BasicTypeHandler();
		DormancyContext ctx = new DormancyContext();
		assertEquals(null, handler.disconnect(null, new DormancyContext()));
		assertEquals(null, handler.apply(null, null, new DormancyContext()));
		assertEquals(null, handler.disconnect(null, ctx));
		assertEquals(null, handler.apply(null, null, ctx));
	}

	@Test
	public void testString() {
		BasicTypeHandler<String> handler = new BasicTypeHandler<String>();
		DormancyContext ctx = new DormancyContext();
		assertEquals("", handler.disconnect("", new DormancyContext()));
		assertEquals("", handler.apply("", " ", new DormancyContext()));
		assertEquals("", handler.disconnect("", ctx));
		assertEquals("", handler.apply("", " ", ctx));
	}

	@Test
	public void testCreateObject() throws Exception {
		BasicTypeHandler<Object> handler = new BasicTypeHandler<Object>();
		Book book = new Book();
		Book other = handler.createObject(book);
		assertEquals(describe(book), describe(other));
		assertNotSame(book, other);

		int[] array = new int[0];
		int[] object = handler.createObject(array);
		assertArrayEquals(array, object);
		assertNotSame(array, object);
	}
}
