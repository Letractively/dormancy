package at.schauer.gregor.dormancy.util;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Book;
import org.hibernate.metadata.ClassMetadata;
import org.junit.Test;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.PropertyAccessor;

import static org.junit.Assert.assertTrue;

/**
 * @author Gregor Schauer
 */
public class DormancyUtilsTest extends AbstractDormancyTest {
	@Test
	public void testGetPropertyAccessor() {
		checkPropertyAccessor(new Object(), DirectFieldAccessor.class);

		// If class metadata is available, Dormancy uses the same access strategy as Hibernate.
		checkPropertyAccessor(new Book(), DirectFieldAccessor.class);
		checkPropertyAccessor(new Application(), BeanWrapperImpl.class);

		// If no class metadata is available, the field access strategy is used
		checkPropertyAccessor(new Book(), DirectFieldAccessor.class, null);
		checkPropertyAccessor(new Application(), DirectFieldAccessor.class, null);
	}

	void checkPropertyAccessor(Object obj, Class<? extends PropertyAccessor> accessorType) {
		checkPropertyAccessor(obj, accessorType, dormancy.getUtils().getClassMetadata(obj, sessionFactory));
	}

	void checkPropertyAccessor(Object obj, Class<? extends PropertyAccessor> accessorType, ClassMetadata metadata) {
		PropertyAccessor propertyAccessor = dormancy.getUtils().getPropertyAccessor(metadata, obj);
		assertTrue(accessorType.isAssignableFrom(propertyAccessor.getClass()));
	}
}
