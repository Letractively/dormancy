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
package at.schauer.gregor.dormancy.util;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Book;
import org.hibernate.metadata.ClassMetadata;
import org.junit.Test;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.PropertyAccessor;

import static org.junit.Assert.assertTrue;

/**
 * @author Gregor Schauer
 */
public class DormancyUtilsTest extends AbstractDormancyTest {
	@Test
	public void testGetPropertyAccessor() {
		checkPropertyAccessor(new Object(), DirectFieldAccessor.class);

		// If class metadata is available, Dormancy uses the same access strategy as Hibernate.
		checkPropertyAccessor(new Book(), DirectFieldAccessor.class);
		checkPropertyAccessor(new Application(), BeanWrapperImpl.class);

		// If no class metadata is available, the field access strategy is used
		checkPropertyAccessor(new Book(), DirectFieldAccessor.class, null);
		checkPropertyAccessor(new Application(), DirectFieldAccessor.class, null);
	}

	void checkPropertyAccessor(Object obj, Class<? extends PropertyAccessor> accessorType) {
		checkPropertyAccessor(obj, accessorType, dormancy.getUtils().getClassMetadata(obj, sessionFactory));
	}

	void checkPropertyAccessor(Object obj, Class<? extends PropertyAccessor> accessorType, ClassMetadata metadata) {
		PropertyAccessor propertyAccessor = dormancy.getUtils().getPropertyAccessor(metadata, obj);
		assertTrue(accessorType.isAssignableFrom(propertyAccessor.getClass()));
	}
}
