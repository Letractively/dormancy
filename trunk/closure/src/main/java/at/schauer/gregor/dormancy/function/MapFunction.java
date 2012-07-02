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
import java.util.Map;

/**
 * Iterates over collections, invoking the provided {@link CollectionFunction}s on every key and element.
 *
 * @author Gregor Schauer
 * @since 1.0.1
 */
public class MapFunction<K, V, E extends Map<K, V>> implements ContextFunction<E> {
	protected ContextFunction<K> keyDelegate;
	protected ContextFunction<V> valueDelegate;

	@Nullable
	@Override
	public FunctionContext<E> apply(@Nullable FunctionContext<E> input) {
		E result = createMap(input.getObj());
		FunctionContext<K> keyContext = new FunctionContext<K>(null, input.getTree());
		FunctionContext<V> valueContext = new FunctionContext<V>(null, input.getTree());

		for (Map.Entry<K, V> element : input.getObj().entrySet()) {
			keyContext.setObj(element.getKey());
			if (keyDelegate != null) {
				keyContext = keyDelegate.apply(keyContext);
			}
			K key = keyContext.getObj();

			valueContext.setObj(element.getValue());
			if (valueDelegate != null) {
				valueContext = valueDelegate.apply(valueContext);
			}
			V value = valueContext.getObj();
			result.put(key, value);
		}
		input.setObj(result);
		return input;
	}

	/**
	 * Creates an empty map of the given type.
	 *
	 * @param src the original map
	 * @return the new map
	 * @see CollectionFactory#createApproximateMap(Object, int)
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public E createMap(@Nonnull Map src) {
		return (E) CollectionFactory.createApproximateMap(src, src.size());
	}
}
