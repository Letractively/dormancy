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
 * The configuration for {@link at.schauer.gregor.dormancy.persister.EntityPersister EntityPersister}s.
 *
 * @author Gregor Schauer
 */
public class EntityPersisterConfiguration {
	/**
	 * Enables saving of new Hibernate entities without identifier
	 */
	private Boolean saveNewEntities;
	/**
	 * Enables version checking for Hibernate entities
	 */
	private Boolean checkVersion;
	/**
	 * Automatically flushes the current session after cloning
	 */
	private Boolean flushAutomatically;
	/**
	 * Enables cloning of objects instead of modifying them
	 */
	private Boolean cloneObjects;
	/**
	 * The parent configuration
	 */
	private EntityPersisterConfiguration parent;

	public EntityPersisterConfiguration() {
		saveNewEntities = false;
		checkVersion = true;
		flushAutomatically = false;
		cloneObjects = false;
	}

	public EntityPersisterConfiguration(@Nonnull EntityPersisterConfiguration parent) {
		this.parent = parent;
	}

	/**
	 * Returns whether new entities should be persisted automatically.
	 * <p/>
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
	 * <p/>
	 * <p>Default is {@code true}.</p>
	 *
	 * @return {@code true} if a version checking is enabled, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getCheckVersion() {
		return checkVersion == null ? parent.getCheckVersion() : checkVersion;
	}

	/**
	 * Sets whether a version check should be performed before processing the properties.
	 *
	 * @param checkVersion {@code true} if a version checking is enabled, {@code false} otherwise
	 */
	public void setCheckVersion(@Nullable Boolean checkVersion) {
		this.checkVersion = checkVersion;
	}

	/**
	 * Returns whether automatic flushing is done upon cloning objects.
	 * <p/>
	 * <p>Default is {@code false}.</p>
	 *
	 * @return {@code true} if automatic flushing is enabled, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getFlushAutomatically() {
		return flushAutomatically == null ? parent.getFlushAutomatically() : flushAutomatically;
	}

	/**
	 * Sets whether automatic flushing should be done upon cloning objects.
	 *
	 * @param flushAutomatically {@code true} if automatic flushing should be enabled, {@code false} otherwise
	 */
	public void setFlushAutomatically(@Nullable Boolean flushAutomatically) {
		this.flushAutomatically = flushAutomatically;
	}

	/**
	 * Returns whether objects are cloned instead of reused.
	 * <p/>
	 * <p>Default is {@code false}.</p>
	 *
	 * @return {@code true} if cloning is enabled, {@code false} otherwise
	 */
	@Nonnull
	public Boolean getCloneObjects() {
		return cloneObjects == null ? parent.getCloneObjects() : cloneObjects;
	}

	/**
	 * Sets whether objects should be cloned instead of reused.
	 *
	 * @param cloneObjects {@code true} if objects should be cloned, {@code false} otherwise
	 */
	public void setCloneObjects(@Nullable Boolean cloneObjects) {
		this.cloneObjects = cloneObjects;
	}
}
