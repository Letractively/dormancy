package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.entity.Employee;
import org.hibernate.Session;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class SetPersisterTest extends PersisterTest<CollectionPersister<Set>> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new CollectionPersister<Set>(dormancy);
		persister.setSessionFactory(sessionFactory);
		persister.setConfig(dormancy.getConfig());
	}

	@Test
	public void testSame() {
		HashSet<String> singleton = new HashSet<String>();

		Set clone = persister.clone(singleton);
		assertEquals(singleton, clone);
		assertNotSame(singleton, clone);

		Set merge = persister.merge(singleton);
		assertEquals(singleton, merge);
		assertNotSame(singleton, merge);

		merge = persister.merge(clone, singleton);
		assertEquals(singleton, merge);
		assertSame(singleton, merge);
	}

	@Test
	public void testEntity() {
		Session session = sessionFactory.getCurrentSession();
		Employee a = (Employee) session.get(Employee.class, 2L);
		assertEquals(true, AbstractDormancyTest.isManaged(a.getEmployees(), session));
		assertEquals(true, AbstractDormancyTest.isManaged(a.getEmployees().iterator().next(), session));

		Set clone = persister.clone(a.getEmployees());
		assertEquals(false, AbstractDormancyTest.isManaged(clone, session));
		assertEquals(false, AbstractDormancyTest.isManaged(clone.iterator().next(), session));

		Set merge = persister.merge(clone);
		assertEquals(true, AbstractDormancyTest.isManaged(merge.iterator().next(), session));

		merge = persister.merge(clone, a.getEmployees());
		assertEquals(true, AbstractDormancyTest.isManaged(merge.iterator().next(), session));
	}
}
