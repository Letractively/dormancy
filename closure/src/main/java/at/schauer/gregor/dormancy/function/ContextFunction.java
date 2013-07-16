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

import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Applies this {@link Function} to the given {@link FunctionContext}.
 *
 * @author Gregor Schauer
 * @see Function
 * @see FunctionContext
 * @since 1.0.1
 */
public interface ContextFunction<E> extends Function<FunctionContext<E>, FunctionContext<E>> {
	/**
	 * Applies this {@link Function} to the given {@link FunctionContext}.<br/>
	 * This method is <i>generally expected</i>, but not absolutely required, to have the following properties:
	 * <p/>
	 * <ul>
	 * <li>Its execution does not cause any observable side effects.</li>
	 * <li>The returned context is the same instance as the given.</li>
	 * </ul>
	 *
	 * @param input the context to process
	 * @return the processed context
	 */
	@Nullable
	@Override
	FunctionContext<E> apply(@Nullable FunctionContext<E> input);
}
