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
package at.dormancy;

import at.dormancy.entity.Application;
import at.dormancy.entity.Book;
import at.dormancy.entity.DataTypes;
import at.dormancy.entity.Employee;
import at.dormancy.persistence.HibernatePersistenceUnitProvider;
import at.dormancy.service.GenericService;
import at.dormancy.service.Service;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;

import static at.dormancy.util.JpaProviderUtils.getPersistentCollectionClass;

/**
 * @author Gregor Schauer
 */
@ContextConfiguration(classes = DormancySpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public abstract class AbstractDormancyTest {
	@Inject
	protected SessionFactory sessionFactory;
	@Inject
	protected Service service;
	@Inject
	protected GenericService genericService;
	@Inject
	protected Dormancy<SessionFactory, Session, ClassMetadata> dormancy;
	@Inject
	protected HibernatePersistenceUnitProvider persistenceUnitProvider;

	// Test data
	protected final Book refBook = new Book("Book");
	protected final Employee refA = new Employee("A", null);
	protected final Employee refB = new Employee("B", refA);
	protected final Employee refC = new Employee("C", refB);
	protected final Application refApp = new Application("Application", refB, Collections.singleton(refC), "authKey");
	protected final DataTypes refDataTypes = new DataTypes(1L, 2, true, true, "string", new Date(), new Timestamp(new Date().getTime()), new int[]{3}, new Integer[]{4});

	@PostConstruct
	public void postConstruct() {
		Session session = sessionFactory.openSession();

		session.save(refBook);
		session.save(refA);
		session.save(refB);
		session.save(refC);
		session.save(refApp);
		session.save(refDataTypes);

		refA.getEmployees().add(refB);
		refB.getEmployees().add(refC);
		session.flush();
		session.close();
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	public static Map<String, ?> describe(@Nullable Object bean) {
		try {
			Map<String, ?> map = BeanUtils.describe(bean);
			map.remove("hibernateLazyInitializer");
			map.remove("handler");
			map.remove("class");
			return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isManaged(@Nonnull Object entity, @Nonnull HibernatePersistenceUnitProvider persistenceUnitProvider) {
		return isManaged(entity, persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext());
	}

	public static boolean isManaged(@Nonnull Object entity, @Nonnull Session session) {
		return isProxy(entity, session) || session.isOpen() && session.contains(entity);
	}

	public static boolean isProxy(@Nonnull Object entity, @Nonnull Session session) {
		if (entity instanceof HibernateProxy || getPersistentCollectionClass().isAssignableFrom(entity.getClass())) {
			return true;
		} else if (entity instanceof Iterable) {
			for (Object elem : (Iterable<?>) entity) {
				if (isProxy(elem, session)) {
					return true;
				}
			}
		} else {
			for (Field field : listFields(entity.getClass())) {
				try {
					Object value = field.get(entity);
					if (value != null && getPersistentCollectionClass().isAssignableFrom(value.getClass())) {
						return true;
					}
				} catch (IllegalAccessException e) {
					// must not happen
					throw new RuntimeException(e);
				}
			}
		}
		return false;
	}

	@Nonnull
	protected static List<Field> listFields(@Nonnull Class<?> clazz) {
		final List<Field> list = new ArrayList<Field>();
		ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) {
				ReflectionUtils.makeAccessible(field);
				list.add(field);
			}
		});
		return list;
	}

	/**
	 * This method is a workaround for some tests that are not compatible with JPA but with Hibernate.
	 *
	 * @return {@code true} if JPA is used, {@code false} otherwise
	 * @deprecated this method will be removed as soon as possible
	 */
	@Deprecated
	protected boolean isJpa() {
		return dormancy.getUtils().getPersistenceContext() instanceof EntityManager;
	}
}
