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
package at.dormancy.persister;

import javax.annotation.Nonnull;

/**
 * Dynamically decides if a certain object type can be persisted.
 *
 * @author Gregor Schauer
 * @since 1.0.3
 */
public interface DynamicEntityPersister<C> extends EntityPersister<C> {
	/**
	 * Checks if the given object type is supported by this instance
	 *
	 * @param clazz the object type
	 * @return {@code true} if the type is supported, {@code false} otherwise
	 */
	boolean supports(@Nonnull Class<?> clazz);
}
