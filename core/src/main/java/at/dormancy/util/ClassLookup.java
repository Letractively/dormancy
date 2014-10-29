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
package at.dormancy.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Throwables.propagate;

/**
 * Performs class lookups using the default {@link ClassLoader}.
 * <p/>
 * Note that none of the methods throw a {@link ClassNotFoundException}. Instead, {@code null} is returned unless
 * something else is provided.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public final class ClassLookup {
	private List<String> names = new ArrayList<String>(1);
	private Class<?> defaultClass;
	private Exception exception;

	/**
	 * Returns the {@link Class} object associated with the class or interface with the given name.
	 * <p/>
	 * If a {@link ClassNotFoundException} was thrown by the {@link ClassLoader} performing the lookup, it gets caught
	 * gracefully and {@code null} is returned.<br/>
	 * Any other serious exception such as {@link ExceptionInInitializerError} or {@link LinkageError} is forwarded to
	 * the caller immediately.
	 *
	 * @param <T>  the type of the class
	 * @param name the full-qualified name of the desired class.
	 * @return the {@code Class} object for the class with the specified name or {@code null}.
	 * @see Class#forName(String)
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> Class<T> forName(@Nullable String name) {
		try {
			return (Class<T>) Class.forName(StringUtils.defaultIfEmpty(name, ""));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * Creates a new {@code ClassLookup} instance.
	 *
	 * @param names the full-qualified names of the classes to lookup
	 * @return the newly created instance
	 */
	@Nonnull
	public static ClassLookup find(@Nullable String... names) {
		return new ClassLookup(names);
	}

	private ClassLookup(@Nullable String... names) {
		Collections.addAll(this.names, ArrayUtils.nullToEmpty(names));
	}

	/**
	 * Searches for a {@link Class} object associated with the class or interface with the given name.
	 *
	 * @param name the full-qualified name of the desired class
	 * @return this instance
	 */
	@Nonnull
	public ClassLookup or(@Nullable String name) {
		if (name != null) {
			this.names.add(name);
		}
		return this;
	}

	/**
	 * Sets the {@link Class} to return in case of none of the lookups was successful.
	 *
	 * @param defaultClass the {@code Class} to return in case every lookup failed
	 * @return this instance
	 */
	@Nonnull
	public ClassLookup or(@Nullable Class<?> defaultClass) {
		this.defaultClass = defaultClass;
		return this;
	}

	/**
	 * Throws the given {@link Exception} in case of none of the lookups was successful.
	 *
	 * @param e the exception to throw
	 * @return this instance
	 */
	@Nonnull
	public ClassLookup orThrow(@Nullable Exception e) {
		this.exception = e;
		return this;
	}

	/**
	 * Throws a {@link ClassNotFoundException} in case of none of the lookups was successful.
	 *
	 * @param msg  the detail message
	 * @param args the arguments referenced by the format specifiers in the message
	 * @return this instance
	 * @see #orThrow(Exception)
	 */
	@Nonnull
	public ClassLookup orThrow(@Nullable String msg, @Nullable Object... args) {
		return orThrow(new ClassNotFoundException(msg != null ? String.format(msg, args) : null));
	}

	/**
	 * Performs the lookup with the full-qualified class names until a {@code Class} was found.
	 * <p/>
	 * If an exception was provided, it is thrown if no class was found. Otherwise, the provided default class or
	 * {@code null} is returned if none was set.
	 * <p/>
	 * If the exception is checked i.e., it is neither of type {@link RuntimeException} nor {@link Error}, it gets
	 * wrapped in a {@link RuntimeException}.
	 *
	 * @param <T> the type of the class
	 * @return the class found or {@code null} if the lookup failed and no default was provided
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> Class<T> get() {
		for (String name : names) {
			Class<T> clazz = forName(name);
			if (clazz != null) {
				return clazz;
			}
		}
		if (exception != null) {
			throw propagate(exception);
		}
		return (Class<T>) defaultClass;
	}

	/**
	 * Performs the lookup with the full-qualified class names and returns all classes found.
	 * <p/>
	 * If an exception was provided, it is thrown if no class was found.<br/>
	 * The resulting list does neither {@code null} values nor the default class (if provided).
	 * <p/>
	 * If the exception is checked i.e., it is neither of type {@link RuntimeException} nor {@link Error}, it gets
	 * wrapped in a {@link RuntimeException}.
	 *
	 * @param <T> the common super type of the classes
	 * @return the class found or {@code null} if the lookup failed and no default was provided
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> List<Class<? extends T>> list() {
		List<Class<? extends T>> list = new ArrayList<Class<? extends T>>(names.size());
		for (String name : names) {
			Class<T> clazz = forName(name);
			if (clazz != null) {
				list.add(clazz);
			}
		}
		if (exception != null) {
			throw propagate(exception);
		}
		return list;
	}
}
