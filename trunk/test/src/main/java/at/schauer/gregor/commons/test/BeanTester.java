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
package at.schauer.gregor.commons.test;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Gregor Schauer
 */
public class BeanTester {
	public static enum Mode {
		TO_STRING, EQUALS_HASHCODE, GETTER, SETTER, ALL
	}
	private static BeanTester instance = new BeanTester();

	public static BeanTester getInstance() {
		return instance;
	}

	private BeanTester() {
	}

	public <T> T instantiate(Class<T> beanClass) {
		try {
			return beanClass == null || Modifier.isAbstract(beanClass.getModifiers())
					|| beanClass.getDeclaredConstructor() == null
					? null : BeanUtils.instantiateClass(beanClass);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	public void test(Object obj, Mode... modes) throws InvocationTargetException, IllegalAccessException {
		if (obj == null) {
			return;
		}
		modes = modes != null && modes.length > 0 ? modes : new Mode[]{Mode.ALL};
		for (Mode mode : modes) {
			if (mode.ordinal() >= Mode.GETTER.ordinal()) {
				PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(obj);
				for (PropertyDescriptor descriptor : descriptors) {
					// read (if possible)
					Method readMethod = descriptor.getReadMethod();
					Object value = (mode == Mode.GETTER || mode == Mode.ALL) && readMethod != null
							? readMethod.invoke(obj) : defaultValue(obj, descriptor);
					// write (if possible)
					Method writeMethod = descriptor.getWriteMethod();
					if ((mode == Mode.SETTER || mode == Mode.ALL) && writeMethod != null) {
						writeMethod.invoke(obj, value);
					}
				}
			}
			if (mode == Mode.TO_STRING || mode == Mode.ALL) {
				obj.toString();
			}
			if (mode == Mode.EQUALS_HASHCODE || mode == Mode.ALL) {
				obj.equals(null);
				obj.equals(obj);
				obj.hashCode();
			}
		}
	}

	public Object defaultValue(Object obj, PropertyDescriptor descriptor) {
		Class<?> returnType = descriptor.getReadMethod().getReturnType();
		if (returnType.isPrimitive()) {
			return returnType == boolean.class ? false : (byte) 0;
		}
		return null;
	}
}
