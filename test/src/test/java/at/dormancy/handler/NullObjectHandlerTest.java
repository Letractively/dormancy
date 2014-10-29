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

import at.dormancy.util.DormancyContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
public class NullObjectHandlerTest {
	@Test
	public void test() throws Exception {
		NullObjectHandler<Object> handler = new NullObjectHandler<Object>();

		assertEquals(null, handler.createObject(null));
		assertEquals(null, handler.createObject(""));

		handler.getSupportedTypes().clear();
		assertEquals(0, handler.getSupportedTypes().size());

		handler.getSupportedTypes().clear();
		handler.getSupportedTypes().add(Object.class);
		assertEquals(1, handler.getSupportedTypes().size());

		assertEquals(null, handler.disconnect(null, new DormancyContext()));
		assertEquals(null, handler.disconnect("", new DormancyContext()));

		assertEquals(null, handler.apply(null, null, new DormancyContext()));
		assertEquals(null, handler.apply("", "", new DormancyContext()));
	}
}
