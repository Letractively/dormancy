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
