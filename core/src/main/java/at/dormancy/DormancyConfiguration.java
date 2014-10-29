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
package at.dormancy;

/**
 * The configuration for {@link Dormancy}.
 *
 * @author Gregor Schauer
 */
public class DormancyConfiguration {
	/**
	 * Enables version checking for Hibernate entities
	 */
	private boolean checkVersion = true;
	/**
	 * Automatically flushes the current persistence context after cloning
	 */
	private boolean flushAutomatically = false;
	/**
	 * Enables cloning of objects instead of modifying them
	 */
	private boolean cloneObjects = false;
	/**
	 * Attempts to create empty collections/maps for uninitialized persistent collections
	 */
	private boolean createEmptyCollections = true;

	/**
	 * Returns whether a version check should be performed before processing the properties.
	 * <p/>
	 * <p>Default is {@code true}.</p>
	 *
	 * @return {@code true} if a version checking is enabled, {@code false} otherwise
	 */
	public boolean isCheckVersion() {
		return checkVersion;
	}

	/**
	 * Sets whether a version check should be performed before processing the properties.
	 *
	 * @param checkVersion {@code true} if a version checking is enabled, {@code false} otherwise
	 */
	public void setCheckVersion(boolean checkVersion) {
		this.checkVersion = checkVersion;
	}

	/**
	 * Returns whether automatic flushing is done upon cloning objects.
	 * <p/>
	 * <p>Default is {@code false}.</p>
	 *
	 * @return {@code true} if automatic flushing is enabled, {@code false} otherwise
	 */
	public boolean isFlushAutomatically() {
		return flushAutomatically;
	}

	/**
	 * Sets whether automatic flushing should be done upon cloning objects.
	 *
	 * @param flushAutomatically {@code true} if automatic flushing should be enabled, {@code false} otherwise
	 */
	public void setFlushAutomatically(boolean flushAutomatically) {
		this.flushAutomatically = flushAutomatically;
	}

	/**
	 * Returns whether objects are cloned instead of reused.
	 * <p/>
	 * <p>Default is {@code false}.</p>
	 *
	 * @return {@code true} if cloning is enabled, {@code false} otherwise
	 */
	public boolean isCloneObjects() {
		return cloneObjects;
	}

	/**
	 * Sets whether objects should be cloned instead of reused.
	 *
	 * @param cloneObjects {@code true} if objects should be cloned, {@code false} otherwise
	 */
	public void setCloneObjects(boolean cloneObjects) {
		this.cloneObjects = cloneObjects;
	}

	/**
	 * Returns whether persistent collections are replaced with empty collections or maps or with {@code null}.
	 *
	 * @return {@code true} if empty collections should be created, {@code false} otherwise
	 */
	public boolean isCreateEmptyCollections() {
		return createEmptyCollections;
	}

	/**
	 * Sets whether persistent collections should be replaced with empty collections or maps or with {@code null}.
	 *
	 * @param createEmptyCollections {@code true} if empty collections should be created, {@code false} otherwise
	 */
	public void setCreateEmptyCollections(boolean createEmptyCollections) {
		this.createEmptyCollections = createEmptyCollections;
	}
}
