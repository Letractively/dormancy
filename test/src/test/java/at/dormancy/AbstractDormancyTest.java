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
package at.dormancy;

import at.dormancy.access.AccessType;
import at.dormancy.entity.Application;
import at.dormancy.entity.Book;
import at.dormancy.entity.DataTypes;
import at.dormancy.entity.Employee;
import at.dormancy.persistence.PersistenceUnitProvider;
import at.dormancy.service.GenericService;
import at.dormancy.service.Service;
import at.dormancy.util.PersistenceContextHolder;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Log4jConfigurer;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.util.*;

import static at.dormancy.util.PersistenceProviderUtils.getPersistentCollectionClass;
import static at.dormancy.util.PersistenceProviderUtils.getPersistentMapClass;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;

/**
 * @author Gregor Schauer
 */
@ContextConfiguration(classes = DormancySpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public abstract class AbstractDormancyTest {
	@Inject
	protected Service service;
	@Inject
	protected GenericService genericService;
	@Inject
	protected Dormancy<Object, Object, Object> dormancy;
	@Inject
	protected PersistenceUnitProvider<Object, Object, Object> persistenceUnitProvider;
	@Inject
	protected PersistenceContextHolder<?> persistenceContextHolder;

	// Test data
	protected final Book refBook = new Book("Book");
	protected final Employee refA = new Employee("A", null);
	protected final Employee refB = new Employee("B", refA);
	protected final Employee refC = new Employee("C", refB);
	protected final Application refApp = new Application("Application", refB,
			new LinkedHashSet<Employee>(ImmutableSet.of(refC)), "authKey");
	protected final DataTypes refDataTypes = new DataTypes(1L, 2, true, true, "string",
			new Date(), new Timestamp(new Date().getTime()), Calendar.getInstance(), TimeZone.getDefault(),
			Currency.getInstance(Locale.GERMANY), Locale.GERMANY,
			DataTypes.class, AccessType.PROPERTY, null, UUID.randomUUID(),
			null, null);

	static {
		try {
			Log4jConfigurer.initLogging(Log4jConfigurer.CLASSPATH_URL_PREFIX + "log4j.properties");
		} catch (FileNotFoundException e) {
			throw Throwables.propagate(e);
		}
	}

	@PostConstruct
	public void postConstruct() {
		persistenceContextHolder.open();

		persistenceContextHolder.save(refBook);
		persistenceContextHolder.save(refA);
		persistenceContextHolder.save(refB);
		persistenceContextHolder.save(refC);
		persistenceContextHolder.save(refApp);
		persistenceContextHolder.save(refDataTypes);

		refA.getEmployees().add(refB);
		refB.getEmployees().add(refC);

		persistenceContextHolder.flush();
		persistenceContextHolder.close();
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

	public static boolean isManaged(@Nonnull Object entity,
									@Nonnull PersistenceUnitProvider<?, ?, ?> persistenceUnitProvider) {
		return isManaged(entity, persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext());
	}

	public static boolean isManaged(@Nonnull Object entity, @Nonnull Object persistenceContext) {
		if (isProxy(entity, persistenceContext)) {
			return true;
		}
		try {
			return (Boolean) invokeMethod(persistenceContext, "isOpen")
					&& (Boolean) invokeMethod(persistenceContext, "contains", entity);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static boolean isProxy(@Nonnull Object entity, @Nonnull Object persistenceContext) {
		if (entity instanceof Proxy
				|| getPersistentCollectionClass().isAssignableFrom(entity.getClass())
				|| getPersistentMapClass().isAssignableFrom(entity.getClass())) {
			return true;
		} else if (entity instanceof Iterable) {
			for (Object elem : (Iterable<?>) entity) {
				if (isProxy(elem, persistenceContext)) {
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
	 * Checks whether JPA or Hibernate is used i.e., whether the persistence context is a {@link org.hibernate.Session}
	 * or an {@link javax.persistence.EntityManager}.
	 *
	 * @return {@code true} if JPA is used, {@code false} otherwise
	 */
	protected boolean isJpa() {
		return dormancy.getUtils().getPersistenceContext() instanceof EntityManager;
	}

	protected String getMessage(Iterable<Class<?>> exceptions) {
		return String.format("Expected one of the following exceptions: %s",
				Iterables.transform(exceptions, new Function<Class<?>, String>() {
					@Override
					public String apply(Class<?> input) {
						return input != null ? input.getName() : "";
					}
				}));
	}
}
