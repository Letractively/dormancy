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

import javax.annotation.Nullable;

/**
 * Uses {@link at.schauer.gregor.dormancy.Dormancy} to clone the objects.
 *
 * @author Gregor Schauer
 * @see at.schauer.gregor.dormancy.Dormancy#merge_(Object, java.util.Map)
 * @since 1.0.1
 */
public class DormancyMergeFunction<E> extends DormancyFunction<E> {
	@Nullable
	@Override
	public FunctionContext<E> apply(@Nullable FunctionContext<E> input) {
		if (input != null) {
			input.setObj(dormancy.merge_(input.getObj(), input.getTree()));
		}
		return input;
	}
}