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
package at.dormancy.access;

import at.dormancy.AbstractDormancyTest;
import at.dormancy.entity.Employee;
import at.dormancy.entity.ReadOnlyEntity;
import at.dormancy.metadata.ObjectMetadata;
import at.dormancy.metadata.resolver.FieldMetadataResolver;
import at.dormancy.metadata.resolver.MetadataResolver;
import at.dormancy.metadata.resolver.PropertyMetadataResolver;
import at.dormancy.util.AbstractDormancyUtils;
import at.dormancy.util.ClassLookup;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.TypeDescriptor;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class MetadataResolverTest extends AbstractDormancyTest {
	@Test
	public void test() {
		Employee entity = new Employee();
		entity.setId(1L);

		Class<MetadataResolver> clazz = ClassLookup.find(
				"at.dormancy.metadata.resolver.HibernateMetadataResolver",
				"at.dormancy.metadata.resolver.JpaMetadataResolver").get();

		Class[] parameterTypes = {AbstractDormancyUtils.class};
		@SuppressWarnings("unchecked")
		Constructor<MetadataResolver> ctor = ConstructorUtils.getMatchingAccessibleConstructor(clazz, parameterTypes);
		MetadataResolver metadataResolver = BeanUtils.instantiateClass(ctor, dormancy.getUtils());

		ObjectMetadata metadata = metadataResolver.getMetadata(entity.getClass());
		MetadataPropertyAccessor propertyAccessor = new MetadataPropertyAccessor(entity, metadata);

		assertSame(entity, propertyAccessor.convertIfNecessary(entity, Object.class));
		assertSame(entity, propertyAccessor.convertIfNecessary(entity, Employee.class));

		assertEquals(Long.class, propertyAccessor.getPropertyType("id"));
		assertEquals(entity.getId(), propertyAccessor.getPropertyValue("id"));

		assertEquals(true, propertyAccessor.getPropertyTypeDescriptor("id")
				.isAssignableTo(TypeDescriptor.valueOf(Number.class)));

		Object fieldVal = propertyAccessor.getFieldAccessor().getPropertyValue("id");
		Object propertyValue = propertyAccessor.getPropertyAccessor().getPropertyValue("id");
		assertEquals(fieldVal, propertyValue);
	}

	@Test
	public void testFieldMetadataResolver() throws Exception {
		FieldMetadataResolver metadataResolver = new FieldMetadataResolver();
		ObjectMetadata metadata = metadataResolver.getMetadata(ReadOnlyEntity.class);
		MetadataPropertyAccessor propertyAccessor = new MetadataPropertyAccessor(ReadOnlyEntity.class, metadata);

		assertEquals(false, propertyAccessor.isReadableProperty("class"));
		assertEquals(false, propertyAccessor.isWritableProperty("class"));

		assertEquals(true, propertyAccessor.isReadableProperty("time"));
		assertEquals(true, propertyAccessor.isWritableProperty("time"));

		assertEquals(false, propertyAccessor.isReadableProperty("name"));
		assertEquals(false, propertyAccessor.isWritableProperty("name"));
	}

	@Test
	public void testPropertyMetadataResolver() throws Exception {
		PropertyMetadataResolver metadataResolver = new PropertyMetadataResolver();
		ObjectMetadata metadata = metadataResolver.getMetadata(ReadOnlyEntity.class);
		MetadataPropertyAccessor propertyAccessor = new MetadataPropertyAccessor(ReadOnlyEntity.class, metadata);

		assertEquals(false, propertyAccessor.isReadableProperty("class"));
		assertEquals(false, propertyAccessor.isWritableProperty("class"));

		assertEquals(true, propertyAccessor.isReadableProperty("time"));
		assertEquals(true, propertyAccessor.isWritableProperty("time"));

		assertEquals(false, propertyAccessor.isReadableProperty("name"));
		assertEquals(false, propertyAccessor.isWritableProperty("name"));
	}

	@Test
	public void testObjectProperties() throws Exception {
		PropertyMetadataResolver metadataResolver = new PropertyMetadataResolver();
		try {
			metadataResolver.getMetadata(Object.class);
			fail(RuntimeException.class.getSimpleName() + " expected");
		} catch (RuntimeException e) {
			assertEquals(IntrospectionException.class, e.getCause().getClass());
		}
	}
}
