package at.schauer.gregor.dormancy;

import at.schauer.gregor.dormancy.interceptor.DormancyInterceptor;
import at.schauer.gregor.dormancy.interceptor.ServiceInterceptor;
import at.schauer.gregor.dormancy.persister.*;
import at.schauer.gregor.dormancy.service.Service;
import at.schauer.gregor.dormancy.service.ServiceImpl;
import org.aopalliance.aop.Advice;
import org.hibernate.dialect.HSQLDialect;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Gregor Schauer
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(value = "at.schauer.gregor.dormancy.persister",
		includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = EntityPersister.class),
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = Dormancy.class)
//				, @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = FunctionPersister.class)
		})
public class DormancySpringConfig {
	@Bean
	public Dormancy dormancy() {
		return new Dormancy();
	}

	@Bean
	public AnnotationSessionFactoryBean sessionFactory() {
		AnnotationSessionFactoryBean sessionFactory = new AnnotationSessionFactoryBean();
		sessionFactory.setDataSource(dataSource());
		sessionFactory.setPackagesToScan(new String[] {this.getClass().getPackage().getName()});
		sessionFactory.setHibernateProperties(hibernateProperties());
		return sessionFactory;
	}

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DataSource dataSource() {
		return new SingleConnectionDataSource("jdbc:hsqldb:mem:db_" + System.nanoTime(), true);
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
		factoryBean.addAdvice(dormancyInterceptor());
		factoryBean.setTarget(serviceImpl());
		return (Service) factoryBean.getObject();
	}

	@Bean
	public Advice serviceInterceptor() {
		return new ServiceInterceptor();
	}

	@Bean
	public Advice dormancyInterceptor() {
		return new DormancyInterceptor(dormancy());
	}

	@Bean
	public Service serviceImpl() {
		return new ServiceImpl();
	}
}
