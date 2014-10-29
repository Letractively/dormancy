package at.dormancy.util;

import at.dormancy.entity.Application;
import at.dormancy.metadata.ObjectMetadata;
import at.dormancy.metadata.resolver.MetadataResolver;
import at.dormancy.metadata.resolver.PropertyMetadataResolver;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DormancyContextTest {
	@Test
	public void test() throws Exception {
		MetadataResolver metadataResolver = new PropertyMetadataResolver();
		ObjectMetadata applicationMetadata = metadataResolver.getMetadata(Application.class);
		ObjectMetadata stringMetadata = metadataResolver.getMetadata(String.class);

		DormancyContext context = new DormancyContext(applicationMetadata, stringMetadata);
		assertEquals(true, context.getAdjacencyMap().isEmpty());
		assertEquals(null, context.getObjectMetadata(Object.class));
		assertSame(stringMetadata, context.getObjectMetadata(String.class));
		assertSame(applicationMetadata, context.getObjectMetadata(Application.class));
	}
}
