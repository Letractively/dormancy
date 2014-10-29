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
package at.dormancy.metadata.resolver;

import at.dormancy.AbstractDormancyTest;
import at.dormancy.entity.Application;
import at.dormancy.entity.Book;
import at.dormancy.entity.EmbeddedIdEntity;
import at.dormancy.metadata.ObjectMetadata;
import at.dormancy.util.AbstractDormancyUtils;
import at.dormancy.util.ClassLookup;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;

import static at.dormancy.access.AccessType.FIELD;
import static at.dormancy.access.AccessType.PROPERTY;
import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class MetadataResolverTest extends AbstractDormancyTest {
	@Test
	public void testIdProperty() {
		ObjectMetadata metadata = createMetadataResolver().getMetadata(Application.class);
		assertEquals(PROPERTY, metadata.getAccessType("id"));
		assertEquals(FIELD, metadata.getAccessType("lastUpdate"));
	}

	@Test
	public void testIdField() {
		ObjectMetadata metadata = createMetadataResolver().getMetadata(Book.class);
		assertEquals(FIELD, metadata.getAccessType("id"));
		assertEquals(FIELD, metadata.getAccessType("title"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidProperty() {
		ObjectMetadata metadata = createMetadataResolver().getMetadata(Book.class);
		metadata.getAccessType("");
	}

	@Test
	public void testEmbeddedId() {
		ObjectMetadata metadata = createMetadataResolver().getMetadata(EmbeddedIdEntity.class);
		assertEquals(FIELD, metadata.getAccessType("embeddableEntity"));
		assertEquals(FIELD, metadata.getAccessType("value"));
	}

	@SuppressWarnings("unchecked")
	private MetadataResolver createMetadataResolver() {
		Class<MetadataResolver> clazz = ClassLookup.find(
				"at.dormancy.metadata.resolver.HibernateMetadataResolver",
				"at.dormancy.metadata.resolver.JpaMetadataResolver").get();
		Class[] parameterTypes = {AbstractDormancyUtils.class};
		Constructor<MetadataResolver> ctor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, parameterTypes);
		return BeanUtils.instantiateClass(ctor, dormancy.getUtils());
	}
}
