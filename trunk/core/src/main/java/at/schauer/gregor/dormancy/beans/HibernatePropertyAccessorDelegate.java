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

import org.hibernate.property.Getter;
import org.hibernate.property.Setter;
import org.springframework.beans.AbstractPropertyAccessor;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;

/**
 * Spring {@link org.springframework.beans.PropertyAccessor} that uses a {@link HibernatePropertyAccessor} for
 * retrieving entity properties.
 *
 * @author Gregor Schauer
 * @see HibernatePropertyAccessor
 */
public class HibernatePropertyAccessorDelegate extends AbstractPropertyAccessor {
	private final HibernatePropertyAccessor hibernatePropertyAccessor;
	private final Object target;

	public HibernatePropertyAccessorDelegate(HibernatePropertyAccessor hibernatePropertyAccessor, Object target) {
		this.hibernatePropertyAccessor = hibernatePropertyAccessor;
		this.target = target;
	}

	/**
	 * @param propertyName the name of the property to retrieve the value for
	 * @return the extracted value
	 * @inheritDoc
	 * @see #getGetter(String)
	 * @see Getter#get(Object)
	 */
	@Override
	public Object getPropertyValue(String propertyName) {
		return getGetter(propertyName).get(target);
	}

	/**
	 * @param propertyName the name of the property to set value of
	 * @param value        the new value
	 * @inheritDoc
	 * @see #getSetter(String)
	 * @see Setter#set(Object, Object, org.hibernate.engine.SessionFactoryImplementor)
	 */
	@Override
	public void setPropertyValue(String propertyName, Object value) {
		getSetter(propertyName).set(target, value, null);
	}

	/**
	 * @param propertyName the name of the property to retrieve a getter for
	 * @return whether the property is readable
	 * @inheritDoc
	 * @see #getGetter(String)
	 */
	@Override
	public boolean isReadableProperty(String propertyName) {
		return getGetter(propertyName) != null;
	}

	/**
	 * @param propertyName the name of the property to retrieve a setter for
	 * @return whether the property is writable
	 * @inheritDoc
	 * @see #getSetter(String)
	 */
	@Override
	public boolean isWritableProperty(String propertyName) {
		return getSetter(propertyName) != null;
	}

	@Override
	public TypeDescriptor getPropertyTypeDescriptor(String propertyName) {
		try {
			return TypeDescriptor.valueOf(getGetter(propertyName).getReturnType());
		} catch (Exception ignored) {
			return TypeDescriptor.valueOf(getSetter(propertyName).getMethod().getParameterTypes()[0]);
		}
	}

	@Override
	public <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam) {
		return requiredType.cast(value);
	}

	/**
	 * Returns the getter of the given property.
	 *
	 * @param propertyName the name of the property to retrieve a getter for
	 * @return the getter
	 * @see HibernatePropertyAccessor#getGetter(Class, String)
	 */
	protected Getter getGetter(String propertyName) {
		return hibernatePropertyAccessor.getGetter(target.getClass(), propertyName);
	}

	/**
	 * Returns the setter of the given property.
	 *
	 * @param propertyName the name of the property to retrieve a setter for
	 * @return the setter
	 * @see HibernatePropertyAccessor#getSetter(Class, String)
	 */
	protected Setter getSetter(String propertyName) {
		return hibernatePropertyAccessor.getSetter(target.getClass(), propertyName);
	}
}
