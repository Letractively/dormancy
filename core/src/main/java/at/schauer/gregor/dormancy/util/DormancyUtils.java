package at.schauer.gregor.dormancy.util;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.PropertyValueException;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Utility methods for Dormancy support code.
 *
 * @author Gregor Schauer
 */
public class DormancyUtils {
	private DormancyUtils() {
	}

	/**
	 * Returns a {@link PropertyAccessor} for the given object.
	 *
	 * @param target the object to create a PropertyAccessor for
	 * @return the PropertyAccessor
	 */
	@Nullable
	public static PropertyAccessor forBeanPropertyAccess(@Nullable Object target) {
		return target != null ? PropertyAccessorFactory.forBeanPropertyAccess(target) : null;
	}

	/**
	 * Iterates over the given collection, looking for an object which is semantically equal to a certain object.<br/>
	 * An object is semantically equal if one of the following conditions apply:
	 * <ul>
	 * <li>the given object overrides the {@link #equals(Object)} equals methods, which returns {@code true} for
	 * another object</li>
	 * <li>the given object has a non-null identifier and its type as well as the identifier value is equals to the
	 * type and identifier value of another object</li>
	 * </ul>
	 *
	 * @param obj        the object
	 * @param collection the collection to traverse
	 * @param session    the Hibernate Session to use
	 * @return the object found or {@code null} if the collection does not contain such an object
	 * @see #getIdentifierValue(org.hibernate.metadata.ClassMetadata, org.springframework.beans.PropertyAccessor, Object, org.hibernate.impl.SessionImpl)
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T findPendant(@Nonnull T obj, @Nonnull Collection collection, @Nonnull SessionImpl session) {
		try {
			Method method = obj.getClass().getMethod("equals", Object.class);
			if (method.getDeclaringClass() != Object.class) {
				// If the given object overrides the equals() method, invoke it for every object in the collection
				for (Object elem : collection) {
					if (obj.equals(elem) && collection.remove(elem)) {
						return (T) elem;
					}
				}
			} else {
				// Otherwise get the Hibernate metadata and a PropertyAccessor to get the identifier
				ClassMetadata objMetadata = getClassMetadata(obj, session.getSessionFactory());
				PropertyAccessor objPropertyAccessor = DormancyUtils.forBeanPropertyAccess(obj);
				Object objIdentifier = objPropertyAccessor.getPropertyValue(objMetadata.getIdentifierPropertyName());

				// For every object in the collection, check if the type matches and if the identifier is equal
				for (Object elem : collection) {
					if (elem != null && elem.getClass() == obj.getClass()
							&& objIdentifier.equals(getIdentifier(objMetadata, null, elem, session))
							&& collection.remove(elem)) {
						return (T) elem;
					}
				}
			}
			return null;
		} catch (NoSuchMethodException e) {
			// Must not happen because Object defines an equals() method
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the {@link ClassMetadata} associated with the given entity class.
	 *
	 * @param obj            the object to retrieve ClassMetadata for
	 * @param sessionFactory the SessionFactory to use
	 * @return the ClassMetadata or {@code null} if the type is not an Hibernate managed entity
	 * @see #getClassMetadata(Class, org.hibernate.SessionFactory)
	 * @see #getClass(Object)
	 */
	@Nullable
	public static ClassMetadata getClassMetadata(@Nullable Object obj, SessionFactory sessionFactory) {
		return obj != null ? getClassMetadata(Hibernate.getClass(obj), sessionFactory) : null;
	}

	/**
	 * Gets the {@link ClassMetadata} associated with the given entity class.
	 *
	 * @param clazz          the type to retrieve ClassMetadata for
	 * @param sessionFactory the SessionFactory to use
	 * @return the ClassMetadata or {@code null} if the type is not an Hibernate managed entity
	 */
	@Nullable
	public static ClassMetadata getClassMetadata(@Nullable Class clazz, SessionFactory sessionFactory) {
		return clazz != null ? sessionFactory.getClassMetadata(clazz) : null;
	}

