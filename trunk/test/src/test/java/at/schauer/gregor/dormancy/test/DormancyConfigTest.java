package at.schauer.gregor.dormancy.test;

import at.schauer.gregor.commons.test.BeanTester;
import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.EntityPersisterConfiguration;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.CollectionEntity;
import at.schauer.gregor.dormancy.entity.Employee;
import org.hibernate.Query;
import org.hibernate.TransientObjectException;
import org.junit.After;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gregor Schauer
 */
public class DormancyConfigTest extends AbstractDormancyTest {
	@After
	@Override
	public void after() {
		super.after();

		ReflectionTestUtils.setField(dormancy, "config", null);
		ReflectionTestUtils.invokeMethod(dormancy, "initialize");
	}

	@Test
	public void testConfigWithoutParent() throws Exception {
		EntityPersisterConfiguration config = new EntityPersisterConfiguration();
		assertNotNull(config);
		BeanTester.getInstance().test(config);
	}

	@Test
	public void testConfigWithParent() throws Exception {
		EntityPersisterConfiguration config = new EntityPersisterConfiguration(new EntityPersisterConfiguration());
		assertNotNull(config);
		BeanTester.getInstance().test(config);
	}

	@Test
	public void testSavePropertiesOfAssociations() {
		dormancy.getConfig().setSaveAssociationsProperties(false);
		Employee b = service.load(Employee.class, 2L);
		b.getBoss().setName("Big Boss");
		service.save(b);

		Employee y = service.load(Employee.class, 2L);
		assertEquals("Big Boss", b.getBoss().getName());
		assertEquals("A", y.getBoss().getName());

		dormancy.getConfig().setSaveAssociationsProperties(true);
		b = service.load(Employee.class, 2L);
		b.getBoss().setName("Big Boss");
		service.save(b);

		y = service.load(Employee.class, 2L);
		assertEquals("Big Boss", b.getBoss().getName());
		assertEquals("Big Boss", y.getBoss().getName());
	}

	@Test(expected = TransientObjectException.class)
	public void testNewInstance() {
		dormancy.getConfig().setSaveNewEntities(false);

		Long id = (Long) service.save(new Book("new"));
		Book load = service.load(Book.class, id);
		assertEquals(false, isManaged(load));
		assertEquals("new", load.getTitle());
	}

	@Test
	public void testSaveTransientCollection() {
		dormancy.getConfig().setSaveNewEntities(true);
		dormancy.getConfig().setSaveAssociationsProperties(false);

		Query query = sessionFactory.getCurrentSession().createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		c.getEmployees().add(new Employee("D", c));
		dormancy.merge(c, query.uniqueResult());

		Employee d = service.load(Employee.class, 4L);
		assertNotNull(d);
	}

	@Test
	public void testSaveTransientAssociation() {
		dormancy.getConfig().setSaveNewEntities(true);
		dormancy.getConfig().setSaveAssociationsProperties(false);

		Query query = sessionFactory.getCurrentSession().createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		c.setBoss(new Employee("D", c));
		dormancy.merge(c, query.uniqueResult());

		Employee d = service.load(Employee.class, 4L);
		assertNotNull(d);
	}

	@Test(expected = TransientObjectException.class)
	public void testNotSaveTransientAssociation() {
		dormancy.getConfig().setSaveNewEntities(false);
		dormancy.getConfig().setSaveAssociationsProperties(false);

		Query query = sessionFactory.getCurrentSession().createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		c.setBoss(new Employee("D", c));
		dormancy.merge(c, query.uniqueResult());
	}

	@Test
	public void testUpdateAssociation() {
		dormancy.getConfig().setSaveAssociationsProperties(false);

		Query query = sessionFactory.getCurrentSession().createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee a = (Employee) dormancy.clone(query.setParameter("id", 1L).uniqueResult());
		Employee c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		c.setBoss(a);
		dormancy.merge(c, query.uniqueResult());

		c = (Employee) dormancy.clone(query.setParameter("id", 3L).uniqueResult());
		assertEquals(a, c.getBoss());
	}

	@Test
	public void testDeleteFromList() {
		dormancy.getConfig().setDeleteRemovedEntities(true);

		Query query = sessionFactory.getCurrentSession().createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee b = (Employee) query.setParameter("id", 2L).uniqueResult();
		b = dormancy.clone(b);
		b.getEmployees().clear();
		dormancy.merge(b, query.uniqueResult());

		assertEquals(true, service.load(Employee.class, 2L).getEmployees().isEmpty());
		assertEquals(null, sessionFactory.getCurrentSession().get(Employee.class, 3L));
	}

	@Test
	public void testDeleteFromMap() {
		dormancy.getConfig().setDeleteRemovedEntities(true);

		CollectionEntity ap = new CollectionEntity();
		ap.setLongMap(new HashMap<Long, Long>(Collections.singletonMap(2L, 3L)));
		sessionFactory.getCurrentSession().save(ap);
		sessionFactory.getCurrentSession().flush();

		ap = (CollectionEntity) sessionFactory.getCurrentSession().get(CollectionEntity.class, 1L);
		CollectionEntity at = dormancy.clone(ap);
		at.getLongMap().clear();

		assertEquals(true, dormancy.merge(at, ap).getLongMap().isEmpty());
		assertEquals(true, service.load(CollectionEntity.class, 1L).getLongMap().isEmpty());
	}
}
