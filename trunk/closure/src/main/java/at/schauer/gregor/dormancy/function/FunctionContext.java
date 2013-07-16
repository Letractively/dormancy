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
package at.schauer.gregor.dormancy.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Holds context sensitive variables for stateless {@link ContextFunction}s.<br/>
 * This implementations holds:
 * <ul>
 * <li>a value that represents the parameter and the last function result</li>
 * <li>an adjacency stack that keeps track of the function invocations</li>
 * </ul>
 *
 * @author Gregor Schauer
 * @since 1.0.1
 */
public class FunctionContext<T> {
	private T obj;
	private Map<Object, Object> tree;

	public FunctionContext() {
		this(null);
	}

	public FunctionContext(@Nullable T obj) {
		this(obj, new IdentityHashMap<Object, Object>());
	}

	public FunctionContext(@Nullable T obj, @Nonnull Map<Object, Object> tree) {
		this.obj = obj;
		this.tree = tree;
	}

	/**
	 * Returns the value associated with this context.
	 *
	 * @return the value
	 */
	@Nullable
	public T getObj() {
		return obj;
	}

	/**
	 * Sets the value associated with this context.
	 *
	 * @param obj the value
	 */
	public void setObj(@Nullable T obj) {
		this.obj = obj;
	}

	/**
	 * Returns the adjacency stack built by function invocations.
	 *
	 * @return the adjacency stack
	 */
	@Nonnull
	public Map<Object, Object> getTree() {
		return tree;
	}
}
