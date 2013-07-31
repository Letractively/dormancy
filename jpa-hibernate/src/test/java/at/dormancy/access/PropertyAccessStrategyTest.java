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
import at.dormancy.entity.Book;
import org.junit.Test;

import static at.dormancy.access.AbstractPropertyAccessStrategy.AccessType.FIELD;
import static at.dormancy.access.AbstractPropertyAccessStrategy.AccessType.PROPERTY;
import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class PropertyAccessStrategyTest {
	@Test
	public void testApplication() {
		AnnotationPropertyAccessStrategy strategy = new JpaPropertyAccessStrategy(Application.class);
		assertEquals(PROPERTY, strategy.getDefaultAccessType());
		assertEquals(PROPERTY, strategy.getAccessType("id"));
		assertEquals(FIELD, strategy.getAccessType("lastUpdate"));
	}

	@Test
	public void testBook() {
		AnnotationPropertyAccessStrategy strategy = new JpaPropertyAccessStrategy(Book.class);
		assertEquals(FIELD, strategy.getAccessType("id"));
		assertEquals(FIELD, strategy.getAccessType("title"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidProperty() {
		AnnotationPropertyAccessStrategy strategy = new JpaPropertyAccessStrategy(Book.class);
		strategy.getAccessType("");
	}
}
