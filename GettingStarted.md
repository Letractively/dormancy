# Getting Started #
### Using Dormancy in your project ###
Please refer to the following page for [using Dormancy in your project](GettingDormancy.md).

### Getting more information ###
To get a deeper understand of how Dormancy works, it is recommended to have a look at the [architecture](Architecture.md).

### Starting to Code ###
At the very first a `Dormancy` instance has to be created.
This can be done manually by creating a new instance or using a dependency injection framework like Spring.
For further information about using Dormancy in Spring-based applications see [Using Spring](UsingSpring.md) or [Using Guice](UsingGuice.md) if Google Guice is used.
An equivalent Guice module can be found in the `scenario` module.

For the sake of simplicity, we assume that the database configuration including `DataSource`, `SessionFactory` (or `EntityManagerFactory`) and `TransactionManager` is already done. The following listing outlines a minimal Spring configuration (typically located in `WEB-INF/applicationContext.xml`):
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xmlns:jee="http://www.springframework.org/schema/jee" xmlns:tx="http://www.springframework.org/schema/tx"
	   xmlns:ctx="http://www.springframework.org/schema/context" xmlns:aop="http://www.springframework.org/schema/aop"
	   xmlns:c="http://www.springframework.org/schema/c" xmlns:p="http://www.springframework.org/schema/p"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
	   http://www.springframework.org/schema/jee http://www.springframework.org/schema/jee/spring-jee.xsd"
	   http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-3.1.xsd
	   http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.1.xsd
	   http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd>

	<tx:annotation-driven proxy-target-class="true" order="0"/>

	<ctx:component-scan base-package="at.dormancy.scenario">
		<ctx:include-filter type="annotation" expression="org.springframework.transaction.annotation.Transactional" />
	</ctx:component-scan>

	<bean id="transactionManager" class="org.springframework.orm.hibernate3.HibernateTransactionManager"
		  p:sessionFactory-ref="sessionFactory"/>

	<bean id="sessionFactory" class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean"
		  lazy-init="false" p:packagesToScan="at.dormancy.scenario.shared.model" p:dataSource-ref="dataSource"/>

	<jee:jndi-lookup id="dataSource" jndi-name="jndi/jdbc/dataSource"/>

</beans>
```

To configure Dormancy to use the defined `SessionFactory`, simply add the following line:

```
	<bean class="at.dormancy.HibernateDormancy"/>	
	<!-- If you are using an EntityManagerFactory, use the following line instead: -->
	<!-- <bean class="at.dormancy.JpaDormancy"/> -->
```

That´s it! Now you can inject the `Dormancy` instance into your backend code like the `SessionFactory` or `EntityManagerFactory`, respectively.
```
@Transactional
public class DormancyScenarioDao {
	@Inject
	SessionFactory sessionFactory;
	@Inject
	HibernateDormancy dormancy;
	// or JpaDormancy

	// methods ommitted...
}
```

Whenever a method retrieves a parameter, which should be merged into the database or it returns a parameter containing unserializable Hibernate/JPA proxies, simply use the provided `Dormancy` instance:
```
	@SuppressWarnings("unchecked")
	public List<Employee> listEmployees() {
		List<Employee> employees = (List<Employee>) sessionFactory.getCurrentSession()
			.createQuery("FROM Employee").list();
		/*
		 * Remove all unitialized proxies by setting their references to null
		 * and replace all initialized proxies by plain JPA entities.
		 */
		return dormancy.clone(employees);
	}

	public void save(Employee employee) {
		/*
		 * Merge the transient "employee" with the persistent counterpart from the database.
		 * "merged" is the persistent JPA entity with all changes applied to it.
		 */
		Employee merged = dormancy.merge(employee);
	}
}
```

It´s on the dice that Dormancy works like an aspect i.e., whenever a transient JPA entity is retrieved, it is very likely that the changes should be persisted. Moreover, every persistent JPA entity, which is returned to the client, has to be detached and all proxies including persistent collections have to be replaced by serializable objects. Thus Dormancy can be configured as either an AspectJ aspect or a `MethodInterceptor` applied by a CGLIB or Java dynamic proxy.

For further information about configuring your application to use Dormancy as a transparent facade between client and GWT services, please refer to [AOP Support](AOPSupport.md).