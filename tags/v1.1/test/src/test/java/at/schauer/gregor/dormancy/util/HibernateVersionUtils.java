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
package at.schauer.gregor.dormancy.util;

import javax.annotation.Nonnull;

/**
 * Utilities for making Hibernate 3 and Hibernate 4 compatible.
 * <p/>
 * Hibernate 4 moved the PersistentCollection class to another package.<br/>
 * Thus, the following code attempts to load the class regardless of the version present in the classpath.
 *
 * @author Gregor Schauer
 * @since 1.1.0
 */
public class HibernateVersionUtils {
	public static final String HIBERNATE_3_COLLECTION = "org.hibernate.collection.PersistentCollection";
	public static final String HIBERNATE_3_LIST = "org.hibernate.collection.PersistentList";
	public static final String HIBERNATE_3_SET = "org.hibernate.collection.PersistentSet";
	public static final String HIBERNATE_3_MAP = "org.hibernate.collection.PersistentMap";

	public static final String HIBERNATE_4_COLLECTION = "org.hibernate.collection.spi.PersistentCollection";
	public static final String HIBERNATE_4_LIST = "org.hibernate.collection.internal.PersistentList";
	public static final String HIBERNATE_4_SET = "org.hibernate.collection.internal.PersistentSet";
	public static final String HIBERNATE_4_MAP = "org.hibernate.collection.internal.PersistentMap";

	/**
	 * Returns {@code true} if Hibernate 3 collections are found in the classpath.
	 *
	 * @return {@code true} if Hibernate 3 is available, {@code false} otherwise
	 */
	public static boolean isHibernate3() {
		try {
			return Class.forName(HIBERNATE_3_COLLECTION) != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Returns {@code true} if Hibernate 4 collections are found in the classpath.
	 *
	 * @return {@code true} if Hibernate 4 is available, {@code false} otherwise
	 */
	public static boolean isHibernate4() {
		try {
			return Class.forName(HIBERNATE_4_COLLECTION) != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Returns the class used by Hibernate for representing a persistent collection.
	 *
	 * @return the persistent collection class
	 */
	@Nonnull
	public static Class<?> getHibernateCollectionClass() {
		try {
			// Attempt to load the PersistentCollection class shipped with Hibernate 4
			return Class.forName(HIBERNATE_3_COLLECTION);
		} catch (ClassNotFoundException e) {
			try {
				// Attempt to load the PersistentCollection class shipped with Hibernate 4
				return Class.forName(HIBERNATE_4_COLLECTION);
			} catch (ClassNotFoundException ex) {
				throw new RuntimeException(String.format("Hibernate not found in classpath: Cannot load '%s' or '%s'",
						HIBERNATE_3_COLLECTION, HIBERNATE_4_COLLECTION));
			}
		}
	}

	/**
	 * Returns the class used by Hibernate for representing a persistent set.
	 *
	 * @return the persistent set class
	 */
	@Nonnull
	public static Class<?> getHibernateSetClass() {
		try {
			// Attempt to load the PersistentSet class shipped with Hibernate 3
			return Class.forName(HIBERNATE_3_SET);
		} catch (ClassNotFoundException e) {
			try {
				// Attempt to load the PersistentSet class shipped with Hibernate 4
				return Class.forName(HIBERNATE_4_SET);
			} catch (ClassNotFoundException ex) {
				throw new RuntimeException(String.format("Hibernate not found in classpath: Cannot load '%s' or '%s'",
						HIBERNATE_3_SET, HIBERNATE_4_SET));
			}
		}
	}
}