	/**
	 * Attempts to get the identifier of the given object by using the provided {@link ClassMetadata} or
	 * {@link PropertyAccessor}.
	 *
	 * @param metadata         the ClassMetadata of the object (may be null)
	 * @param propertyAccessor the PropertyAccessor for the object (may be null)
	 * @param bean             the object
	 * @param session          the Hibernate session to use
	 * @return the identifier or {@code null} if the identifier cannot be retrieved or is {@code null}
	 * @see #getIdentifierValue(org.hibernate.metadata.ClassMetadata, org.springframework.beans.PropertyAccessor, Object, org.hibernate.impl.SessionImpl)
	 */
	@Nullable
	public static <T> Serializable getIdentifier(@Nonnull ClassMetadata metadata, @Nullable PropertyAccessor propertyAccessor, @Nonnull T bean, @Nonnull SessionImpl session) {
		Serializable identifier = metadata.getIdentifier(bean, EntityMode.POJO);
		if (identifier == null && propertyAccessor != null) {
			identifier = Serializable.class.cast(propertyAccessor.getPropertyValue(metadata.getIdentifierPropertyName()));
		}
		return identifier;
	}

	/**
	 * Retrieves the identifier of the given object by using the provided {@link ClassMetadata} or
	 * {@link PropertyAccessor}.
	 * If the identifier cannot be retrieved, an {@link PropertyValueException} is thrown.
	 *
	 * @param metadata         the ClassMetadata of the object (may be null)
	 * @param propertyAccessor the PropertyAccessor for the object (may be null)
	 * @param bean             the object
	 * @param session          the Hibernate session to use
	 * @return the identifier or {@code null} if the identifier cannot be retrieved or is {@code null}
	 * @see #getIdentifier(org.hibernate.metadata.ClassMetadata, org.springframework.beans.PropertyAccessor, Object, org.hibernate.impl.SessionImpl)
	 */
	@Nonnull
	public static <T> Serializable getIdentifierValue(@Nonnull ClassMetadata metadata, @Nullable PropertyAccessor propertyAccessor, @Nonnull T bean, @Nonnull SessionImpl session) {
		Serializable identifier = getIdentifier(metadata, propertyAccessor, bean, session);
		if (identifier == null) {
			// If the identifier of the database object is null, it is really null, which indicates a database problem, or it cannot be retrieved
			throw new PropertyValueException("Cannot read identifier", metadata.getEntityName(), metadata.getIdentifierPropertyName());
		}
		return identifier;
	}

	/**
	 * Retrieves the property of the given object by using the provided {@link ClassMetadata} or
	 * {@link PropertyAccessor}.
	 *
	 * @param metadata         the ClassMetadata of the object (may be null)
	 * @param propertyAccessor the PropertyAccessor for the object (may be null)
	 * @param bean             the object
	 * @param propertyName     the name of the property
	 * @return the property
	 * @see PropertyAccessor#getPropertyValue(String)
	 * @see ClassMetadata#getPropertyValue(Object, String, org.hibernate.EntityMode)
	 */
	@Nullable
	public static Object getPropertyValue(@Nullable ClassMetadata metadata, @Nullable PropertyAccessor propertyAccessor, @Nonnull Object bean, @Nonnull String propertyName) {
		Assert.isTrue(metadata != null || propertyAccessor != null, "ClassMetadata and PropertyAccessor cannot both be null");
		return propertyAccessor != null ? propertyAccessor.getPropertyValue(propertyName) : metadata.getPropertyValue(bean, propertyName, EntityMode.POJO);
	}

	/**
	 * Sets the property of the given object by using the provided {@link ClassMetadata} or {@link PropertyAccessor}.
	 *
	 * @param metadata         the ClassMetadata of the object (may be null)
	 * @param propertyAccessor the PropertyAccessor for the object (may be null)
	 * @param bean             the object
	 * @param propertyName     the name of the property
	 * @param value            the value to set
	 * @see PropertyAccessor#setPropertyValue(String, Object)
	 * @see ClassMetadata#setPropertyValue(Object, String, Object, org.hibernate.EntityMode)
	 */
	public static void setPropertyValue(@Nullable ClassMetadata metadata, @Nullable PropertyAccessor propertyAccessor, @Nonnull Object bean, @Nonnull String propertyName, @Nullable Object value) {
		Assert.isTrue(metadata != null || propertyAccessor != null, "ClassMetadata and PropertyAccessor cannot both be null");
		if (propertyAccessor != null) {
			// Use Spring´s PropertyAccessor to set the value directly
			propertyAccessor.setPropertyValue(propertyName, value);
		} else {
			// If no PropertyAccessor is available, use Hibernate´s ClassMetadata to set the value
			metadata.setPropertyValue(bean, propertyName, value, EntityMode.POJO);
		}
	}

	/**
	 * Gets the unproxified type of the given object.
	 *
	 * @param proxy a persistable object or proxy
	 * @return the true class of the instance
	 * @throws org.hibernate.HibernateException
	 *
	 * @see Hibernate#getClass(Object)
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClass(@Nonnull Object proxy) {
		return Hibernate.getClass(proxy);
	}
}
