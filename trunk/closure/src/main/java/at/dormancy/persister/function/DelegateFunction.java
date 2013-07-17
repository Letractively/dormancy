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
package at.dormancy.persister.function;

/**
 * Invokes the provided {@link ContextFunction} on the object.<br/>
 * This class exists as a convenience for internal implementations delegating to another {@link ContextFunction}.
 *
 * @author Gregor Schauer
 * @since 1.0.1
 */
public abstract class DelegateFunction<E, D> implements ContextFunction<E> {
	protected ContextFunction<D> delegate;
}
