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

import at.schauer.gregor.dormancy.Dormancy;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.springframework.core.CollectionFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Gregor Schauer
 */
public class FunctionPersister<E> extends AbstractContainerPersister<Iterable<E>> {
	@Inject
	public FunctionPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Iterable<E>> Iterable<E> clone_(@Nullable T dbObj, @Nonnull final Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (Iterable<E>) tree.get(dbObj);
		}
		Collection<E> container = createContainer(dbObj);
		tree.put(dbObj, container);

		Iterables.addAll(container, Iterables.transform(dbObj, getCloneFunction(dbObj, tree)));
		return container;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Iterable<E>> Iterable<E> merge_(@Nullable T trObj, @Nonnull final Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (Iterable<E>) tree.get(trObj);
		}
		Collection<E> container = createContainer(trObj);
		tree.put(trObj, container);

		Iterables.addAll(container, Iterables.transform(trObj, getMergeFunction(trObj, tree)));
		return container;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Iterable<E>> Iterable<E> merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull final Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (Iterable<E>) tree.get(trObj);
		}
		Collection<E> container = createContainer(trObj);
		tree.put(trObj, container);

		Iterables.addAll(container, Iterables.transform(trObj, getMergeFunction(trObj, dbObj, tree)));
		return container;
	}

	public <T extends Iterable<E>> Function<E, E> getCloneFunction(@Nonnull T dbObj, @Nonnull final Map<Object, Object> tree) {
		return new Function<E, E>() {
			@Override
			public E apply(@Nullable E input) {
				return dormancy.clone_(input, tree);
			}
		};
	}

	public <T extends Iterable<E>> Function<E, E> getMergeFunction(@Nonnull T trObj, @Nonnull final Map<Object, Object> tree) {
		return new Function<E, E>() {
			@Override
			public E apply(@Nullable E input) {
				return dormancy.clone_(input, tree);
			}
		};
	}

	public <T extends Iterable<E>> Function<E, E> getMergeFunction(@Nonnull T trObj, T dbObj, @Nonnull final Map<Object, Object> tree) {
		final Iterator<E> iterator = dbObj.iterator();
		return new Function<E, E>() {
			@Override
			public E apply(@Nullable E input) {
				return dormancy.merge_(input, iterator.next(), tree);
			}
		};
	}

	/**
	 * Creates an empty container of the given type.
	 *
	 * @param container the original container
	 * @return the new container
	 * @see CollectionFactory#createApproximateCollection(Object, int)
	 */
	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	protected Collection<E> createContainer(@Nonnull Iterable<E> container) {
		return CollectionFactory.createApproximateCollection(container, Iterables.size(container));
	}
}
