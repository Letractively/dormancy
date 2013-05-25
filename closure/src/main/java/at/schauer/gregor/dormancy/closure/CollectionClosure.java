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
package at.schauer.gregor.dormancy.closure;

import java.util.Collection;

/**
 * @author Gregor Schauer
 */
public abstract class CollectionClosure<S extends Collection, T extends Collection> extends DelegateClosure<T> {
	public CollectionClosure() {
	}

	public CollectionClosure(S src) {
		createCollection(src);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(Object input) {
		Collection collection = Collection.class.cast(input);
		for (Object element : collection) {
			delegate.execute(element);
			getResult().add(delegate.getResult());
		}
	}

	public abstract void createCollection(S src);
}
