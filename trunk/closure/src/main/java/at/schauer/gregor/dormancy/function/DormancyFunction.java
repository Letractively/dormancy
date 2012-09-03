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

import at.schauer.gregor.dormancy.Dormancy;

/**
 * This class exists as a convenience for internal implementations, which use {@link Dormancy}.
 *
 * @author Gregor Schauer
 * @since 1.0.1
 */
public abstract class DormancyFunction<E> implements ContextFunction<E> {
	protected Dormancy dormancy;
}