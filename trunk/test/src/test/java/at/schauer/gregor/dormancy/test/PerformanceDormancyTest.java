package at.schauer.gregor.dormancy.test;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Employee;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.lang.annotation.*;

import static org.junit.Assert.assertNotNull;

/**
 * @author Gregor Schauer
 */
public class PerformanceDormancyTest extends AbstractDormancyTest {
	protected static final Logger logger = Logger.getLogger(PerformanceDormancyTest.class);

	@Perform(name = "Session.get(Object, Serializable)", n = 100)
	@Test(timeout = 200)
	public void testHibernateGet() {
		Perform perform = getAnnotation();
		assertNotNull(perform);

		long start = System.currentTimeMillis();
		for (int i = 0; i < perform.n(); i++) {
			sessionFactory.getCurrentSession().get(Employee.class, 2L);
			sessionFactory.getCurrentSession().clear();
		}

		log(perform, start);
	}

	@Perform(name = "Dormancy.clone(Object)", n = 100)
	@Test(timeout = 300)
	public void testClone() {
		Perform perform = getAnnotation();
		Employee b = (Employee) sessionFactory.getCurrentSession().get(Employee.class, 2L);
		assertNotNull(b);

		long start = System.currentTimeMillis();
		for (int i = 0; i < perform.n(); i++) {
			Employee clone = dormancy.clone(b);
		}

		log(perform, start);
	}

	@Perform(name = "Dormancy.merge(Object)", n = 100)
	@Test(timeout = 200)
	public void testMerge() {
		Perform perform = getAnnotation();
		Employee b = service.load(Employee.class, 2L);
		assertNotNull(b);


		long start = System.currentTimeMillis();
		for (int i = 0; i < perform.n(); i++) {
			Employee merge = dormancy.merge(b);
			sessionFactory.getCurrentSession().clear();
		}

		log(perform, start);
	}

	@Perform(name = "Dormancy.merge(Object, Object)", n = 100)
	@Test(timeout = 100)
	public void testMergeTogether() {
		Perform perform = getAnnotation();
		Employee bp = (Employee) sessionFactory.getCurrentSession().get(Employee.class, 2L);
		Employee bt = service.load(Employee.class, 2L);
		assertNotNull(bp);

		long start = System.currentTimeMillis();
		for (int i = 0; i < perform.n(); i++) {
			Employee merge = dormancy.merge(bt, bp);
			sessionFactory.getCurrentSession().clear();
		}

		log(perform, start);
	}

	private void log(Perform perform, long start) {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Executed %d times %s in %d ms", perform.n(), perform.name(), System.currentTimeMillis() - start));
		}
	}

	@Documented
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Perform {
		int n();

		String name();
	}

	protected Perform getAnnotation() {
		String methodName = getMethodName();
		return MethodUtils.getAccessibleMethod(this.getClass(), methodName, new Class[0]).getAnnotation(Perform.class);
	}

	protected static String getMethodName() {
		return Thread.currentThread().getStackTrace()[3].getMethodName();
	}
}
