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
package at.schauer.gregor.dormancy.persister.callback;

import at.schauer.gregor.dormancy.persistence.PersistenceUnitProvider;
import org.springframework.transaction.annotation.Transactional;

/**
 * Callback interface for JPA code.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public interface EntityCallback<T, PU, PC, PMD> {
	/**
	 * Gets called by {@link at.schauer.gregor.dormancy.Dormancy#merge(Object, EntityCallback)} with an active
	 * persistence context.<br/>
	 * It does not need to care about activating or closing it, or handling transactions.
	 *
	 * @param persistenceUnitProvider holds the persistence unit and the current context
	 * @return the result of the invocation
	 */
	@Transactional
	T work(PersistenceUnitProvider<PU, PC, PMD> persistenceUnitProvider);
}
