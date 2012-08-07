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
package at.schauer.gregor.dormancy.beans;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Employee;
import org.hibernate.classic.Session;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.LongType;
import org.junit.Test;
import org.springframework.beans.InvalidPropertyException;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class LazyInitializerPropertyAccessorTest extends AbstractDormancyTest {
	@Test
	public void test() {
		Session session = sessionFactory.getCurrentSession();
		Application a = (Application) session.get(Application.class, 1L);
		Employee e = a.getResponsibleUser();
		if (e instanceof HibernateProxy) {
			LazyInitializer initializer = ((HibernateProxy) e).getHibernateLazyInitializer();
			LazyInitializerPropertyAccessor propertyAccessor = new LazyInitializerPropertyAccessor(initializer);

			assertEquals(true, propertyAccessor.isReadableProperty("id"));
			assertEquals(true, propertyAccessor.isWritableProperty("id"));
			assertSame(LongType.class, propertyAccessor.getPropertyTypeDescriptor("id").getType());
			assertEquals(2L, propertyAccessor.getPropertyValue("id"));
			assertEquals(true, propertyAccessor.lazyInitializer.isUninitialized());

			assertEquals(true, propertyAccessor.isReadableProperty("name"));
			assertEquals(true, propertyAccessor.isWritableProperty("name"));
			assertSame(String.class, propertyAccessor.getPropertyTypeDescriptor("name").getType());
			assertEquals("B", propertyAccessor.getPropertyValue("name"));
			assertEquals(false, propertyAccessor.lazyInitializer.isUninitialized());

			propertyAccessor.setPropertyValue("id", 0L);
			propertyAccessor.setPropertyValue("name", "Y");
			assertEquals(0L, propertyAccessor.getPropertyValue("id"));
			assertEquals("Y", propertyAccessor.getPropertyValue("name"));

			Object l = 0L;
			assertEquals(Long.valueOf(0L), propertyAccessor.convertIfNecessary(l, Long.class));
		} else {
			fail(HibernateProxy.class.getSimpleName() + " expected");
		}
	}

	@Test
	public void testException() {
		Session session = sessionFactory.getCurrentSession();
		Application a = (Application) session.get(Application.class, 1L);
		Employee e = a.getResponsibleUser();
		LazyInitializer initializer = ((HibernateProxy) e).getHibernateLazyInitializer();
		LazyInitializerPropertyAccessor propertyAccessor = new LazyInitializerPropertyAccessor(initializer);

		try {
			propertyAccessor.getPropertyValue("");
			fail(IllegalArgumentException.class.getSimpleName() + " expected");
		} catch (IllegalArgumentException ex) {
			// expected
		}
		try {
			propertyAccessor.setPropertyValue("", null);
			fail(IllegalArgumentException.class.getSimpleName() + " expected");
		} catch (IllegalArgumentException ex) {
			// expected
		}
		try {
			propertyAccessor.getPropertyTypeDescriptor("");
			fail(InvalidPropertyException.class.getSimpleName() + " expected");
		} catch (InvalidPropertyException ex) {
			// expected
		}

		try {
			throw propertyAccessor.throwException("", null);
		} catch (InvalidPropertyException ex) {
			// expected
		}
	}
}
