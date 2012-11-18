package at.schauer.gregor.dormancy.util;

/**
 * Utilities for making Hibernate 3 and Hibernate 4 compatible.
 * <p/>
 * Hibernate 4 moved the PersistentCollection class to another package.<br/>
 * Thus, the following code attempts to load the class regardless of the version present in the classpath.
 *
 * @author Gregor Schauer
 */
public class HibernateVersionUtils {
	public static final String HIBERNATE_3_COLLECTION = "org.hibernate.collection.PersistentCollection";
	public static final String HIBERNATE_4_COLLECTION = "org.hibernate.collection.spi.PersistentCollection";
	public static final String HIBERNATE_3_SET = "org.hibernate.collection.PersistentSet";
	public static final String HIBERNATE_4_SET = "org.hibernate.collection.internal.PersistentSet";

	public static boolean isHibernate3() {
		try {
			return Class.forName(HIBERNATE_3_COLLECTION) != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static boolean isHibernate4() {
		try {
			return Class.forName(HIBERNATE_4_COLLECTION) != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

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
