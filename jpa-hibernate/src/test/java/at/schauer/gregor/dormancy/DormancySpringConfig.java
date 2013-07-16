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
package at.schauer.gregor.dormancy;

import at.schauer.gregor.dormancy.interceptor.DormancyAdvisor;
import at.schauer.gregor.dormancy.interceptor.ServiceInterceptor;
import at.schauer.gregor.dormancy.persistence.JpaPersistenceUnitProvider;
import at.schauer.gregor.dormancy.persister.EntityPersister;
import at.schauer.gregor.dormancy.service.GenericService;
import at.schauer.gregor.dormancy.service.Service;
import at.schauer.gregor.dormancy.service.ServiceImpl;
import at.schauer.gregor.dormancy.util.ClassLookup;
import at.schauer.gregor.dormancy.util.JpaProviderUtils;
import org.aopalliance.aop.Advice;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Gregor Schauer
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(value = "at.schauer.gregor.dormancy.persister",
		includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = EntityPersister.class))
public class DormancySpringConfig {
	public static final Level LOG_LEVEL = Level.WARN;
	@Inject
	ApplicationContext ctx;

	@Bean
	public JpaPersistenceUnitProvider persistenceUnitProvider() throws Exception {
		JpaPersistenceUnitProvider persistenceUnit = new JpaPersistenceUnitProvider();
		ctx.getAutowireCapableBeanFactory().autowireBean(persistenceUnit);
		return persistenceUnit;
	}

	@Bean
	public Dormancy<EntityManagerFactory, EntityManager, EntityType<?>> dormancy() throws Exception {
		Logger.getLogger(Dormancy.class).setLevel(LOG_LEVEL);
		EntityPersisterConfiguration config = new EntityPersisterConfiguration();
		Dormancy<EntityManagerFactory, EntityManager, EntityType<?>> dormancy = new Dormancy<EntityManagerFactory, EntityManager, EntityType<?>>(persistenceUnitProvider());
		dormancy.setConfig(config);
		return dormancy;
	}

	@Bean
	public FactoryBean<EntityManagerFactory> entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setPersistenceProviderClass(ClassLookup.<PersistenceProvider>forName(
				JpaProviderUtils.isEclipseLink()
						? "org.eclipse.persistence.jpa.PersistenceProvider"
						: "org.hibernate.ejb.HibernatePersistence"));
		entityManagerFactory.setDataSource(dataSource());
		entityManagerFactory.setPackagesToScan(DormancySpringConfig.class.getPackage().getName());
		entityManagerFactory.setJpaProperties(jpaProperties());
		// Uncomment the following line to enable load-time weaving for EclipseLink
		// entityManagerFactory.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
		return entityManagerFactory;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return builder.setType(EmbeddedDatabaseType.HSQL).build();
	}

	@Bean
	public PlatformTransactionManager transactionManager() throws Exception {
		return new JpaTransactionManager(entityManagerFactory().getObject());
	}

	@Bean
	public Properties jpaProperties() {
		Properties properties = new Properties();
		if (JpaProviderUtils.isEclipseLink()) {
			// Comment the following line to enable load-time weaving for EclipseLink
			properties.setProperty("eclipselink.weaving", "false");
			properties.setProperty("eclipselink.ddl-generation", "create-tables");
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
	public Service service() throws Exception {
		ProxyFactoryBean factoryBean = new ProxyFactoryBean();
		factoryBean.addAdvice(serviceInterceptor());
		factoryBean.addAdvice(dormancyAdvisor());
		factoryBean.setTarget(genericService());
		return (Service) factoryBean.getObject();
	}

	@Bean
	public Advice serviceInterceptor() {
		return new ServiceInterceptor();
	}

	@Bean
	public Advice dormancyAdvisor() throws Exception {
		Logger.getLogger(DormancyAdvisor.class).setLevel(LOG_LEVEL);
		DormancyAdvisor support = new DormancyAdvisor(dormancy());
		support.setMode(DormancyAdvisor.Mode.RESULT);
		return support;
	}

	@Bean
	public GenericService genericService() {
		return new ServiceImpl();
	}
}
