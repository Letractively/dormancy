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
	 * Skips fields declared {@code transient}
	 */
	private Boolean skipTransient;
	/**
	 * Skips fields declared {@code final}
	 */
	private Boolean skipFinal;
	/**
	 * Automatically flushes the current session after cloning
	 */
	private Boolean autoFlushing;
	/**
	 * Traverses object graphs recursively without requiring {@link at.schauer.gregor.dormancy.persister.EntityPersister}s.
	 */
	private Boolean recursiveTraversal;
	/**
	 * The parent configuration
	 */
	private EntityPersisterConfiguration parent;

	public EntityPersisterConfiguration() {
		deleteRemovedEntities = false;
		saveAssociationsProperties = false;
		saveNewEntities = false;
		versionChecking = true;
		skipTransient = false;
		skipFinal = false;
		autoFlushing = true;
		recursiveTraversal = true;
	}

	public EntityPersisterConfiguration(@Nonnull EntityPersisterConfiguration parent) {
		this.parent = parent;
	}

	/**
	 * Returns whether entities of deleted associated should be deleted permanently.
	 *
	 * <p>Default is {@code false}.</p>
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
	 * <p>Default is {@code false}.</p>
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
	 * <p>Default is {@code false}.</p>
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
	 * <p>Default is {@code true}.</p>
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

	/**
	 * Returns whether {@code transient} fields should be skipped.
	 *
	 * <p>Default is {@code false}.</p>
	 *
	 * @return {@code true} if {@code transient} fields should be skipped, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getSkipTransient() {
		return skipTransient == null ? parent.getSkipTransient() : skipTransient;
	}

	/**
	 * Sets whether {@code transient} fields should be skipped.
	 *
	 * @param skipTransient {@code true} if {@code transient} fields should be skipped, {@code false} otherwise
	 */
	public void setSkipTransient(@Nullable Boolean skipTransient) {
		this.skipTransient = skipTransient;
	}

	/**
	 * Returns whether {@code final} fields should be skipped.
	 *
	 * <p>Default is {@code false}.</p>
	 *
	 * @return {@code true} if {@code final} fields should be skipped, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getSkipFinal() {
		return skipFinal == null ? parent.getSkipFinal() : skipFinal;
	}

	/**
	 * Sets whether {@code final} fields should be skipped.
	 *
	 * @param skipFinal {@code true} if {@code final} fields should be skipped, {@code false} otherwise
	 */
	public void setSkipFinal(@Nullable Boolean skipFinal) {
		this.skipFinal = skipFinal;
	}

	/**
	 * Returns whether automatic flushing is done upon cloning objects.
	 *
	 * <p>Default is {@code true}.</p>
	 *
	 * @return {@code true} if automatic flushing is enabled, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getAutoFlushing() {
		return autoFlushing == null ? parent.getAutoFlushing() : autoFlushing;
	}

	/**
	 * Sets whether automatic flushing should be done upon cloning objects.
	 *
	 * @param autoFlushing {@code true} if automatic flushing should be enabled, {@code false} otherwise
	 */
	public void setAutoFlushing(@Nullable Boolean autoFlushing) {
		this.autoFlushing = autoFlushing;
	}

	/**
	 * Returns whether recursive traversal of object graphs is enabled.
	 *
	 * <p>Default is {@code true}.</p>
	 *
	 * @return {@code true} if recursive traversal is enabled, {@code false} otherwise
	 */
	public Boolean getRecursiveTraversal() {
		return recursiveTraversal;
	}

	/**
	 * Sets whether object graphs should be traversed recursively if no
	 * {@link at.schauer.gregor.dormancy.persister.EntityPersister} is specified.
	 *
	 * @param recursiveTraversal {@code true} if recursive traversal should be enabled, {@code false} otherwise
	 */
	public void setRecursiveTraversal(Boolean recursiveTraversal) {
		this.recursiveTraversal = recursiveTraversal;
	}
}
