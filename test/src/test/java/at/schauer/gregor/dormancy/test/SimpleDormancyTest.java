package at.schauer.gregor.dormancy.test;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.domain.DTO;
import at.schauer.gregor.dormancy.entity.*;
import org.hibernate.FlushMode;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class SimpleDormancyTest extends AbstractDormancyTest {
	@Test
	public void testNull() {
		assertEquals(null, dormancy.clone(null));
		assertEquals(null, dormancy.merge(null));
		assertEquals(null, dormancy.merge(null, (Object) null));
	}

	@Test
	public void testNonEntity() throws SQLException {
		DTO a = new DTO();
		assertSame(a, dormancy.clone(a));
		assertSame(a, dormancy.merge(a));
		assertSame(a, dormancy.merge(a, a));
	}

	@Test
	public void testDoingNothing() {
		assertNotNull(service);
		service.doNothing();
	}

	@Test(expected = BeanInstantiationException.class)
	public void testInvalidEntity() {
		dormancy.clone(new InvalidEntity(false));
		fail(BeanInstantiationException.class.getSimpleName() + " expected");
	}

	@Test
	public void testNewInstance() {
		Long id = (Long) service.save(new Book("new"));
		Book load = service.load(Book.class, id);
		assertEquals(false, isManaged(load));
		assertEquals("new", load.getTitle());
	}

	@Test(expected = ObjectNotFoundException.class)
	public void testNonExistingEntity() {
		Book book = new Book("new");
		book.setId(0L);
		dormancy.merge(book);
	}

	@Test
	public void testDataTypes() {
		DataTypes a = (DataTypes) sessionFactory.getCurrentSession().load(DataTypes.class, 1L);
		DataTypes b = service.load(DataTypes.class, 1L);
		assertNotSame(a, b);
		assertEquals(false, a.equals(b));
		assertEquals(describe(a), describe(b));
		assertEquals(true, isManaged(a));
		assertEquals(false, isManaged(b));

		b.setIntArray(new int[]{11});
		b.setIntegerArray(new Integer[]{12});
		Long id = (Long) service.save(b);
		DataTypes c = service.load(DataTypes.class, id);
		assertEquals(describe(b), describe(c));
	}

	@Test
	public void testCompare() throws Exception {
		Book a = (Book) sessionFactory.getCurrentSession().load(Book.class, 1L);
		Book b = service.load(Book.class, 1L);
		assertNotSame(a, b);
		assertEquals(describe(a), describe(b));
		assertEquals(true, isManaged(a));
		assertEquals(false, isManaged(b));
	}

	@Test
	public void testEmployeeHierarchy() {
		Employee bp = (Employee) sessionFactory.getCurrentSession().load(Employee.class, 2L);
		Employee bt = service.load(Employee.class, 2L);

		assertEquals(false, bp.getColleagues() == null);
		assertEquals(null, bt.getColleagues());
		assertEquals(1, bp.getEmployees().size());
		assertEquals(0, bt.getEmployees().size());

		assertNotNull(bp.getBoss());
		assertNotNull(bt.getBoss());
		Map<String, ?> map = describe(bp.getBoss());
		map.put("employees", null);
		assertEquals(map, describe(bt.getBoss()));
	}

	@Test
	public void testManipulateId() {
		sessionFactory.getCurrentSession().save(new Book("2"));
		sessionFactory.getCurrentSession().flush();

		Book a = service.load(Book.class, 1L);
		Book b = service.load(Book.class, 2L);

		b.setId(1L);
		b.setTitle(UUID.randomUUID().toString());
		service.save(b);

		Book c = service.load(Book.class, b.getId());
		assertEquals(true, a.getId().equals(c.getId()));
		assertEquals(false, a.getTitle().equals(c.getTitle()));
	}

	@Test
	public void testUpdateReference() {
		Session session = sessionFactory.getCurrentSession();
		session.setFlushMode(FlushMode.MANUAL);

		Query query = session.createQuery("FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee a = dormancy.clone((Employee) query.setParameter("id", 1L).uniqueResult());
		Employee b = dormancy.clone((Employee) query.setParameter("id", 2L).uniqueResult());
		Employee c = dormancy.clone((Employee) query.setParameter("id", 3L).uniqueResult());

		assertEquals(b, c.getBoss());
		assertEquals(false, a.equals(c.getBoss()));
		assertEquals(true, b.equals(c.getBoss()));
		assertEquals(false, a.getEmployees().contains(c));
		assertEquals(true, b.getEmployees().contains(c));
		session.clear();

		b.getEmployees().remove(c);
		a.getEmployees().add(c);
		c.setBoss(a);
		session.save(dormancy.merge(b, (Employee) query.setParameter("id", 2L).uniqueResult()));
		session.save(dormancy.merge(a, (Employee) query.setParameter("id", 1L).uniqueResult()));
		session.save(dormancy.merge(c, (Employee) query.setParameter("id", 3L).uniqueResult()));

		Employee x = dormancy.clone((Employee) query.setParameter("id", 1L).uniqueResult());
		Employee y = dormancy.clone((Employee) query.setParameter("id", 2L).uniqueResult());
		Employee z = dormancy.clone((Employee) query.setParameter("id", 3L).uniqueResult());
		assertEquals(true, x.equals(z.getBoss()));
		assertEquals(false, y.equals(z.getBoss()));
		assertEquals(true, x.getEmployees().contains(z));
		assertEquals(false, y.getEmployees().contains(z));
	}

	@Test
	public void testUpdateReferencedEntity() {
		Query query = sessionFactory.getCurrentSession().createQuery("FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee c = dormancy.clone((Employee) query.setParameter("id", 3L).uniqueResult());
		c.getBoss().setName("Master");
		sessionFactory.getCurrentSession().save(dormancy.merge(c, (Employee) query.setParameter("id", 3L).uniqueResult()));

		Employee z = dormancy.clone((Employee) query.setParameter("id", 3L).uniqueResult());
		assertEquals("Master", z.getBoss().getName());
	}

	@Test
	public void testUpdateUnreferencedEntity() {
		Employee c = service.load(Employee.class, 3L);
		c.getBoss().setName("Chief");
		service.save(c);

		Employee z = service.load(Employee.class, 3L);
		assertEquals("Chief", z.getBoss().getName());
	}

	@Test
	public void testModifyCollection() throws SQLException {
		Session session = sessionFactory.getCurrentSession();
		session.setFlushMode(FlushMode.MANUAL);

		Query query = session.createQuery("FROM Employee e LEFT JOIN FETCH e.colleagues LEFT JOIN FETCH e.employees WHERE id = :id");
		Employee a = dormancy.clone((Employee) query.setParameter("id", 1L).uniqueResult());
		Employee b = dormancy.clone((Employee) query.setParameter("id", 2L).uniqueResult());
		Employee c = dormancy.clone((Employee) query.setParameter("id", 3L).uniqueResult());
		Employee d = new Employee("Newbie", a);
		assertEquals(1, a.getEmployees().size());
		assertEquals(true, a.getEmployees().contains(b));

		a.getEmployees().remove(b);
		b.getEmployees().remove(c);
		b.setBoss(b);
		session.merge(b);

		a.getEmployees().add(c);
		a.getEmployees().add(d);
		c.setBoss(a);
		d.setBoss(a);

		Employee merge = dormancy.merge(a, (Employee) query.setParameter("id", 1L).uniqueResult());
		session.save(merge);
		session.flush();
	}


	@Test
	public void testModifyCollectionNew() throws SQLException {
		Session session = sessionFactory.getCurrentSession();

		HibernateCallback<Application> hibernateCallback = new HibernateCallback<Application>() {
			@Override
			public Application doInHibernate(Session session) throws SQLException {
				return (Application) session.createQuery("SELECT a FROM Application a JOIN FETCH a.employees WHERE a.id = 1").uniqueResult();
			}
		};

		Application app = dormancy.clone(hibernateCallback.doInHibernate(session));
		app.getEmployees().clear();
		app.getEmployees().add(service.load(Employee.class, 1L));
		Application merge = dormancy.merge(app, hibernateCallback);

		assertEquals(describe(app), describe(merge));

		assertEquals(false, isManaged(app.getEmployees()));
		assertEquals(true, isManaged(merge.getEmployees()));

		assertEquals("A", app.getEmployees().iterator().next().getName());
		assertEquals("A", merge.getEmployees().iterator().next().getName());

		assertEquals("A", service.load(Employee.class, 1L).getName());
		assertEquals("B", service.load(Employee.class, 2L).getName());
		assertEquals("C", service.load(Employee.class, 3L).getName());
	}
}
