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
package at.schauer.gregor.dormancy.test;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.domain.DTO;
import at.schauer.gregor.dormancy.domain.ReadOnlyDTO;
import at.schauer.gregor.dormancy.domain.Stage;
import at.schauer.gregor.dormancy.domain.WriteOnlyDTO;
import at.schauer.gregor.dormancy.entity.*;
import at.schauer.gregor.dormancy.persister.AbstractEntityPersister;
import at.schauer.gregor.dormancy.persister.NoOpPersister;
import org.hibernate.FlushMode;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 */
public class SimpleDormancyTest extends AbstractDormancyTest {
	Map<Class<?>, AbstractEntityPersister<?>> persisterMap;

	@Before
	public void before() {
		if (persisterMap == null) {
			persisterMap = new HashMap<Class<?>, AbstractEntityPersister<?>>(dormancy.getPersisterMap());
		} else {
			dormancy.getPersisterMap().clear();
			dormancy.getPersisterMap().putAll(persisterMap);
		}
		dormancy.addEntityPersister(NoOpPersister.getInstance(), DTO.class);
	}

	@Test
	public void testNull() {
		assertEquals(null, dormancy.clone(null));
		assertEquals(null, dormancy.merge(null));
		assertEquals(null, dormancy.merge(null, (Object) null));
	}

