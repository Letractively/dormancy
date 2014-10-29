package at.dormancy.scenario.server;

import at.dormancy.Dormancy;
import at.dormancy.HibernateDormancy;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import org.apache.commons.collections.MapUtils;
import org.hibernate.SessionFactory;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * @author Gregor Schauer
 * @since 2.0.0
 */
public class DormancyScenarioModule extends AbstractModule {
	@SuppressWarnings("unchecked")
	protected void configure() {
		// Prepare Dormancy
		bind(Dormancy.class).to(HibernateDormancy.class).in(Scopes.SINGLETON);


		// Prepare the DataSource
		SingleConnectionDataSource dataSource = new SingleConnectionDataSource("jdbc:hsqldb:mem:database", true);
		bind(DataSource.class).toInstance(dataSource);

		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
//		databasePopulator.addScript(new ClassPathResource("/WEB-INF/init.sql"));
		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDatabasePopulator(databasePopulator);
		dataSourceInitializer.setDataSource(dataSource);
		dataSourceInitializer.afterPropertiesSet();


		// Prepare the SessionFactory
		Properties hibernateProperties = MapUtils.toProperties(ImmutableMap.of("hibernate.hbm2ddl.auto", "create"));
		/*
		 * For the sake of simplicity, Spring´s AnnotationSessionFactoryBean is used here.
		 * Typically, an EntityManager could be provided by the application server or ...
		 */
		AnnotationSessionFactoryBean sessionFactoryBean = new AnnotationSessionFactoryBean();
		sessionFactoryBean.setPackagesToScan(new String[]{"at.dormancy.scenario.shared.model"});
		sessionFactoryBean.setHibernateProperties(hibernateProperties);
		sessionFactoryBean.setDataSource(dataSource);
		try {
			sessionFactoryBean.afterPropertiesSet();
			bind(SessionFactory.class).toInstance(sessionFactoryBean.getObject());
		} catch (Exception e) {
			addError(e);
		}


		// Configure transaction management
		// Like the SessionFactory, the transaction management is done by using Spring.
		PlatformTransactionManager transactionManager =
				new HibernateTransactionManager(sessionFactoryBean.getObject());
		bind(PlatformTransactionManager.class).toInstance(transactionManager);

		TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
		transactionInterceptor.setTransactionManager(transactionManager);
		transactionInterceptor.setTransactionAttributeSource(new MatchAlwaysTransactionAttributeSource());
		transactionInterceptor.afterPropertiesSet();
		bindInterceptor(Matchers.annotatedWith(Transactional.class), Matchers.any(), transactionInterceptor);


		/*
		 * Scan for @Transactional components in the classpath and register them if possible.
		 * This works similar to Springs component scan. However, it is not that feature rich.
		 */
		Iterable<Class<?>> classes = ImmutableList.of();
		try {
			ImmutableSet<ClassPath.ClassInfo> classInfos = ClassPath.from(getClass().getClassLoader())
					.getTopLevelClassesRecursive("at.dormancy.scenario");
			classes = transform(classInfos, new Function<ClassPath.ClassInfo, Class<?>>() {
				@Nonnull
				@Override
				public Class<?> apply(ClassPath.ClassInfo input) {
					return input.load();
				}
			});
		} catch (IOException e) {
			addError(e);
		}

		classes = filter(classes, new Predicate<Class<?>>() {
			@Override
			public boolean apply(Class<?> input) {
				return input.isAnnotationPresent(Transactional.class);
			}
		});

		for (Class clazz : classes) {
			for (Class<?> iface : ClassUtils.getAllInterfacesForClassAsSet(clazz)) {
				bind(iface).to(clazz).in(Scopes.SINGLETON);
			}
		}
	}
}
