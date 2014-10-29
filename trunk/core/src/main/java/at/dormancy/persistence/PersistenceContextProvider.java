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
package at.dormancy.persistence;

import javax.annotation.Nonnull;

/**
 * Interface used to access the entity manager for the persistence unit.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public interface PersistenceContextProvider<PC> {
	/**
	 * Returns the current persistence context.
	 *
	 * @return the persistence context
	 */
	@Nonnull
	PC getPersistenceContext();
}
