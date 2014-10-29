/*
 * Copyright 2014 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.handler;

import com.google.common.base.Predicate;

import javax.annotation.Nonnull;

/**
 * Implementors of this interface can decide dynamically for which types this {@link ObjectHandler} can be applied to.
 *
 * @author Gregor Schauer
 * @since 1.0.3
 */
public interface DynamicObjectHandler<C> extends ObjectHandler<C> {
	/**
	 * Creates a {@link Predicate} for checking if a certain object type is supported by this {@code ObjectHandler}.
	 *
	 * @return the predicate
	 */
	@Nonnull
	Predicate<Class<?>> getPredicate();
}
