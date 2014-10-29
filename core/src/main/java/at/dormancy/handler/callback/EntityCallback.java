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
package at.dormancy.handler.callback;

import at.dormancy.Dormancy;
import at.dormancy.persistence.PersistenceUnitProvider;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Callback interface for JPA code.
 *
 * @param <T>   the type of the object to fetch
 * @param <PU>  the type of the persistence unit to use
 * @param <PC>  the type of the persistence context to use
 * @param <PMD> the type of the persistence metadata to use
 * @author Gregor Schauer
 * @since 2.0.0
 */
public interface EntityCallback<T, PU, PC, PMD> {
	/**
	 * Gets called by {@link Dormancy#apply(Object, EntityCallback)} with an active persistence context.<br/>
	 * It does not need to care about activating or closing it, or handling transactions.
	 *
	 * @param persistenceUnitProvider holds the persistence unit and the current context
	 * @return the result of the invocation
	 */
	@Nullable
	@Transactional
	T work(@Nonnull PersistenceUnitProvider<PU, PC, PMD> persistenceUnitProvider);
}