	@Test
	public void testPrimitive() {
		Long l = Long.MAX_VALUE;
		Double pi = Math.PI;
		assertSame(true, dormancy.clone(true));
		assertSame(5, dormancy.clone(5));
		assertSame(pi, dormancy.clone(pi));
		assertSame("", dormancy.clone(""));
		assertSame(l, dormancy.clone(l));

		assertSame(true, dormancy.merge(true));
		assertSame(5, dormancy.merge(5));
		assertSame(pi, dormancy.merge(pi));
		assertSame("", dormancy.merge(""));
		assertSame(l, dormancy.merge(l));
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
	public void testCloneInvalidEntity() {
		dormancy.getConfig().setCloneObjects(true);
		try {
			dormancy.clone(new InvalidEntity(false));
			fail(BeanInstantiationException.class.getSimpleName() + " expected");
		} finally {
			dormancy.getConfig().setCloneObjects(false);
		}
	}

	@Test
	public void testUseInvalidEntity() {
		InvalidEntity obj = new InvalidEntity(false);
		InvalidEntity clone = dormancy.clone(obj);
		assertSame(obj, clone);
	}

	@Test(expected = MethodInvocationException.class)
	public void testCloneEntityWithoutSetter() {
		ReadOnlyEntity obj = new ReadOnlyEntity(1L, "readOnly");
		dormancy.clone(obj);
	}

	@Test(expected = MethodInvocationException.class)
	public void testMergeEntityWithoutSetter() {
		ReadOnlyEntity obj = new ReadOnlyEntity(1L, "readOnly");
		ReadOnlyEntity modified = new ReadOnlyEntity(1L, "read");
		dormancy.merge(modified, obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testCloneEntityWithoutGetter() {
		WriteOnlyEntity obj = new WriteOnlyEntity(1L, "writeOnly");
		dormancy.clone(obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testMergeEntityWithoutGetter() {
		WriteOnlyEntity obj = new WriteOnlyEntity(1L, "writeOnly");
		WriteOnlyEntity modified = new WriteOnlyEntity(1L, "write");
		dormancy.merge(modified, obj);
	}

	@Test
	public void testCloneDTOWithoutSetter() {
		ReadOnlyDTO obj = new ReadOnlyDTO(1L, "readOnly");
		dormancy.clone(obj);
	}

	@Test
	public void testMergeDTOWithoutSetter() {
		ReadOnlyDTO obj = new ReadOnlyDTO(1L, "readOnly");
		ReadOnlyDTO modified = new ReadOnlyDTO(1L, "read");
		dormancy.merge(modified, obj);
	}

	@Test
	public void testCloneDTOWithoutGetter() {
		WriteOnlyDTO obj = new WriteOnlyDTO(1L, "writeOnly");
		dormancy.clone(obj);
	}

	@Test
	public void testMergeDTOWithoutGetter() {
		WriteOnlyDTO obj = new WriteOnlyDTO(1L, "writeOnly");
		WriteOnlyDTO modified = new WriteOnlyDTO(1L, "write");
		dormancy.merge(modified, obj);
	}

	@Test
	public void testNewInstance() {
		dormancy.getConfig().setSaveNewEntities(true);
		try {
			Long id = (Long) service.save(new Book("new"));
			Book load = service.load(Book.class, id);
			assertEquals(false, isManaged(load, sessionFactory.getCurrentSession()));
			assertEquals("new", load.getTitle());
		} finally {
			dormancy.getConfig().setSaveNewEntities(false);
		}
	}

	@Test(expected = ObjectNotFoundException.class)
	public void testNonExistingEntity() {
		Book book = new Book("new");
		book.setId(0L);
		dormancy.merge(book);
	}

	@Test
	public void testDataTypes() {
		Session session = sessionFactory.getCurrentSession();
		DataTypes a = (DataTypes) session.load(DataTypes.class, 1L);
		DataTypes b = service.load(DataTypes.class, 1L);
		assertNotSame(a, b);
		assertEquals(false, a.equals(b));
		assertEquals(describe(a), describe(b));
		assertEquals(true, isManaged(a, session));
		assertEquals(false, isManaged(b, session));

		b.setIntArray(new int[]{11});
		b.setIntegerArray(new Integer[]{12});
		Long id = (Long) service.save(b);
		DataTypes c = service.load(DataTypes.class, id);
		assertEquals(describe(b), describe(c));
	}

	@Test
	public void testDateTime() {
		Clock clock = new Clock();
		sessionFactory.getCurrentSession().save(clock);
		sessionFactory.getCurrentSession().flush();

		Clock load = service.load(Clock.class, 1L);
		load.update();
		service.save(load);
		Clock bar = service.load(Clock.class, 1L);
		assertEquals(load, bar);
	}

	@Test
	public void testCompare() throws Exception {
		Session session = sessionFactory.getCurrentSession();
		Book a = (Book) session.load(Book.class, 1L);
		Book b = service.load(Book.class, 1L);
		assertNotSame(a, b);
		assertEquals(describe(a), describe(b));
		assertEquals(true, isManaged(a, session));
		assertEquals(false, isManaged(b, session));
	}

	@Test
	public void testEnum() {
		Stage stage = Stage.DEV;

		Stage clone = dormancy.clone(stage);
		assertSame(stage, clone);

		Stage merge = dormancy.merge(clone);
		assertSame(stage, merge);

		merge = dormancy.merge(clone, stage);
		assertSame(stage, merge);
	}

	@Test
	public void testEmployeeHierarchy() {
		Employee bp = (Employee) sessionFactory.getCurrentSession().load(Employee.class, 2L);
		Employee bt = service.load(Employee.class, 2L);

		assertEquals(false, bp.getColleagues() == null);
		assertEquals(Collections.<Employee>emptySet(), bt.getColleagues());
		assertEquals(1, bp.getEmployees().size());
		assertEquals(Collections.<Employee>emptySet(), bt.getEmployees());

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
	public void testUpdateReferencedEntity() {
		Query query = sessionFactory.getCurrentSession().createQuery("FROM Employee e LEFT JOIN FETCH e.employees WHERE e.id = :id");
		Employee c = dormancy.clone((Employee) query.setParameter("id", 3L).uniqueResult());
		c.getBoss().setName("Master");
		sessionFactory.getCurrentSession().save(dormancy.merge(c, (Employee) query.setParameter("id", 3L).uniqueResult()));

		Employee z = dormancy.clone((Employee) query.setParameter("id", 3L).uniqueResult());
		assertEquals("Master", z.getBoss().getName());
	}

	@Ignore
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
		dormancy.getConfig().setCloneObjects(true);

		Session session = sessionFactory.getCurrentSession();

		HibernateCallback<Application> hibernateCallback = new HibernateCallback<Application>() {
			@Override
			public Application doInHibernate(Session session) {
				return (Application) session.createQuery("SELECT a FROM Application a JOIN FETCH a.employees WHERE a.id = 1").uniqueResult();
			}
		};

		Application app = dormancy.clone(hibernateCallback.doInHibernate(session));
		app.getEmployees().clear();
		app.getEmployees().add(service.load(Employee.class, 1L));
		Application merge = dormancy.merge(app, hibernateCallback);

		assertEquals(describe(app), describe(merge));

		assertEquals(false, isManaged(app.getEmployees(), session));
		assertEquals(true, isManaged(merge.getEmployees(), session));

		assertEquals("A", app.getEmployees().iterator().next().getName());
		assertEquals("A", merge.getEmployees().iterator().next().getName());

		assertEquals("A", service.load(Employee.class, 1L).getName());
		assertEquals("B", service.load(Employee.class, 2L).getName());
		assertEquals("C", service.load(Employee.class, 3L).getName());
	}
}
