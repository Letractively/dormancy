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

import javax.annotation.Nullable;

/**
 * Contains operations for cloning Hibernate managed objects and merging them into a persistence context.
 * <p/>
 * The term cloning includes any operations required for creating an object graph which is a copy of the given object
 * and its associations. It is neither guaranteed that the types of the constructed objects is the same as the types of
 * the given objects nor its data is exactly the same. In other words, the object graph may be altered (if necessary)
 * but the semantics of the properties must not change.<br/>
 * Merging means to create and/or load Hibernate managed entities which are pendents of the given objects and applying
 * any semantic value changes to them.
 * <p/>
 * It is recommended that merging a previously cloned entity into a persistence context returns the entity
 * without modifications e.g., {@code entityPersister.merge(entityPersister.clone(anObject)).equals(anObject)} results
 * to {@code true}.
 * <p/>
 * Implementors must be threadsafe (preferably immutable).
 *
 * @author Gregor Schauer
 */
public interface EntityPersister<C> {
	/**
	 * Clones the given object.
	 *
	 * @param dbObj the object to clone
	 * @return the cloned object
	 */
	@Nullable
	<T extends C> C clone(@Nullable T dbObj);

	/**
	 * Merges the given object into the current persistence context.
	 *
	 * @param trObj the object to merge
	 * @return the merged object
	 */
	@Nullable
	<T extends C> C merge(@Nullable T trObj);

	/**
	 * Merges the given transient object with the persistent object.
	 *
	 * @param trObj the transient object
	 * @param dbObj the persistent object
	 * @return the persistent object with applied property changes
	 */
	@Nullable
	<T extends C> C merge(@Nullable T trObj, @Nullable T dbObj);
}
