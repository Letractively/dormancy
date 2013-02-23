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
package at.schauer.gregor.dormancy.test;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Book;
import org.hibernate.StaleObjectStateException;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Gregor Schauer
 */
public class VersioningDormancyTest extends AbstractDormancyTest {
	@Test
	public void testVersion() {
		Application app = service.load(Application.class, 1L);
		app.setName(UUID.randomUUID().toString());
		assertEquals(0, app.getLastUpdate().intValue());

		service.save(app);
		app = service.load(Application.class, 1L);
		assertEquals(1, app.getLastUpdate().intValue());
	}

	@Test
	public void testStaleObjectStateException() {
		Application app = service.load(Application.class, 1L);
		app.setName(UUID.randomUUID().toString());
		service.save(app);

		try {
			app.setName(UUID.randomUUID().toString());
			service.save(app);
			fail(StaleObjectStateException.class.getSimpleName() + " expected");
		} catch (StaleObjectStateException e) {
			// expected
		}
	}

	@Test(expected = StaleObjectStateException.class)
	public void testManipulateVersion() {
		Application app = service.load(Application.class, 1L);
		ReflectionTestUtils.setField(app, "lastUpdate", app.getLastUpdate() + 1);
		service.save(app);
	}

	@Test
	public void testNonVersioning() {
		Book book = service.load(Book.class, 1L);
		book.setTitle(UUID.randomUUID().toString());
		service.save(book);

		String title = UUID.randomUUID().toString();
		book.setTitle(title);
		service.save(book);
		assertEquals(title, service.load(Book.class, 1L).getTitle());
	}
}
