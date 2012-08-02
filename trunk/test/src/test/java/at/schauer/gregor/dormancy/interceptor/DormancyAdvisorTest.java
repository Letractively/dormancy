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
package at.schauer.gregor.dormancy.interceptor;

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.persister.EntityPersister;
import at.schauer.gregor.dormancy.service.Service;
import at.schauer.gregor.dormancy.service.ServiceImpl;
import org.aopalliance.aop.Advice;
import org.apache.commons.lang.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.HSQLDialect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.Pointcut;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gregor Schauer
 */
@Configuration
@ContextConfiguration(classes = DormancyAdvisorTest.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(value = "at.schauer.gregor.dormancy.persister",
		includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = EntityPersister.class))
@RunWith(SpringJUnit4ClassRunner.class)
public class DormancyAdvisorTest {
	@Inject
	Service service;
	@Inject
	SessionFactory sessionFactory;
	@Inject
	DormancyAdvisor dormancyAdvisor;

	@Test
	@Transactional
	public void test() {
		sessionFactory.getCurrentSession().save(new Book(UUID.randomUUID().toString()));
		assertNotNull(service.load(Book.class, 1L));
	}

	@Test
	public void testPointcut() {
		Pointcut pointcut = dormancyAdvisor.getPointcut();

		assertEquals(true, pointcut.getClassFilter().matches(dormancyAdvisor.getClass()));
		assertEquals(true, pointcut.getClassFilter().matches(this.getClass()));
		assertEquals(true, pointcut.getClassFilter().matches(ServiceImpl.class));
		assertEquals(true, pointcut.getClassFilter().matches(Service.class));
		assertEquals(true, pointcut.getClassFilter().matches(Object.class));

		Method method = MethodUtils.getAccessibleMethod(ServiceImpl.class, "doNothing", new Class[0]);
		assertEquals(false, pointcut.getMethodMatcher().matches(method, ServiceImpl.class));
	}

	@Test
	public void testOrder() {
		assertEquals(Ordered.LOWEST_PRECEDENCE, dormancyAdvisor.getOrder());
		dormancyAdvisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
		assertEquals(Ordered.HIGHEST_PRECEDENCE, dormancyAdvisor.getOrder());

	}

	@Bean
	public Dormancy dormancy() {
		return new Dormancy();
	}

	@Bean
	public AnnotationSessionFactoryBean sessionFactory() {
		AnnotationSessionFactoryBean sessionFactory = new AnnotationSessionFactoryBean();
		sessionFactory.setDataSource(dataSource());
		sessionFactory.setPackagesToScan(new String[]{Book.class.getPackage().getName()});
		sessionFactory.setHibernateProperties(hibernateProperties());
		return sessionFactory;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return builder.setType(EmbeddedDatabaseType.HSQL).build();
		// return new SingleConnectionDataSource("jdbc:hsqldb:mem:db_" + System.nanoTime(), true);
		// return new SingleConnectionDataSource("jdbc:hsqldb:file:db/db;shutdown=true;hsqldb.write_delay=0", true);
	}

	@Bean
	public PlatformTransactionManager transactionManager() throws SQLException {
		return new HibernateTransactionManager(sessionFactory().getObject());
	}

	@Bean
	public Properties hibernateProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", HSQLDialect.class.getName());
		properties.setProperty("hibernate.show_sql", "false");
		properties.setProperty("current_session_context_class", "thread");
		properties.setProperty("hibernate.hbm2ddl.auto", "create");
		properties.setProperty("javax.persistence.validation.mode", "none");
		return properties;
	}

	@Bean
	public Advice serviceInterceptor() {
		return new ServiceInterceptor();
	}

	@Bean
	public DormancyAdvisorAspect dormancyInterceptor() {
		return new DormancyAdvisorAspect(dormancyAdvisor());
	}

	@Bean
	public DormancyAdvisor dormancyAdvisor() {
		DormancyAdvisor advisor = new DormancyAdvisor(dormancy());
		advisor.setAnnotationType(Transactional.class);
		return advisor;
	}

	@Bean
	public Service service() {
		return new ServiceImpl();
	}

	@Aspect
	static class DormancyAdvisorAspect {
		DormancyAdvisor delegate;

		public DormancyAdvisorAspect(DormancyAdvisor delegate) {
			this.delegate = delegate;
		}

		@Around("target(at.schauer.gregor.dormancy.service.ServiceImpl) && @target(org.springframework.transaction.annotation.Transactional)")
		public Object around(@Nonnull ProceedingJoinPoint joinPoint) throws Throwable {
			return delegate.around(joinPoint);
		}
	}
}
