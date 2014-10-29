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
import javax.annotation.Nullable;

/**
 * Interface used to access the entity manager factory for the persistence unit.
 *
 * @param <PU>  the type of the persistence unit to use
 * @param <PC>  the type of the persistence context to use
 * @param <PMD> the type of the persistence metadata to use
 * @author Gregor Schauer
 * @since 2.0.0
 */
public interface PersistenceUnitProvider<PU, PC, PMD> {
	/**
	 * Returns the persistence unit used for accessing the database.
	 *
	 * @return the persistence unit
	 */
	@Nonnull
	PU getPersistenceUnit();

	/**
	 * Returns a {@link PersistenceContextProvider} holding the current persistence context.
	 *
	 * @return a persistence context provider
	 */
	@Nonnull
	PersistenceContextProvider<PC> getPersistenceContextProvider();

	/**
	 * Returns the metadata for the given persistent class.
	 *
	 * @param clazz the class to retrieve metadata for
	 * @return the persistence metadata of the class or {@code null} if the class is not a managed entity
	 */
	@Nullable
	PMD getMetadata(@Nonnull Class<?> clazz);
}
