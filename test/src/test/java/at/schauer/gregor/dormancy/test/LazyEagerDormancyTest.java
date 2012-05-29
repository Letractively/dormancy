package at.schauer.gregor.dormancy.test;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Employee;
import org.hibernate.HibernateException;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Gregor Schauer
 */
public class LazyEagerDormancyTest extends AbstractDormancyTest {
	@Test(expected = HibernateException.class)
	public void testLazyOneToOne() {
		Application app = service.load(Application.class, 1L);
		assertNull(app.getResponsibleUser());

		app.setResponsibleUser(service.load(Employee.class, 1L));
		service.save(app);
	}

	@Test(expected = HibernateException.class)
	public void testOverwriteLazyNullProperty() {
		Employee b = service.load(Employee.class, 2L);
		assertEquals(true, b.getEmployees().isEmpty());
		b.setEmployees(Collections.singleton(service.load(Employee.class, 3L)));
		service.save(b);
	}

	@Test(expected = HibernateException.class)
	public void testOverwriteLazyInitializedProperty() {
		Employee b = service.load(Employee.class, 2L);
		assertEquals(null, b.getColleagues());
		b.setColleagues(Collections.singleton(service.load(Employee.class, 3L)));
		service.save(b);
	}
}
