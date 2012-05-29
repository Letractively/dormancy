package at.schauer.gregor.dormancy;

import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.entity.DataTypes;
import at.schauer.gregor.dormancy.entity.Employee;
import at.schauer.gregor.dormancy.service.Service;
import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.PersistentCollection;
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

/**
 * @author Gregor Schauer
 */
@ContextConfiguration(classes = DormancySpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

	// @After
	public void after() {
		sessionFactory.close();
		/*
		Session session = sessionFactory.getCurrentSession();

		Map<String, AbstractCollectionPersister> collectionPersisterMap = sessionFactory.getAllCollectionMetadata();
		for (AbstractCollectionPersister persister : collectionPersisterMap.values()) {
			session.createSQLQuery("DELETE FROM " + persister.getTableName()).executeUpdate();
		}
		for (Object entityName : sessionFactory.getAllClassMetadata().keySet()) {
			session.createQuery("DELETE FROM " + entityName).executeUpdate();
		}
		session.flush();
		*/
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

	public boolean isManaged(@Nonnull Object entity) {
		return isManaged(entity, sessionFactory.getCurrentSession());
	}

	public static boolean isManaged(@Nonnull final Object entity, @Nonnull Session session) {
		if (entity instanceof HibernateProxy || entity instanceof PersistentCollection) {
			return true;
		} else if (entity instanceof Iterable) {
			for (Object elem : (Iterable<?>) entity) {
				if (isManaged(elem, session)) {
					return true;
				}
			}
		} else {
			for (Field field : listFields(entity.getClass())) {
				try {
					Object value = field.get(entity);
					if (value instanceof PersistentCollection) {
						return true;
					}
				} catch (IllegalAccessException e) {
					// must not happen
					throw new RuntimeException(e);
				}
			}
		}
		return session.isOpen() && session.contains(entity);
	}

	@Nonnull
	protected static List<Field> listFields(@Nonnull Class<?> clazz) {
		final List<Field> list = new ArrayList<Field>();
		ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalAccessException {
				ReflectionUtils.makeAccessible(field);
				list.add(field);
			}
		});
		return list;
	}
}
