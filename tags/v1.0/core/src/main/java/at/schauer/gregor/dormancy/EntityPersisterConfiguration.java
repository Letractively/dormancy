/*
 * Copyright 2012 Gregor Schauer
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
package at.schauer.gregor.dormancy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The configuration for {@link at.schauer.gregor.dormancy.persister.EntityPersister}s.
 *
 * @author Gregor Schauer
 */
public class EntityPersisterConfiguration {
	/**
	 * Permanently deletes removed entities from collections
	 */
	private Boolean deleteRemovedEntities;
	/**
	 * Enables saving of dirty properties of associations (similar to {@link javax.persistence.CascadeType})
	 */
	private Boolean saveAssociationsProperties;
	/**
	 * Enables saving of new Hibernate entities without identifier
	 */
	private Boolean saveNewEntities;
	/**
	 * Enables version checking for Hibernate entities
	 */
	private Boolean versionChecking;
	/**
	 * The parent configuration
	 */
	private EntityPersisterConfiguration parent;

	public EntityPersisterConfiguration() {
		this.deleteRemovedEntities = false;
		this.saveAssociationsProperties = false;
		this.saveNewEntities = false;
		this.versionChecking = true;
	}

	public EntityPersisterConfiguration(@Nonnull EntityPersisterConfiguration parent) {
		this.parent = parent;
	}

	/**
	 * Returns whether entities of deleted associated should be deleted permanently.
	 *
	 * @return {@code true} if the removed entities should be deleted, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getDeleteRemovedEntities() {
		return deleteRemovedEntities == null ? parent.getDeleteRemovedEntities() : deleteRemovedEntities;
	}

	/**
	 * Sets whether entities of deleted associated should be deleted permanently.
	 *
	 * @param deleteRemovedEntities {@code true} if the removed entities should be deleted, {@code false} otherwise
	 */
	public void setDeleteRemovedEntities(@Nullable Boolean deleteRemovedEntities) {
		this.deleteRemovedEntities = deleteRemovedEntities;
	}

	/**
	 * Returns whether properties of associated entities should be processed.
	 *
	 * @return {@code true} if the properties of associated entities should be processed, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getSaveAssociationsProperties() {
		return saveAssociationsProperties == null ? parent.getSaveAssociationsProperties() : saveAssociationsProperties;
	}

	/**
	 * Sets whether properties of associated entities should be processed.
	 *
	 * @param saveAssociationsProperties {@code true} if the properties of associated entities should be processed, {@code false} otherwise
	 */
	public void setSaveAssociationsProperties(@Nullable Boolean saveAssociationsProperties) {
		this.saveAssociationsProperties = saveAssociationsProperties;
	}

	/**
	 * Returns whether new entities should be persisted automatically.
	 *
	 * @return {@code true} if new entities should be processed, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getSaveNewEntities() {
		return saveNewEntities == null ? parent.getSaveNewEntities() : saveNewEntities;
	}

	/**
	 * Sets whether new entities should be persisted automatically.
	 *
	 * @param saveNewEntities {@code true} if new entities should be processed, {@code false} otherwise
	 */
	public void setSaveNewEntities(@Nullable Boolean saveNewEntities) {
		this.saveNewEntities = saveNewEntities;
	}

	/**
	 * Returns whether a version check should be performed before processing the properties.
	 *
	 * @return {@code true} if a version checking is enabled, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getVersionChecking() {
		return versionChecking == null ? parent.getVersionChecking() : versionChecking;
	}

	/**
	 * Sets whether a version check should be performed before processing the properties.
	 *
	 * @param versionChecking {@code true} if a version checking is enabled, {@code false} otherwise
	 */
	public void setVersionChecking(@Nullable Boolean versionChecking) {
		this.versionChecking = versionChecking;
	}
}
