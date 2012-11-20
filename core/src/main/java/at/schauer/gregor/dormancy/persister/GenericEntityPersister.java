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
package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Determines the properties to process dynamically.
 *
 * @author Gregor Schauer
 * @since 1.0.1
 */
public abstract class GenericEntityPersister<C> extends AbstractEntityPersister<C> {
	protected boolean reuseObject;
	protected Dormancy dormancy;

	@Inject
	protected GenericEntityPersister(@Nonnull Dormancy dormancy) {
		this.dormancy = dormancy;
	}

	/**
	 * Creates a new instance of the given object type.<br/>
	 * If {@link #reuseObject} is {@code true}, the given object is returned directly. Otherwise, a new instances is
	 * created if possible.
	 * Note that the class must have a no-arg constructor, which can be declared {@code private} because this method
	 * tries to set it accessible.
	 *
	 * @param trObj the object
	 * @param <T>   the type of the object
	 * @return the instance
	 * @see org.springframework.beans.BeanUtils#instantiateClass(Class)
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	protected <T extends C> T createObject(@Nonnull T trObj) {
		return reuseObject ? trObj : (T) BeanUtils.instantiateClass(dormancy.getUtils().getClass(trObj));
	}

	/**
	 * Sets whether the results should be written to the given objects directly or if a new instance has to be created.
	 *
	 * @param reuseObject {@code true} if the given objects should be reused, {@code false} otherwise
	 */
	public void setReuseObject(boolean reuseObject) {
		this.reuseObject = reuseObject;
	}
}
