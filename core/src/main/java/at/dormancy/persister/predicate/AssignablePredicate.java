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
package at.dormancy.persister.predicate;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * {@link Predicate} implementation that returns {@code true} if the input may be assigned to at least one of the types
 * stored in this {@link Predicate}.
 *
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class AssignablePredicate implements Predicate, Serializable {
	protected Class<?>[] types;

	public AssignablePredicate(@Nullable Class<?>... types) {
		this.types = types;
	}

	/**
	 * @return {@code true} if the parameter is assignable to at least one of the given types, {@code false} otherwise
	 * @see ClassUtils#isAssignable(Class, Class)
	 */
	@Override
	public boolean evaluate(@Nonnull Object object) {
		Class<?> clazz = object.getClass();
		for (Class<?> supportedType : types) {
			if (ClassUtils.isAssignable(supportedType, clazz)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns an array of the types used for testing.
	 *
	 * @return the types
	 */
	@Nonnull
	public Class<?>[] getTypes() {
		return types != null ? types : ArrayUtils.EMPTY_CLASS_ARRAY;
	}

	/**
	 * Sets the types to use for testing.
	 *
	 * @param types the types
	 */
	public void setTypes(@Nullable Class<?>... types) {
		this.types = types;
	}
}
