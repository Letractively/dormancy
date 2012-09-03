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
package at.schauer.gregor.dormancy.function;

import org.springframework.core.CollectionFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Iterates over collections, invoking the provided {@link CollectionFunction} on every element.
 *
 * @author Gregor Schauer
 * @since 1.0.1
 */
public class CollectionFunction<E extends Collection<D>, D> extends DelegateFunction<E, D> {
	@Nullable
	@Override
	public FunctionContext<E> apply(@Nullable FunctionContext<E> input) {
		E result = createCollection(input.getObj());
		FunctionContext<D> elemContext = new FunctionContext<D>(null, input.getTree());
		for (D elem : input.getObj()) {
			elemContext.setObj(elem);
			elemContext = delegate.apply(elemContext);
			result.add(elemContext.getObj());
		}
		input.setObj(result);
		return input;
	}

	/**
	 * Creates an empty collection of the given type.
	 *
	 * @param src the original collection
	 * @return the new collection
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public E createCollection(@Nonnull Collection<?> src) {
		return (E) CollectionFactory.createApproximateCollection(src, src.size());
	}
}
