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

import org.hibernate.proxy.LazyInitializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Simply returns {@code null}.
 *
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class NullPersister<C> extends AbstractEntityPersister<C> implements DynamicEntityPersister<C> {
	protected static class NullPersisterHolder {
		protected static final NullPersister instance = new NullPersister();
	}

	@SuppressWarnings("unchecked")
	public static <C> NullPersister<C> getInstance() {
		return NullPersisterHolder.instance;
	}

	/**
	 * Returns {@code null}.
	 *
	 * @param dbObj the object to clone
	 * @param tree  the adjacency map to use for traversal
	 * @return {@code null}
	 */
	@Override
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		return null;
	}

	/**
	 * Returns {@code null}.
	 *
	 * @param trObj the object to merge
	 * @param tree  the adjacency map to use for traversal
	 * @return {@code null}
	 */
	@Override
	public <T extends C> C merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		return null;
	}

	/**
	 * Returns {@code null}.
	 *
	 * @param trObj the transient object
	 * @param dbObj the persistent object
	 * @param tree  the adjacency map to use for traversal
	 * @return {@code null}
	 */
	@Override
	public <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		return null;
	}

	@Override
	public boolean supports(@Nonnull Class<?> clazz) {
		return LazyInitializer.class.isAssignableFrom(clazz);
	}
}
