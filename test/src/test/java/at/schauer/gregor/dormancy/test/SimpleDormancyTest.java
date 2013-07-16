/*
 * Copyright 2013 Gregor Schauer
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
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.ObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;

import javax.persistence.EntityNotFoundException;
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
		assertEquals(null, dormancy.merge(null, null));
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
		dormancy.getConfig().setCloneObjects(false);
		InvalidEntity obj = new InvalidEntity(false);
		InvalidEntity clone = dormancy.clone(obj);
		assertSame(obj, clone);

		dormancy.getConfig().setCloneObjects(true);
		try {
			assertNotNull(dormancy.clone(obj));
			fail(BeanInstantiationException.class.getSimpleName() + " expected");
		} catch (BeanInstantiationException e) {
			// expected
		}
	}

	@Test(expected = InvalidPropertyException.class)
	public void testCloneEntityWithPrivateSetter() {
		ReadOnlyEntity obj = new ReadOnlyEntity(1L, System.nanoTime());
		dormancy.clone(obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testMergeEntityWithPrivateSetter() {
		ReadOnlyEntity obj = new ReadOnlyEntity(1L, System.nanoTime());
		ReadOnlyEntity modified = new ReadOnlyEntity(1L, System.nanoTime());
		dormancy.merge(modified, obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testCloneEntityWithPrivateGetter() {
		WriteOnlyEntity obj = new WriteOnlyEntity(1L, System.nanoTime());
		dormancy.clone(obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testMergeEntityWithPrivateGetter() {
		WriteOnlyEntity obj = new WriteOnlyEntity(1L, System.nanoTime());
		WriteOnlyEntity modified = new WriteOnlyEntity(1L, System.nanoTime());
		dormancy.merge(modified, obj);
	}

	@Test(expected = MethodInvocationException.class)
	public void testCloneEntityWithoutSetter() {
		UnsupportedWriteEntity obj = new UnsupportedWriteEntity(1L, "readOnly");
		dormancy.clone(obj);
	}

	@Test(expected = MethodInvocationException.class)
	public void testMergeEntityWithoutSetter() {
		UnsupportedWriteEntity obj = new UnsupportedWriteEntity(1L, "readOnly");
		UnsupportedWriteEntity modified = new UnsupportedWriteEntity(1L, "read");
		dormancy.merge(modified, obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testCloneEntityWithoutGetter() {
		UnsupportedReadEntity obj = new UnsupportedReadEntity(1L, "writeOnly");
		dormancy.clone(obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testMergeEntityWithoutGetter() {
		UnsupportedReadEntity obj = new UnsupportedReadEntity(1L, "writeOnly");
		UnsupportedReadEntity modified = new UnsupportedReadEntity(1L, "write");
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
			Book load = service.get(Book.class, id);
			assertEquals(false, isManaged(load, persistenceUnitProvider));
			assertEquals("new", load.getTitle());
		} finally {
			dormancy.getConfig().setSaveNewEntities(false);
		}
	}

	@Test
	public void testNonExistingEntity() {
		Book book = new Book("new");
		book.setId(0L);
		try {
			dormancy.merge(book);
			fail(String.format("%s or %s expected", ObjectNotFoundException.class.getSimpleName(), EntityNotFoundException.class.getSimpleName()));
		} catch (ObjectNotFoundException e) {
			// Hibernate
		} catch (EntityNotFoundException e) {
			// JPA
		}
	}

	@Test
	public void testDataTypes() {
		dormancy.getConfig().setCloneObjects(true);
		DataTypes a = genericService.get(DataTypes.class, refDataTypes.getId());
		DataTypes b = service.get(DataTypes.class, refDataTypes.getId());
		assertNotSame(a, b);
		assertEquals(false, a.equals(b));
		assertEquals(describe(a), describe(b));
		assertEquals(true, isManaged(a, persistenceUnitProvider));
		assertEquals(false, isManaged(b, persistenceUnitProvider));

		b.setIntArray(new int[]{11});
		b.setIntegerArray(new Integer[]{12});
		Long id = (Long) service.save(b);
		DataTypes c = service.get(DataTypes.class, id);
		assertEquals(describe(b), describe(c));
	}

	@Test
	public void testDateTime() {
		Clock clock = new Clock();
		genericService.save(clock);
		persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext().flush();

		Clock load = service.get(Clock.class, clock.getId());
		load.update();
		service.save(load);
		Clock bar = service.get(Clock.class, clock.getId());
		assertEquals(load, bar);
	}

	@Test
	public void testCompare() throws Exception {
		dormancy.getConfig().setCloneObjects(true);
		Book a = genericService.get(Book.class, refBook.getId());
		Book b = service.get(Book.class, refBook.getId());
		assertNotSame(a, b);
		assertEquals(describe(a), describe(b));
		assertEquals(true, isManaged(a, persistenceUnitProvider));
		assertEquals(false, isManaged(b, persistenceUnitProvider));
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
		dormancy.getConfig().setCloneObjects(true);
		Employee bp = genericService.get(Employee.class, refB.getId());
		Employee bt = service.get(Employee.class, refB.getId());

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
		Long otherId = (Long) genericService.save(new Book("2"));
		persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext().flush();

		Book a = service.get(Book.class, refBook.getId());
		Book b = service.get(Book.class, otherId);

		b.setId(refBook.getId());
		b.setTitle(UUID.randomUUID().toString());
		service.save(b);

		Book c = service.get(Book.class, b.getId());
		assertEquals(true, a.getId().equals(c.getId()));
		assertEquals(false, a.getTitle().equals(c.getTitle()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEmbeddedId() throws Exception {
		if (isJpa()) {
			return;
		}

		EmbeddedIdEntity entity = new EmbeddedIdEntity();
		entity.setEmbeddableEntity(new EmbeddableEntity());
		entity.setValue("test");

		genericService.save(entity);
		persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext().flush();
		Map<String, String> expected = BeanUtils.describe(entity);

		EmbeddedIdEntity clone = dormancy.clone(entity);
		assertEquals(expected, BeanUtils.describe(clone));

		persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext().clear();
		EmbeddedIdEntity merge = dormancy.merge(clone);
		assertNotSame(entity, merge);
		assertEquals(expected, BeanUtils.describe(merge));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testIdClass() throws Exception {
		Long timestamp = System.currentTimeMillis();
		Long id = 1L;
		IdClassEntity entity = new IdClassEntity();
		entity.setValue("");
		entity.setId(id);
		entity.setTimestamp(timestamp);

		genericService.save(entity);
		persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext().flush();
		Map<String, String> expected = BeanUtils.describe(entity);

		IdClassEntity clone = dormancy.clone(entity);
		assertEquals(expected, BeanUtils.describe(clone));

		persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext().clear();
		IdClassEntity merge = dormancy.merge(clone);
		assertNotSame(entity, merge);
		assertEquals(expected, BeanUtils.describe(merge));

		IdClassPk identifier = (IdClassPk) dormancy.getUtils().getIdentifier(dormancy.getUtils().getMetadata(entity), entity);
		IdClassPk identifierValue = (IdClassPk) dormancy.getUtils().getIdentifierValue(dormancy.getUtils().getMetadata(entity), entity);
		assertNotSame(identifier, identifierValue);
		assertEquals(id, identifier.id);
		assertEquals(timestamp, identifier.timestamp);
	}

	@Test
	public void testMappedSuperclass() throws Exception {
		MappedSuperclassEntity refA = new MappedSuperclassEntity();
		MappedSuperclassEntity refB = new MappedSuperclassEntity();
		refA.setValue("");
		refB.setValue("");

		genericService.save(refA);
		genericService.save(refB);
		refA.setNext(refB);
		persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext().flush();
		persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext().clear();

		MappedSuperclassEntity a = genericService.get(MappedSuperclassEntity.class, refA.getId());
		assertNotNull(a.getValue());
		assertEquals(true, isManaged(a, persistenceUnitProvider));
		assertNotNull(a.getNext());
	}

	@Test
	public void testEntityNotExists() {
		Book book;
		try {
			book = genericService.load(Book.class, Long.MAX_VALUE);
		} catch (EntityNotFoundException e) {
			// The persistence provider runtime is permitted to throw the EntityNotFoundException when getReference is called.
			return;
		}
		assertNotNull(book);
		assertEquals(true, dormancy.getUtils().isJavassistProxy(book.getClass()));
		assertNull(dormancy.clone(book));
	}
}
