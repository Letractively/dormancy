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
import at.schauer.gregor.dormancy.entity.Book;
import org.junit.Test;

import static at.schauer.gregor.dormancy.access.AbstractPropertyAccessStrategy.AccessMode.FIELD;
import static at.schauer.gregor.dormancy.access.AbstractPropertyAccessStrategy.AccessMode.PROPERTY;
import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
public class PropertyAccessStrategyTest {
	@Test
	public void testApplication() {
		AnnotationPropertyAccessStrategy strategy = new JpaAccessTypeStrategy(Application.class);
		assertEquals(PROPERTY, strategy.getDefaultAccessMode());
		assertEquals(PROPERTY, strategy.getAccessMode("id"));
		assertEquals(FIELD, strategy.getAccessMode("lastUpdate"));
	}

	@Test
	public void testBook() {
		AnnotationPropertyAccessStrategy strategy = new JpaAccessTypeStrategy(Book.class);
		assertEquals(FIELD, strategy.getAccessMode("id"));
		assertEquals(FIELD, strategy.getAccessMode("title"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidProperty() {
		AnnotationPropertyAccessStrategy strategy = new JpaAccessTypeStrategy(Book.class);
		strategy.getAccessMode("");
	}
}
