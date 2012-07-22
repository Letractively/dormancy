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

import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;

/**
 * Abstracts the notion of a "property" by using a {@link org.hibernate.property.BasicPropertyAccessor} or a
 * {@link org.hibernate.property.DirectPropertyAccessor} if not applicable.
 *
 * @author Gregor Schauer
 * @see org.hibernate.property.BasicPropertyAccessor
 * @see org.hibernate.property.DirectPropertyAccessor
 */
public class HibernatePropertyAccessor implements PropertyAccessor {
	/**
	 * @author Gregor Schauer
	 */
	private static class HibernatePropertyAccessorHolder {
		private static final HibernatePropertyAccessor instance = new HibernatePropertyAccessor();
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return the property accessor
	 */
	public static HibernatePropertyAccessor getInstance() {
		return HibernatePropertyAccessorHolder.instance;
	}

	/**
	 * @param theClass     the class containing the property
	 * @param propertyName the name of the property to retrieve a getter for
	 * @return the getter
	 * @inheritDoc
	 * @see PropertyAccessorFactory#getPropertyAccessor(String)
	 */
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

	/**
	 * @param theClass     the class containing the property
	 * @param propertyName the name of the property to retrieve a setter for
	 * @return the setter
	 * @inheritDoc
	 * @see PropertyAccessorFactory#getPropertyAccessor(String)
	 */
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
