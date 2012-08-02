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
package at.schauer.gregor.dormancy.persister;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Abstract implementation containing some convenience methods.
 *
 * @author Gregor Schauer
 */
public abstract class AbstractEntityPersister<C> implements EntityPersister<C> {
	@Nullable
	@Override
	public <T extends C> C clone(@Nullable T dbObj) {
		return clone_(dbObj, createAdjacencyMap());
	}

	/**
	 * Clones the given object.
	 *
	 * @param dbObj the object to clone
	 * @param tree  the adjacency map to use for traversal
	 * @return the cloned object
	 */
	@Nullable
	public abstract <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree);

	@Nullable
	@Override
	public <T extends C> C merge(@Nullable T trObj) {
		return merge_(trObj, createAdjacencyMap());
	}

	/**
	 * Merges the given object into the current {@link org.hibernate.Session}.
	 *
	 * @param trObj the object to merge
	 * @param tree  the adjacency map to use for traversal
	 * @return the merged object
	 */
	@Nullable
	public abstract <T extends C> C merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree);

	@Nullable
	@Override
	public <T extends C> C merge(@Nullable T trObj, @Nullable T dbObj) {
		return merge_(trObj, dbObj, createAdjacencyMap());
	}

	/**
	 * Merges the given transient object with the persistent object.
	 *
	 * @param trObj the transient object
	 * @param dbObj the persistent object
	 * @param tree  the adjacency map to use for traversal
	 * @return the persistent object with applied property changes
	 */
	@Nullable
	public abstract <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree);

	/**
	 * Creates an empty adjacency map used for traversing object graphs.
	 *
	 * @return the adjacency map
	 */
	@Nonnull
	protected <K, V> Map<K, V> createAdjacencyMap() {
		return new IdentityHashMap<K, V>();
	}

	/**
	 * The types of objects supported by this implementation.
	 *
	 * @return the supported types
	 */
	@Nullable
	public Class<?>[] getSupportedTypes() {
		return null;
	}
}