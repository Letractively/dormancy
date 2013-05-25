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
package at.schauer.gregor.dormancy;

import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.DataTypes;
import at.schauer.gregor.dormancy.entity.Employee;
import at.schauer.gregor.dormancy.service.Service;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;

import static at.schauer.gregor.dormancy.util.HibernateVersionUtils.getHibernateCollectionClass;

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
	protected Dormancy dormancy;

	@PostConstruct
	public void postConstruct() {
		Session session = sessionFactory.openSession();

		Book book = new Book("Book");
		Employee a = new Employee("A", null);
		Employee b = new Employee("B", a);
		Employee c = new Employee("C", b);
		Application app = new Application("Application", b, Collections.singleton(c), "authKey");
		DataTypes dataTypes = new DataTypes(1L, 2, true, true, "string", new Date(), new Timestamp(new Date().getTime()), new int[]{3}, new Integer[]{4});

		session.save(book);
		session.save(a);
		session.save(b);
		session.save(c);
		session.save(app);
		session.save(dataTypes);

		a.getEmployees().add(b);
		b.getEmployees().add(c);
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

	public static boolean isManaged(@Nonnull Object entity, @Nonnull Session session) {
		return isProxy(entity, session) || session.isOpen() && session.contains(entity);
	}

	public static boolean isProxy(@Nonnull Object entity, @Nonnull Session session) {
		if (entity instanceof HibernateProxy || getHibernateCollectionClass().isAssignableFrom(entity.getClass())) {
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
					if (value != null && getHibernateCollectionClass().isAssignableFrom(value.getClass())) {
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
}
