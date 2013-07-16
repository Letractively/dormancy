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
package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.EntityPersisterConfiguration;
import at.schauer.gregor.dormancy.persistence.PersistenceUnitProvider;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Processes non Hibernate managed objects including wrappers and containers such as collections.
 *
 * @author Gregor Schauer
 */
public abstract class AbstractContainerPersister<C> extends AbstractEntityPersister<C> {
	protected PersistenceUnitProvider<?, ?, ?> persistenceUnitProvider;
	protected Dormancy<?, ?, ?> dormancy;
	protected EntityPersisterConfiguration config;

	@Inject
	protected AbstractContainerPersister(@Nonnull Dormancy dormancy) {
		this.dormancy = dormancy;
		this.config = new EntityPersisterConfiguration(dormancy.getConfig());
	}

	/**
	 * Sets the {@code PersistenceUnitProvider} that should be used to create persistence contexts.
	 *
	 * @param persistenceUnitProvider the persistence unit provider to use
	 */
	@Inject
	public void setPersistentUnitProvider(@Nonnull PersistenceUnitProvider<?, ?, ?> persistenceUnitProvider) {
		this.persistenceUnitProvider = persistenceUnitProvider;
	}

	/**
	 * Returns the {@code EntityPersisterConfiguration} that should be used.
	 *
	 * @return the configuration to use
	 */
	@Nonnull
	public EntityPersisterConfiguration getConfig() {
		return config;
	}

	/**
	 * Sets the {@code EntityPersisterConfiguration} that should be used.
	 *
	 * @param config the configuration to use
	 */
	public void setConfig(@Nonnull EntityPersisterConfiguration config) {
		this.config = config;
	}

	/**
	 * Creates an empty container of the given type.
	 *
	 * @param container the original container
	 * @return the new container
	 */
	@Nonnull
	protected abstract C createContainer(@Nonnull C container);
}
