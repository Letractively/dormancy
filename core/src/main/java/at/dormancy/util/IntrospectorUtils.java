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
package at.dormancy.util;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Utility class that simplifies bean introspection and caches {@link PropertyDescriptor}s for improved performance.</p>
 * <p/>
 * There are some alternative solutions e.g.,
 * <ul>
 * <li>Spring´s {@link org.springframework.beans.CachedIntrospectionResults CachedIntrospectionResults} that is a
 * little bit slower but provides more features. Note that it is NOT intended for direct use by application code.</li>
 * <li>Apache´s {@link org.apache.commons.beanutils.PropertyUtils PropertyUtils}, which are comparable to
 * {@code CachedIntrospectionResults}</li>
 * <li>Spring´s {@link org.springframework.beans.ConfigurablePropertyAccessor ConfigurablePropertyAccessor} and
 * {@link org.springframework.beans.BeanWrapper BeanWrapper} do not use {@link PropertyDescriptor}s but also provide a
 * convenient way for accessing bean properties</li>
 * </ul>
 *
 * @author Gregor Schauer
 * @since 1.1.0
 */
public class IntrospectorUtils {
	protected static Map<Class<?>, Map<String, PropertyDescriptor>> beanInfoMap
			= new IdentityHashMap<Class<?>, Map<String, PropertyDescriptor>>();

	private IntrospectorUtils() {
	}

	/**
	 * Returns the value of the specified property of the specified bean.
	 *
	 * @param bean         bean whose property is to be extracted
	 * @param propertyName propertyName the propertyName of the property to get the value of
	 * @return the value of the property
	 * @see #getFieldValue(Object, String)
	 * @see java.beans.PropertyDescriptor#getReadMethod()
	 */
	@Nullable
	public static Object getValue(@Nonnull Object bean, @Nonnull String propertyName) {
		PropertyDescriptor descriptor = getDescriptorMap(bean.getClass()).get(propertyName);
		Method readMethod = descriptor != null ? descriptor.getReadMethod() : null;
		if (readMethod != null) {
			return ReflectionUtils.invokeMethod(readMethod, bean);
		} else {
			return getFieldValue(bean, propertyName);
		}
	}

	/**
	 * Returns the value of the specified property of the specified bean by accessing it directly.
	 *
	 * @param bean         bean whose property is to be extracted
	 * @param propertyName the propertyName of the property to get the value of
	 * @return the value of the property
	 * @see ReflectionTestUtils#getField(Object, String)
	 */
	@Nonnull
	protected static Object getFieldValue(@Nonnull Object bean, @Nonnull String propertyName) {
		return ReflectionTestUtils.getField(bean, propertyName);
	}

	/**
	 * Sets the value of the specified property of the specified bean.
	 *
	 * @param bean         bean whose property is to be modified
	 * @param propertyName propertyName of the property to be modified
	 * @param value        value to which this property is to be set
	 * @see #setFieldValue(Object, String, Object)
	 * @see java.beans.PropertyDescriptor#getWriteMethod()
	 */
	public static void setValue(@Nonnull Object bean, @Nonnull String propertyName, @Nullable Object value) {
		PropertyDescriptor descriptor = getDescriptorMap(bean.getClass()).get(propertyName);
		Method writeMethod = descriptor != null ? descriptor.getWriteMethod() : null;
		if (writeMethod != null) {
			ReflectionUtils.invokeMethod(writeMethod, bean, value);
		} else {
			setFieldValue(bean, propertyName, value);
		}
	}

	/**
	 * Sets the value of the specified property of the specified bean.
	 *
	 * @param bean         bean whose property is to be set
	 * @param propertyName the propertyName of the property to set the value of
	 * @param value        the new value
	 * @see ReflectionTestUtils#setField(Object, String, Object)
	 */
	protected static void setFieldValue(@Nonnull Object bean, @Nonnull String propertyName, @Nullable Object value) {
		ReflectionTestUtils.setField(bean, propertyName, value);
	}

	/**
	 * Returns a map containing the property names and their corresponding property descriptors.<p/>
	 * <p/>
	 * Due to the fact that {@link #getClass()} is a valid getter method, the method
	 * {@link java.beans.BeanInfo#getPropertyDescriptors()} returns a property descriptor for {@code class}.
	 * This method does NOT return the property {@code class}.
	 *
	 * @param clazz the class to retrieve its property descriptors
	 * @return the property descriptors of the class
	 * @see #getDescriptors(Class)
	 */
	@Nonnull
	protected static Map<String, PropertyDescriptor> getDescriptorMap(@Nonnull Class<?> clazz) {
		Map<String, PropertyDescriptor> descriptorMap = beanInfoMap.get(clazz);
		if (descriptorMap == null) {
			descriptorMap = new HashMap<String, PropertyDescriptor>();
			beanInfoMap.put(clazz, descriptorMap);

			for (PropertyDescriptor descriptor : getDescriptors(clazz)) {
				if (!"class".equals(descriptor.getName())) {
					descriptorMap.put(descriptor.getName(), descriptor);
				}
			}
		}
		return descriptorMap;
	}

	/**
	 * Returns the property descriptors of the given class.</p>
	 * <p/>
	 * This is just a convenient method that catches the {@link IntrospectionException} and rethrows it as an unchecked
	 * exception.
	 *
	 * @param clazz the class to retrieve its property descriptors
	 * @return the property descriptors of the class
	 * @see Introspector#getBeanInfo(Class)
	 * @see java.beans.BeanInfo#getPropertyDescriptors()
	 */
	@Nonnull
	private static PropertyDescriptor[] getDescriptors(@Nonnull Class<?> clazz) {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			return beanInfo.getPropertyDescriptors();
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}
}
