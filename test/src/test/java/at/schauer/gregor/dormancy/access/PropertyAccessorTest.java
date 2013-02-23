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
package at.schauer.gregor.dormancy.access;

import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.ReadOnlyEntity;
import at.schauer.gregor.dormancy.entity.WriteOnlyEntity;
import at.schauer.gregor.dormancy.util.DormancyUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.PropertyAccessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Gregor Schauer
 */
public class PropertyAccessorTest {
	static DormancyUtils utils;

	static {
		try {
			utils = (DormancyUtils) ConstructorUtils.invokeConstructor(DormancyUtils.class, new Object[]{null}, new Class[]{SessionFactory.class});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testApplication() {
		Application application = new Application();
		application.setId(1L);
		application.setName("app");

		PropertyAccessor accessor = new StrategyPropertyAccessor(application, utils.getAccessTypeStrategy(AopUtils.getTargetClass(application)));
		assertSame(Long.class, accessor.getPropertyType("id"));
		assertSame(String.class, accessor.getPropertyType("name"));

		assertEquals(1L, accessor.getPropertyValue("id"));
		assertEquals("app", accessor.getPropertyValue("name"));
	}

	@Test(expected = MethodInvocationException.class)
	public void testReadOnlyEntity() {
		Object entity = new ReadOnlyEntity(1L, "val");
		PropertyAccessor accessor = new StrategyPropertyAccessor(entity, utils.getAccessTypeStrategy(AopUtils.getTargetClass(entity)));
		assertEquals(1L, accessor.getPropertyValue("id"));
		assertEquals("val", accessor.getPropertyValue("value"));

		accessor.setPropertyValue("id", 0L);
		accessor.setPropertyValue("value", "value");
	}

	@Test(expected = InvalidPropertyException.class)
	public void testWriteOnlyEntity() {
		Object entity = new WriteOnlyEntity(1L, "val");
		PropertyAccessor accessor = new StrategyPropertyAccessor(entity, utils.getAccessTypeStrategy(AopUtils.getTargetClass(entity)));
		assertEquals(1L, accessor.getPropertyValue("id"));
		assertEquals("val", accessor.getPropertyValue("value"));

		accessor.setPropertyValue("id", 0L);
		accessor.setPropertyValue("value", "value");
	}
}
