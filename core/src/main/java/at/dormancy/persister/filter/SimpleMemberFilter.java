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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Filters {@link Field}s and {@link Method}s by using the same condition.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public abstract class SimpleMemberFilter implements MemberFilter {
	@Override
	public boolean matches(@Nonnull Field field) {
		return matchesFilter(field);
	}

	@Override
	public boolean matches(@Nonnull Method method) {
		return matchesFilter(method);
	}

	/**
	 * Determine whether the given member matches.
	 * @param member the member to check
	 * @return {@code true} if the member matches the filter, {@code false} otherwise
	 */
	protected abstract <M extends AccessibleObject & Member> boolean matchesFilter(@Nonnull M member);
}
