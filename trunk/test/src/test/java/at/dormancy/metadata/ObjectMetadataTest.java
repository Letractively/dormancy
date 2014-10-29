package at.dormancy.metadata;

import at.dormancy.access.AccessType;
import at.dormancy.entity.Book;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectMetadataTest {
	@Test
	public void test() throws Exception {
		ObjectMetadata m0 = new ObjectMetadata(Book.class);
		assertSame(Book.class, m0.getType());
		assertEquals(true, m0.getProperties().isEmpty());

		ObjectMetadata m1 = m0.withProperties(AccessType.FIELD, "id");
		assertEquals(true, m0.getProperties().isEmpty());
		assertEquals(false, m1.getProperties().isEmpty());
		assertNotSame(m0, m1);

		ObjectMetadata m2 = m1.withProperties(AccessType.FIELD, "id");
		assertSame(m1, m2);

		ObjectMetadata m3 = m2.withProperties(AccessType.PROPERTY, "id");
		assertNotSame(m2, m3);

		ObjectMetadata m4 = m3.withoutProperty("id");
		assertNotSame(m3, m4);
		assertEquals(false, m3.getProperties().isEmpty());
		assertEquals(true, m0.getProperties().isEmpty());

		ObjectMetadata m5 = m4.withoutProperty("id");
		assertSame(m4, m5);
	}
}
