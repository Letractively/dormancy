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

import at.schauer.gregor.dormancy.interceptor.DormancyAdvisor;
import at.schauer.gregor.dormancy.interceptor.ServiceInterceptor;
import at.schauer.gregor.dormancy.persister.EntityPersister;
import at.schauer.gregor.dormancy.service.Service;
import at.schauer.gregor.dormancy.service.ServiceImpl;
import org.aopalliance.aop.Advice;
import org.hibernate.dialect.HSQLDialect;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Gregor Schauer
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(value = "at.schauer.gregor.dormancy.persister",
		includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = EntityPersister.class))
public class DormancySpringConfig {
	@Bean
	public Dormancy dormancy() {
		EntityPersisterConfiguration config = new EntityPersisterConfiguration();
		config.setDeleteRemovedEntities(false);
		config.setSaveAssociationsProperties(true);
		config.setSaveNewEntities(true);
		config.setVersionChecking(true);
		Dormancy dormancy = new Dormancy();
		dormancy.setConfig(config);
		return dormancy;
	}

	@Bean
	public AnnotationSessionFactoryBean sessionFactory() {
		AnnotationSessionFactoryBean sessionFactory = new AnnotationSessionFactoryBean();
		sessionFactory.setDataSource(dataSource());
		sessionFactory.setPackagesToScan(new String[]{DormancySpringConfig.class.getPackage().getName()});
		sessionFactory.setHibernateProperties(hibernateProperties());
		return sessionFactory;
	}

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
	public Service service() {
		ProxyFactoryBean factoryBean = new ProxyFactoryBean();
		factoryBean.addAdvice(serviceInterceptor());
		factoryBean.addAdvice(dormancyAdvisor());
		factoryBean.setTarget(serviceImpl());
		return (Service) factoryBean.getObject();
	}

	@Bean
	public Advice serviceInterceptor() {
		return new ServiceInterceptor();
	}

	@Bean
	public Advice dormancyAdvisor() {
		DormancyAdvisor support = new DormancyAdvisor(dormancy());
		support.setMode(DormancyAdvisor.Mode.BOTH);
		return support;
	}

	@Bean
	public Service serviceImpl() {
		return new ServiceImpl();
	}
}
