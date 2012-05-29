package at.schauer.gregor.dormancy.util;

import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;

/**
 * @author Gregor Schauer
 */
public class HibernatePropertyAccessor implements PropertyAccessor {
	@Override
	public Getter getGetter(Class theClass, String propertyName) {
		try {
			PropertyAccessor propertyAccessor = PropertyAccessorFactory.getPropertyAccessor(null);
			return propertyAccessor.getGetter(theClass, propertyName);
		} catch (PropertyNotFoundException e) {
			PropertyAccessor directAccessor = PropertyAccessorFactory.getPropertyAccessor("field");
			return directAccessor.getGetter(theClass, propertyName);
		}
	}

	@Override
	public Setter getSetter(Class theClass, String propertyName) {
		try {
			PropertyAccessor propertyAccessor = PropertyAccessorFactory.getPropertyAccessor(null);
			return propertyAccessor.getSetter(theClass, propertyName);
		} catch (PropertyNotFoundException e) {
			PropertyAccessor directAccessor = PropertyAccessorFactory.getPropertyAccessor("field");
			return directAccessor.getSetter(theClass, propertyName);
		}
	}
}
