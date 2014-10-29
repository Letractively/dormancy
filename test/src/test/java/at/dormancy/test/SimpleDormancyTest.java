/*
 * Copyright 2014 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.test;

import at.dormancy.AbstractDormancyTest;
import at.dormancy.domain.Dto;
import at.dormancy.domain.ReadOnlyDto;
import at.dormancy.domain.Stage;
import at.dormancy.domain.WriteOnlyDto;
import at.dormancy.entity.*;
import at.dormancy.handler.BasicTypeHandler;
import at.dormancy.handler.ObjectHandler;
import at.dormancy.util.ClassLookup;
import at.dormancy.util.PersistenceProviderUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;

import javax.persistence.EntityNotFoundException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;

/**
 * @author Gregor Schauer
 */
public class SimpleDormancyTest extends AbstractDormancyTest {
	Map<Class<?>, ObjectHandler<?>> handlerMap;

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		Map<Class<?>, ObjectHandler<?>> registryHandlerMap =
				(Map<Class<?>, ObjectHandler<?>>) getField(dormancy.getRegistry(), "handlerMap");
		if (handlerMap == null) {
			handlerMap = new HashMap<Class<?>, ObjectHandler<?>>(registryHandlerMap);
		} else {
			registryHandlerMap.clear();
			registryHandlerMap.putAll(handlerMap);
		}
		dormancy.getRegistry().addObjectHandler(new BasicTypeHandler<Object>(), Dto.class);
	}

	@Test
	public void testNull() {
		assertEquals(null, dormancy.disconnect(null));
		assertEquals(null, dormancy.apply(null));
		assertEquals(null, dormancy.apply(null, null));
	}

	@Test
	public void testPrimitive() {
		Long l = Long.MAX_VALUE;
		Double pi = Math.PI;
		assertSame(true, dormancy.disconnect(true));
		assertSame(5, dormancy.disconnect(5));
		assertSame(pi, dormancy.disconnect(pi));
		assertSame("", dormancy.disconnect(""));
		assertSame(l, dormancy.disconnect(l));

		assertSame(true, dormancy.apply(true));
		assertSame(5, dormancy.apply(5));
		assertSame(pi, dormancy.apply(pi));
		assertSame("", dormancy.apply(""));
		assertSame(l, dormancy.apply(l));
	}

	@Test
	public void testNonEntity() throws SQLException {
		Dto a = new Dto();
		assertSame(a, dormancy.disconnect(a));
		assertSame(a, dormancy.apply(a));
		assertSame(a, dormancy.apply(a, a));
	}

	@Test
	public void testDoingNothing() {
		assertNotNull(service);
		service.doNothing();
	}

	@Test(expected = BeanInstantiationException.class)
	public void testDisconnectInvalidEntity() {
		dormancy.getConfig().setCloneObjects(true);
		try {
			dormancy.disconnect(new InvalidEntity(false));
			fail(BeanInstantiationException.class.getSimpleName() + " expected");
		} finally {
			dormancy.getConfig().setCloneObjects(false);
		}
	}

	@Test
	public void testUseInvalidEntity() {
		dormancy.getConfig().setCloneObjects(false);
		InvalidEntity obj = new InvalidEntity(false);
		InvalidEntity disconnected = dormancy.disconnect(obj);
		assertSame(obj, disconnected);

		dormancy.getConfig().setCloneObjects(true);
		try {
			assertNotNull(dormancy.disconnect(obj));
			fail(BeanInstantiationException.class.getSimpleName() + " expected");
		} catch (BeanInstantiationException e) {
			// expected
		}
	}

	@Test(expected = InvalidPropertyException.class)
	public void testDisconnectEntityWithPrivateSetter() {
		ReadOnlyEntity obj = new ReadOnlyEntity(1L, System.nanoTime());
		dormancy.disconnect(obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testMergeEntityWithPrivateSetter() {
		ReadOnlyEntity obj = new ReadOnlyEntity(1L, System.nanoTime());
		ReadOnlyEntity modified = new ReadOnlyEntity(1L, System.nanoTime());
		dormancy.apply(modified, obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testDisconnectEntityWithPrivateGetter() {
		WriteOnlyEntity obj = new WriteOnlyEntity(1L, System.nanoTime());
		dormancy.disconnect(obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testMergeEntityWithPrivateGetter() {
		WriteOnlyEntity obj = new WriteOnlyEntity(1L, System.nanoTime());
		WriteOnlyEntity modified = new WriteOnlyEntity(1L, System.nanoTime());
		dormancy.apply(modified, obj);
	}

	@Test(expected = MethodInvocationException.class)
	public void testDisconnectEntityWithoutSetter() {
		UnsupportedWriteEntity obj = new UnsupportedWriteEntity(1L, "readOnly");
		dormancy.disconnect(obj);
	}

	@Test(expected = MethodInvocationException.class)
	public void testMergeEntityWithoutSetter() {
		UnsupportedWriteEntity obj = new UnsupportedWriteEntity(1L, "readOnly");
		UnsupportedWriteEntity modified = new UnsupportedWriteEntity(1L, "read");
		dormancy.apply(modified, obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testDisconnectEntityWithoutGetter() {
		UnsupportedReadEntity obj = new UnsupportedReadEntity(1L, "writeOnly");
		dormancy.disconnect(obj);
	}

	@Test(expected = InvalidPropertyException.class)
	public void testMergeEntityWithoutGetter() {
		UnsupportedReadEntity obj = new UnsupportedReadEntity(1L, "writeOnly");
		UnsupportedReadEntity modified = new UnsupportedReadEntity(1L, "write");
		dormancy.apply(modified, obj);
	}

	@Test
	public void testDisconnectDtoWithoutSetter() {
		ReadOnlyDto obj = new ReadOnlyDto(1L, "readOnly");
		dormancy.disconnect(obj);
	}

	@Test
	public void testMergeDtoWithoutSetter() {
		ReadOnlyDto obj = new ReadOnlyDto(1L, "readOnly");
		ReadOnlyDto modified = new ReadOnlyDto(1L, "read");
		dormancy.apply(modified, obj);
	}

	@Test
	public void testDisconnectDtoWithoutGetter() {
		WriteOnlyDto obj = new WriteOnlyDto(1L, "writeOnly");
		dormancy.disconnect(obj);
	}

	@Test
	public void testMergeDtoWithoutGetter() {
		WriteOnlyDto obj = new WriteOnlyDto(1L, "writeOnly");
		WriteOnlyDto modified = new WriteOnlyDto(1L, "write");
		dormancy.apply(modified, obj);
	}

	@Test(expected = RuntimeException.class)
	public void testNewInstance() {
		service.save(new Book("new"));
	}

	@Test
	public void testNonExistingEntity() {
		Book book = new Book("new");
		book.setId(0L);

		List<Class<?>> exceptions = ClassLookup.find(
				"org.hibernate.ObjectNotFoundException",
				"javax.persistence.EntityNotFoundException")
				.list();
		try {
			dormancy.apply(book);

		} catch (Exception e) {
			assertEquals(getMessage(exceptions), true, exceptions.contains(e.getClass()));
		}
	}

	@Test
	public void testDataTypes() {
		dormancy.getConfig().setCloneObjects(true);
		DataTypes a = genericService.get(DataTypes.class, refDataTypes.getId());
		DataTypes b = service.get(DataTypes.class, refDataTypes.getId());
		assertNotSame(a, b);
		assertEquals(false, a.equals(b));
		Map<String, ?> expected = describe(a);
		Map<String, ?> actual = describe(b);

		assertEquals(4, StringUtils.getLevenshteinDistance(
				expected.remove("calendar").toString(),
				actual.remove("calendar").toString()));
		assertEquals(expected, actual);
		assertEquals(true, isManaged(a, persistenceUnitProvider));
		assertEquals(false, isManaged(b, persistenceUnitProvider));

		Long id = (Long) service.save(b);
		DataTypes c = service.get(DataTypes.class, id);

		expected = describe(b);
		actual = describe(c);
		assertEquals(PersistenceProviderUtils.isEclipseLink() ? 0 : 4, StringUtils.getLevenshteinDistance(
				expected.remove("calendar").toString(), actual.remove("calendar").toString()));
		assertEquals(expected.remove("calendar"), actual.remove("calendar"));
		assertEquals(expected, actual);
	}

	@Test
	public void testDateTime() {
		Clock clock = new Clock();
		genericService.save(clock);
		dormancy.getUtils().flush();

		Clock loaded = service.get(Clock.class, clock.getId());
		loaded.update();
		service.save(loaded);
		Clock updated = service.get(Clock.class, clock.getId());
		assertEquals(loaded, updated);
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

		Stage disconnected = dormancy.disconnect(stage);
		assertSame(stage, disconnected);

		Stage merged = dormancy.apply(disconnected);
		assertSame(stage, merged);

		merged = dormancy.apply(disconnected, stage);
		assertSame(stage, merged);
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
		dormancy.getUtils().flush();

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
		dormancy.getUtils().flush();
		Map<String, String> expected = BeanUtils.describe(entity);

		EmbeddedIdEntity disconnected = dormancy.disconnect(entity);
		assertEquals(expected, BeanUtils.describe(disconnected));

		persistenceContextHolder.clear();
		EmbeddedIdEntity merged = dormancy.apply(disconnected);
		assertNotSame(entity, merged);
		assertEquals(expected, BeanUtils.describe(merged));
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
		dormancy.getUtils().flush();
		Map<String, String> expected = BeanUtils.describe(entity);

		IdClassEntity disconnected = dormancy.disconnect(entity);
		assertEquals(expected, BeanUtils.describe(disconnected));

		persistenceContextHolder.clear();
		IdClassEntity merged = dormancy.apply(disconnected);
		assertNotSame(entity, merged);
		assertEquals(expected, BeanUtils.describe(merged));

		Object metadata = dormancy.getUtils().getMetadata(entity);
		IdClassId identifier = (IdClassId) dormancy.getUtils().getIdentifier(metadata, entity);
		IdClassId identifierValue = (IdClassId) dormancy.getUtils().getIdentifierValue(metadata, entity);
		assertNotSame(identifier, identifierValue);
		assertEquals(id, identifier.id);
		assertEquals(timestamp, identifier.timestamp);
	}

	@Ignore
	@Test
	// TODO: write test that uses @OneToOne identifier
	public void testOneToOneId() {
		Account account = new Account();
		account.setEmployee(refA);
		genericService.save(account);
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
		dormancy.getUtils().flush();
		persistenceContextHolder.clear();

		MappedSuperclassEntity a = genericService.get(MappedSuperclassEntity.class, refA.getId());
		assertNotNull(a.getValue());
		assertEquals(true, isManaged(a, persistenceUnitProvider));
		assertNotNull(a.getNext());
	}

	@Test
	public void testEntityNotExists() throws Exception {
		Book book;
		try {
			book = genericService.load(Book.class, Long.MAX_VALUE);
		} catch (EntityNotFoundException e) {
			/*
			The persistence provider runtime is permitted to throw the EntityNotFoundException when getReference is
			called.
			*/
			return;
		}
		assertNotNull(book);
		assertEquals(true, dormancy.getUtils().isProxy(book.getClass()));
		assertEquals(BeanUtils.describe(new Book()), BeanUtils.describe(dormancy.disconnect(book)));
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidIdentifier() throws Exception {
		dormancy.getUtils().getIdentifierValue(dormancy.getUtils().getMetadata(Book.class), new Book());
	}
}
