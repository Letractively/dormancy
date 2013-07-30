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

import at.dormancy.entity.Employee;
import org.junit.Test;
import org.springframework.core.convert.TypeDescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class StrategyPropertyAccessorTest {
	@Test
	public void test() {
		Employee entity = new Employee();
		entity.setId(1L);

		HibernatePropertyAccessStrategy strategy = new HibernatePropertyAccessStrategy(entity.getClass());
		StrategyPropertyAccessor propertyAccessor = new StrategyPropertyAccessor(entity, strategy);

		assertSame(entity, propertyAccessor.convertIfNecessary(entity, Object.class));
		assertSame(entity, propertyAccessor.convertIfNecessary(entity, Employee.class));

		assertEquals(Long.class, propertyAccessor.getPropertyType("id"));
		assertEquals(entity.getId(), propertyAccessor.getPropertyValue("id"));

		assertEquals(true, propertyAccessor.getPropertyTypeDescriptor("id").isAssignableTo(TypeDescriptor.valueOf(Number.class)));

		Object fieldVal = propertyAccessor.getFieldAccessor().getPropertyValue("id");
		Object propertyValue = propertyAccessor.getPropertyAccessor().getPropertyValue("id");
		assertEquals(fieldVal, propertyValue);
	}

	@Test
	public void testIsProperty() throws Exception {
		HibernatePropertyAccessStrategy strategy = new HibernatePropertyAccessStrategy(Class.class);
		StrategyPropertyAccessor propertyAccessor = new StrategyPropertyAccessor(Object.class, strategy);

		assertEquals(false, propertyAccessor.isReadableProperty("class"));
		assertEquals(false, propertyAccessor.isWritableProperty("class"));

		assertEquals(true, propertyAccessor.isReadableProperty("name"));
		assertEquals(true, propertyAccessor.isWritableProperty("name"));
	}
}
