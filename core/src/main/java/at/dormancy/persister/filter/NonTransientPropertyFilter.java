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
package at.dormancy.persister.filter;

import at.dormancy.util.ClassLookup;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.springframework.util.ReflectionUtils.*;

/**
 * Selects {@link Field}s or {@link Method}s not annotated with {@link javax.persistence.Transient}.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class NonTransientPropertyFilter implements FieldFilter, MethodFilter {
	private static final NonTransientPropertyFilter INSTANCE = new NonTransientPropertyFilter();
	private final Class<? extends Annotation> annotationType;

	public static NonTransientPropertyFilter getInstance() {
		return INSTANCE;
	}

	public NonTransientPropertyFilter() {
		annotationType = ClassLookup.find("javax.persistence.Transient")
				.orThrow("Cannot initialize %s: javax.persistence.Transient cannot be found in the classpath", getClass().getName()).get();
	}

	@Override
	public boolean matches(Field field) {
		return !field.isAnnotationPresent(annotationType);
	}

	@Override
	public boolean matches(Method method) {
		return !method.isAnnotationPresent(annotationType);
	}
}
