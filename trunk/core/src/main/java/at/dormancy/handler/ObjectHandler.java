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

import at.dormancy.util.DormancyContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Contains operations for disconnecting JPA managed objects and merging them into a persistence context.
 * <p/>
 * The term disconnecting includes any operations required for creating an object graph which is a copy of the given
 * object and its associations. It is neither guaranteed that the types of the constructed objects is the same as the
 * types of the given objects nor its data is exactly the same. In other words, the object graph may be altered
 * (if necessary) but the semantics of the properties must not change.
 * <p/>
 * (Re-)applying means to create and/or load JPA managed entities which are pendents of the given objects and applying
 * any semantic value changes to them.
 * <p/>
 * It is recommended that the application of the properties of a previously disconnected entity onto managed entities
 * returns the entity without modifications e.g.,
 * {@code objectHandler.apply(objectHandler.disconnect(anObject)).equals(anObject)} results to {@code true}.
 * <p/>
 * Implementors must be threadsafe (preferably immutable).
 *
 * @author Gregor Schauer
 */
public interface ObjectHandler<T> {
	/**
	 * Creates a new object based on the type of the given one.
	 *
	 * @param obj the original object
	 * @param <O> the type of the object
	 * @return the newly created object
	 */
	@Nullable
	<O extends T> O createObject(@Nonnull O obj);

	/**
	 * Disconnects the given object from the persistence context.
	 *
	 * @param dbObj the object to disconnect
	 * @param ctx   the context of the operation
	 * @param <O>   the type of the object
	 * @param <R>   the type of the disconnected object
	 * @return the disconnected object
	 */
	@Nullable
	<R extends T, O extends R> R disconnect(O dbObj, @Nonnull DormancyContext ctx);

	/**
	 * Applies the changes from the given object to the persistent object.
	 *
	 * @param trObj the modified object
	 * @param dbObj the JPA managed object
	 * @param ctx   the context of the operation
	 * @param <O>   the type of the object
	 * @param <R>   the type of the disconnected object
	 * @return the persistent object with applied property changes
	 */
	@Nullable
	<O extends T, R extends O> R apply(O trObj, R dbObj, @Nonnull DormancyContext ctx);
}
