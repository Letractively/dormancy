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

/**
 * Combines multiple {@link MemberFilter}s with <i>and</i> semantics.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class AndFilter implements MemberFilter {
	private final MemberFilter[] memberFilters;

	public AndFilter(@Nonnull MemberFilter... memberFilters) {
		this.memberFilters = memberFilters;
	}

	@Override
	public boolean matches(@Nonnull Field field) {
		for (MemberFilter memberFilter : memberFilters) {
			if (!memberFilter.matches(field)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean matches(@Nonnull Method method) {
		for (MemberFilter memberFilter : memberFilters) {
			if (!memberFilter.matches(method)) {
				return false;
			}
		}
		return true;
	}
}
