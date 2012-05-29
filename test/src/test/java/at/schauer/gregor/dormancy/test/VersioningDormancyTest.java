package at.schauer.gregor.dormancy.test;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Book;
import org.hibernate.StaleObjectStateException;
import org.junit.Test;

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
		app.setLastUpdate(app.getLastUpdate() + 1);
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
