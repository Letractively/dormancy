/*
 * Copyright 2014 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.handler;

import at.dormancy.util.ClassLookup;
import at.dormancy.util.DormancyContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Simply returns {@code null}.
 *
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class NullObjectHandler<T> implements StaticObjectHandler<T> {
	private final Set<Class<?>> supportedTypes = new HashSet<Class<?>>();

	@SuppressWarnings("unchecked")
	public NullObjectHandler() {
		Class<?> lazyInitializer = ClassLookup.forName("org.hibernate.proxy.LazyInitializer");
		if (lazyInitializer != null) {
			supportedTypes.add(lazyInitializer);
		}
		Class<?> valueHolder = ClassLookup.forName("org.eclipse.persistence.indirection.ValueHolder");
		if (valueHolder != null) {
			supportedTypes.add(valueHolder);
		}
	}

	@Nullable
	@Override
	public <R extends T> R createObject(@Nullable R obj) {
		return null;
	}

	@Nullable
	@Override
	public <R extends T, O extends R> R disconnect(@Nullable O dbObj, @Nonnull DormancyContext ctx) {
		return null;
	}

	@Nullable
	@Override
	public <O extends T, R extends O> R apply(@Nullable O trObj, @Nullable R dbObj, @Nonnull DormancyContext ctx) {
		return null;
	}

	@Nonnull
	@Override
	public Set<Class<?>> getSupportedTypes() {
		return supportedTypes;
	}
}
