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

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Selects {@link Field}s or {@link Method}s, which are {@code static final}.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class StaticFinalFieldFilter implements MemberFilter {
	private static final StaticFinalFieldFilter INSTANCE = new StaticFinalFieldFilter();

	@Nonnull
	public static StaticFinalFieldFilter getInstance() {
		return INSTANCE;
	}

	private StaticFinalFieldFilter() {
	}

	@Override
	public boolean matches(@Nonnull Field field) {
		return Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers());
	}

	@Override
	public boolean matches(@Nonnull Method method) {
		return Modifier.isStatic(method.getModifiers()) && Modifier.isFinal(method.getModifiers());
	}
}
