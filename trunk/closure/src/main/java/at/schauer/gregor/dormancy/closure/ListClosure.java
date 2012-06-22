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

import org.springframework.core.CollectionFactory;

import java.util.List;

/**
 * @author Gregor Schauer
 */
public abstract class ListClosure extends CollectionClosure<List, List> {
	public ListClosure() {
	}

	public ListClosure(List src) {
		super(src);
	}

	@Override
	public void createCollection(List src) {
		result = List.class.cast(CollectionFactory.createApproximateCollection(src, src.size()));
	}
}
