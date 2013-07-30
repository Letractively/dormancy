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
package at.dormancy.util;

import javax.annotation.Nonnull;

/**
 * Utilities for making JPA providers compatible.
 * <p/>
 * They attempts to load the collection classes regardless of the implementation and version present in the classpath.
 * <p/>
 * Currently, the following JPA providers are supported:
 * <ul>
 *     <li>Hibernate 3</li>
 *     <li>Hibernate 4</li>
 *     <li>EclipseLink</li>
 * </ul>
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class JpaProviderUtils {
	protected static final String HIBERNATE_3_COLLECTION = "org.hibernate.collection.PersistentCollection";
	protected static final String HIBERNATE_3_LIST = "org.hibernate.collection.PersistentList";
	protected static final String HIBERNATE_3_SET = "org.hibernate.collection.PersistentSet";
	protected static final String HIBERNATE_3_MAP = "org.hibernate.collection.PersistentMap";

	protected static final String HIBERNATE_4_COLLECTION = "org.hibernate.collection.spi.PersistentCollection";
	protected static final String HIBERNATE_4_LIST = "org.hibernate.collection.internal.PersistentList";
	protected static final String HIBERNATE_4_SET = "org.hibernate.collection.internal.PersistentSet";
	protected static final String HIBERNATE_4_MAP = "org.hibernate.collection.internal.PersistentMap";

	protected static final String JPA_ECLIPSE_LINK_COLLECTION = "org.eclipse.persistence.indirection.IndirectCollection";
	protected static final String JPA_ECLIPSE_LINK_LIST = "org.eclipse.persistence.indirection.IndirectList";
	protected static final String JPA_ECLIPSE_LINK_SET = "org.eclipse.persistence.indirection.IndirectSet";
	protected static final String JPA_ECLIPSE_LINK_MAP = "org.eclipse.persistence.indirection.IndirectMap";

	protected static final Class<?> PERSISTENT_COLLECTION = ClassLookup.find(JPA_ECLIPSE_LINK_COLLECTION, HIBERNATE_4_COLLECTION, HIBERNATE_3_COLLECTION).orThrow(new IllegalStateException("No supported persistence provider found")).get();
	protected static final Class<?> PERSISTENT_LIST = ClassLookup.find(JPA_ECLIPSE_LINK_LIST, HIBERNATE_4_LIST, HIBERNATE_3_LIST).orThrow(new IllegalStateException("No supported persistence provider found")).get();
	protected static final Class<?> PERSISTENT_SET = ClassLookup.find(JPA_ECLIPSE_LINK_SET, HIBERNATE_4_SET, HIBERNATE_3_SET).orThrow(new IllegalStateException("No supported persistence provider found")).get();
	protected static final Class<?> PERSISTENT_MAP = ClassLookup.find(JPA_ECLIPSE_LINK_MAP, HIBERNATE_4_MAP, HIBERNATE_3_MAP).orThrow(new IllegalStateException("No supported persistence provider found")).get();

	/**
	 * Returns {@code true} if Hibernate 3 collections are found in the classpath.
	 *
	 * @return {@code true} if Hibernate 3 is available, {@code false} otherwise
	 */
	public static boolean isHibernate3() {
		return getPersistentCollectionClass().getName().equals(HIBERNATE_3_COLLECTION);
	}

	/**
	 * Returns {@code true} if Hibernate 4 collections are found in the classpath.
	 *
	 * @return {@code true} if Hibernate 4 is available, {@code false} otherwise
	 */
	public static boolean isHibernate4() {
		return getPersistentCollectionClass().getName().equals(HIBERNATE_4_COLLECTION);
	}

	/**
	 * Returns {@code true} if EclipseLink collections are found in the classpath.
	 *
	 * @return {@code true} if EclipseLink is available, {@code false} otherwise
	 */
	public static boolean isEclipseLink() {
		return getPersistentCollectionClass().getName().equals(JPA_ECLIPSE_LINK_COLLECTION);
	}

	/**
	 * Returns the class used by Hibernate for representing a persistent collection.
	 *
	 * @return the persistent collection class
	 */
	@Nonnull
	public static Class<?> getPersistentCollectionClass() {
		return PERSISTENT_COLLECTION;
	}

	/**
	 * Returns the class that represents a persistent list.
	 *
	 * @return the persistent list class
	 */
	@Nonnull
	public static Class<?> getPersistentListClass() {
		return PERSISTENT_LIST;
	}

	/**
	 * Returns the class that represents a persistent set.
	 *
	 * @return the persistent set class
	 */
	@Nonnull
	public static Class<?> getPersistentSetClass() {
		return PERSISTENT_SET;
	}

	/**
	 * Returns the class that represents a persistent map.
	 *
	 * @return the persistent map class
	 */
	@Nonnull
	public static Class<?> getPersistentMapClass() {
		return PERSISTENT_MAP;
	}
}
