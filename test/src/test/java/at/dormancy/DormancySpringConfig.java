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

import at.dormancy.aop.DormancyAdvisor;
import at.dormancy.aop.ServiceInterceptor;
import at.dormancy.handler.ObjectHandler;
import at.dormancy.persistence.PersistenceUnitProvider;
import at.dormancy.service.GenericService;
import at.dormancy.service.Service;
import at.dormancy.util.ClassLookup;
import at.dormancy.util.PersistenceContextHolder;
import org.aopalliance.aop.Advice;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;

import static at.dormancy.util.PersistenceProviderUtils.*;
import static org.springframework.beans.BeanUtils.instantiateClass;

/**
 * @author Gregor Schauer
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(value = "at.dormancy.handler",
		includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ObjectHandler.class))
public class DormancySpringConfig {
	@Bean
	public PersistenceContextHolder<?> persistenceContextHolder() {
		Class<PersistenceContextHolder<?>> clazz = ClassLookup.find(
				"at.dormancy.util.HibernatePersistenceContextHolder",
				"at.dormancy.util.JpaPersistenceContextHolder").get();
		return instantiateClass(clazz, PersistenceContextHolder.class);
	}

	@Bean
	@SuppressWarnings("unchecked")
	public PersistenceUnitProvider<?, ?, ?> persistenceUnitProvider(@Named("persistenceUnit") Object sessionFactory)
			throws Exception {
		Class<? extends PersistenceUnitProvider> clazz = ClassLookup.find(
				"at.dormancy.persistence.HibernatePersistenceUnitProvider",
				"at.dormancy.persistence.JpaPersistenceUnitProvider").get();
		Class<? extends PersistenceUnitProvider> type = (Class<? extends PersistenceUnitProvider>)
				getPersistenceUnitProviderType(sessionFactory);
		Constructor<? extends PersistenceUnitProvider> ctor =
				ConstructorUtils.getMatchingAccessibleConstructor(clazz, new Class[]{type});
		return instantiateClass(ctor, sessionFactory);
	}

	private Class<?> getPersistenceUnitProviderType(Object sessionFactory) {
		if (sessionFactory instanceof EntityManagerFactory) {
			return EntityManagerFactory.class;
		} else {
			return ClassLookup.find("org.hibernate.SessionFactory").orThrow("org.hibernate.SessionFactory").get();
		}
	}

	@Bean
	public Dormancy<Object, Object, Object> dormancy(
			PersistenceUnitProvider<Object, Object, Object> persistenceUnitProvider) throws Exception {
		DormancyConfiguration config = new DormancyConfiguration();
		Dormancy<Object, Object, Object> dormancy = new Dormancy<Object, Object, Object>(persistenceUnitProvider);
		dormancy.config = config;
		return dormancy;
	}

	@Bean
	public FactoryBean<?> persistenceUnit() throws IOException {
		if (isJpa()) {
			String providerImpl;
			if (isEclipseLink()) {
				providerImpl = "org.eclipse.persistence.jpa.PersistenceProvider";
			} else if (isOpenJpa()) {
				providerImpl = "org.apache.openjpa.persistence.PersistenceProviderImpl";
			} else {
				providerImpl = "org.hibernate.ejb.HibernatePersistence";
			}
			LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
			entityManagerFactory.setPersistenceProviderClass(ClassLookup.<PersistenceProvider>forName(providerImpl));
			entityManagerFactory.setDataSource(dataSource());
			entityManagerFactory.setPackagesToScan(DormancySpringConfig.class.getPackage().getName());
			entityManagerFactory.setJpaProperties(jpaProperties());
			return entityManagerFactory;
		} else if (isHibernate3()) {
			AnnotationSessionFactoryBean sessionFactory = new AnnotationSessionFactoryBean();
			sessionFactory.setDataSource(dataSource());
			sessionFactory.setPackagesToScan(new String[]{DormancySpringConfig.class.getPackage().getName()});
			sessionFactory.setHibernateProperties(hibernateProperties());
			return sessionFactory;
		} else if (isHibernate4()) {
			LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
			sessionFactory.setDataSource(dataSource());
			sessionFactory.setPackagesToScan(DormancySpringConfig.class.getPackage().getName());
			sessionFactory.setHibernateProperties(hibernateProperties());
			return sessionFactory;
		} else {
			LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
			entityManagerFactory.setPersistenceProviderClass(ClassLookup.<PersistenceProvider>forName(
					isEclipseLink()
							? "org.eclipse.persistence.jpa.PersistenceProvider"
							: "org.hibernate.ejb.HibernatePersistence"));
			entityManagerFactory.setDataSource(dataSource());
			entityManagerFactory.setPackagesToScan(DormancySpringConfig.class.getPackage().getName());
			entityManagerFactory.setJpaProperties(jpaProperties());
			return entityManagerFactory;
		}
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return builder.setType(EmbeddedDatabaseType.HSQL).build();
	}

	@Bean
	public PlatformTransactionManager transactionManager(@Named("persistenceUnit") Object persistenceUnit)
			throws Exception {
		Class<? extends PlatformTransactionManager> clazz;
		Class<?> type = getPersistenceUnitProviderType(persistenceUnit);
		if (type.isAssignableFrom(EntityManagerFactory.class)) {
			clazz = org.springframework.orm.jpa.JpaTransactionManager.class;
		} else if (isHibernate3()) {
			clazz = org.springframework.orm.hibernate3.HibernateTransactionManager.class;
		} else if (isHibernate4()) {
			clazz = org.springframework.orm.hibernate4.HibernateTransactionManager.class;
		} else {
			clazz = org.springframework.orm.jpa.JpaTransactionManager.class;
		}

		@SuppressWarnings("unchecked")
		Constructor<? extends PlatformTransactionManager> constructor =
				ConstructorUtils.getMatchingAccessibleConstructor(clazz, new Class[]{type});
		return instantiateClass(constructor, persistenceUnit);
	}

	@Bean
	public Properties hibernateProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		properties.setProperty("hibernate.show_sql", "false");
		properties.setProperty("hibernate.hbm2ddl.auto", "create");
		properties.setProperty("current_session_context_class", "thread");
		properties.setProperty("javax.persistence.validation.mode", "none");
		return properties;
	}

	@Bean
	public Properties jpaProperties() {
		Properties properties = new Properties();
		if (isEclipseLink()) {
			// Comment the following line to enable load-time weaving for EclipseLink
			properties.setProperty("eclipselink.weaving", "false");
			properties.setProperty("eclipselink.ddl-generation", "create-tables");
		} else if (isOpenJpa()) {
			properties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
		} else {
			properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
			properties.setProperty("hibernate.show_sql", "false");
			properties.setProperty("hibernate.hbm2ddl.auto", "create");
			properties.setProperty("current_session_context_class", "thread");
			properties.setProperty("javax.persistence.validation.mode", "none");
		}
		return properties;
	}

	@Bean
	public Service service(DormancyAdvisor dormancyAdvisor, GenericService genericService) throws Exception {
		ProxyFactoryBean factoryBean = new ProxyFactoryBean();
		factoryBean.addAdvice(serviceInterceptor());
		factoryBean.addAdvice(dormancyAdvisor);
		factoryBean.setTarget(genericService);
		return (Service) factoryBean.getObject();
	}

	@Bean
	public Advice serviceInterceptor() {
		return new ServiceInterceptor();
	}

	@Bean
	public DormancyAdvisor dormancyAdvisor(Dormancy<?, ?, ?> dormancy) throws Exception {
		DormancyAdvisor support = new DormancyAdvisor(dormancy);
		support.setMode(DormancyAdvisor.Mode.BOTH);
		return support;
	}

	@Bean
	public GenericService genericService() {
		Class<GenericService> clazz = ClassLookup.find(
				"at.dormancy.service.HibernateServiceImpl",
				"at.dormancy.service.JpaServiceImpl").get();
		return instantiateClass(clazz, GenericService.class);
	}

	private boolean isJpa() {
		return isHibernateJpa() || isOpenJpa();
	}

	private boolean isHibernateJpa() {
		return ClassLookup.forName("org.hibernate.ejb.EntityManagerFactoryImpl") != null;
	}

	private boolean isOpenJpa() {
		return ClassLookup.forName("org.apache.openjpa.persistence.EntityManagerFactoryImpl") != null;
	}
}
