/*
 * Copyright 2013 Gregor Schauer
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
package at.dormancy.access;

import at.dormancy.entity.Application;
import at.dormancy.entity.UnsupportedReadEntity;
import at.dormancy.entity.UnsupportedWriteEntity;
import at.dormancy.util.DormancyUtils;
import org.junit.Test;
import org.springframework.beans.*;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class PropertyAccessorTest {
	DormancyUtils utils;

	@SuppressWarnings("unchecked")
	public DormancyUtils getUtils() {
		if (utils == null) {
			try {
				Constructor<DormancyUtils> ctor = (Constructor<DormancyUtils>) DormancyUtils.class.getConstructors()[0];
				utils = BeanUtils.instantiateClass(ctor, new Object[]{null});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return utils;
	}

	@Test
	public void testApplication() {
		Application application = new Application();
		PropertyAccessor accessor = new StrategyPropertyAccessor(application, getUtils().getAccessTypeStrategy(application.getClass()));
		assertSame(Long.class, accessor.getPropertyType("id"));
		assertSame(Long.class, accessor.getPropertyTypeDescriptor("id").getType());
		assertSame(String.class, accessor.getPropertyType("name"));
		assertSame(String.class, accessor.getPropertyTypeDescriptor("name").getType());

		accessor.setPropertyValue(new PropertyValue("id", 1L));
		accessor.setPropertyValue(new PropertyValue("name", "app"));

		assertEquals(1L, accessor.getPropertyValue("id"));
		assertEquals("app", accessor.getPropertyValue("name"));

		assertEquals(true, accessor.isReadableProperty("lastUpdate"));
		assertEquals(true, accessor.isWritableProperty("lastUpdate"));
	}

	@Test(expected = MethodInvocationException.class)
	public void testReadOnlyEntity() {
		Object entity = new UnsupportedWriteEntity(1L, "val");
		PropertyAccessor accessor = new StrategyPropertyAccessor(entity, getUtils().getAccessTypeStrategy(entity.getClass()));
		assertEquals(1L, accessor.getPropertyValue("id"));
		assertEquals("val", accessor.getPropertyValue("value"));

		assertEquals(true, accessor.isReadableProperty("id"));
		assertEquals(true, accessor.isWritableProperty("id"));
		accessor.setPropertyValue("id", 0L);

		assertEquals(true, accessor.isReadableProperty("value"));
		assertEquals(true, accessor.isWritableProperty("value"));
		accessor.setPropertyValue("value", "value");
	}

	@Test(expected = InvalidPropertyException.class)
	public void testWriteOnlyEntity() {
		Object entity = new UnsupportedReadEntity(1L, "val");
		PropertyAccessor accessor = new StrategyPropertyAccessor(entity, getUtils().getAccessTypeStrategy(entity.getClass()));
		assertEquals(1L, accessor.getPropertyValue("id"));
		assertEquals("val", accessor.getPropertyValue("value"));

		assertEquals(true, accessor.isReadableProperty("id"));
		assertEquals(true, accessor.isWritableProperty("id"));
		accessor.setPropertyValue("id", 0L);

		assertEquals(true, accessor.isReadableProperty("value"));
		assertEquals(true, accessor.isWritableProperty("value"));
		accessor.setPropertyValue("value", "value");
	}
}
