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

import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;

/**
 * @author Gregor Schauer
 */
public class HibernatePropertyAccessor implements PropertyAccessor {
	@Override
	public Getter getGetter(Class theClass, String propertyName) {
		try {
			PropertyAccessor propertyAccessor = PropertyAccessorFactory.getPropertyAccessor(null);
			return propertyAccessor.getGetter(theClass, propertyName);
		} catch (PropertyNotFoundException ignored) {
			PropertyAccessor directAccessor = PropertyAccessorFactory.getPropertyAccessor("field");
			return directAccessor.getGetter(theClass, propertyName);
		}
	}

	@Override
	public Setter getSetter(Class theClass, String propertyName) {
		try {
			PropertyAccessor propertyAccessor = PropertyAccessorFactory.getPropertyAccessor(null);
			return propertyAccessor.getSetter(theClass, propertyName);
		} catch (PropertyNotFoundException ignored) {
			PropertyAccessor directAccessor = PropertyAccessorFactory.getPropertyAccessor("field");
			return directAccessor.getSetter(theClass, propertyName);
		}
	}
}
