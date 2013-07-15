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
import at.schauer.gregor.dormancy.interceptor.DormancyAdvisor;
import org.hibernate.StaleObjectStateException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Gregor Schauer
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VersioningDormancyTest extends AbstractDormancyTest {
	@Inject
	DormancyAdvisor dormancyAdvisor;

	@Test
	public void testVersion() {
		dormancy.getConfig().setCloneObjects(true);
		dormancyAdvisor.setMode(DormancyAdvisor.Mode.BOTH);
		Application app = service.get(Application.class, refApp.getId());
		app.setName(UUID.randomUUID().toString());
		int version = app.getLastUpdate().intValue();

		service.save(app);
		app = service.get(Application.class, refApp.getId());
		assertEquals(version + 1, app.getLastUpdate().intValue());
	}

	@Test
	public void testStaleObjectStateException() {
		Application app = service.get(Application.class, refApp.getId());
		app.setName(UUID.randomUUID().toString());
		service.save(app);

		try {
			app.setName(UUID.randomUUID().toString());
			service.save(app);
			fail(String.format("%s or %s expected", StaleObjectStateException.class.getSimpleName(),
					OptimisticLockException.class.getSimpleName()));
		} catch (StaleObjectStateException e) {
			// expected with Hibernate
		} catch (OptimisticLockException e) {
			// expected with JPA
		}
	}

	@Test
	public void testManipulateVersion() throws Throwable {
		Application app = service.get(Application.class, refApp.getId());
		ReflectionTestUtils.setField(app, "lastUpdate", app.getLastUpdate() + 1);
		try {
			service.save(app);
			fail(String.format("%s or %s expected", StaleObjectStateException.class.getSimpleName(),
					OptimisticLockException.class.getSimpleName()));
		} catch (StaleObjectStateException e) {
			// expected with Hibernate
		} catch (OptimisticLockException e) {
			// expected with JPA
		}
	}

	@Test
	public void testNonVersioning() {
		Book book = service.get(Book.class, refBook.getId());
		book.setTitle(UUID.randomUUID().toString());
		service.save(book);

		String title = UUID.randomUUID().toString();
		book.setTitle(title);
		service.save(book);
		assertEquals(title, service.get(Book.class, refBook.getId()).getTitle());
	}
}
