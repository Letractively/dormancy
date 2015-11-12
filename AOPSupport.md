# AOP Support #

The `DormancyAdvisor` is capable of intercepting method invocations like service calls and transparently performs the appropriate clone or merge operations for the parameters and the return value, respectively.
It provides maximum flexibility by implementing `org.springframework.aop.support.AbstractPointcutAdvisor` as well as `org.aopalliance.intercept.MethodInterceptor`.
Thus it is possible to use it as a simple method interceptor for a Java dynamic proxies or CGLIB proxies which can be created comfortably by a `org.springframework.aop.framework.ProxyFactoryBean` for example.
Additionally, it contains a configurable `org.springframework.aop.Pointcut` that matches any methods annotated with `PersistenceEndpoint` or its declaring class is annotated with the annotation.

For more complex scenarios, it is possible to declare it as an AspectJ aspect instead of modifying the pointcut of the previous approach.
The following sections describe the alternatives in detail.

## Use `DormancyAdvisor` as an AspectJ aspect ##

This is the recommended approach because it can be configured completely declaratively and is more flexible than a `MethodInterceptor`.
It uses the `DormancyAdvisor` as an AspectJ aspect within a Spring-based application.
Therefore, Spring´s autoproxy facility has to be enabled, which uses the AspectJ weaver and a bytecode manipulation provider i.e., CGLIB for creating proxies of the objects matching any pointcuts.

In the following listing, a custom pointcut is defined that matches methods annotated with `org.springframework.transaction.annotation.Transactional` as well as methods of annotated classes.
In other words, whenever Spring applies a `org.springframework.transaction.interceptor.TransactionInterceptor` to an instance, the `DormancyAdvisor` is applied as well.

The `order` value must be chosen in a way that all aspects are applied in the correct order i.e., the `DormancyAdvisor` must have a lower precedence than the `TransactionInterceptor`.
Otherwise, the transaction used for loading entities, which are required for the merger with the transient objects, is not initialized in time and it will be closed before `Dormancy` can traverse the object graph for cloning them, which may result in a `org.hibernate.LazyInitializationException` in case of Hibernate or a `org.eclipse.persistence.exceptions.ValidationException` in case of EclipseLink.

```
<tx:annotation-driven proxy-target-class="true" order="0"/>
<aop:aspectj-autoproxy proxy-target-class="true"/>

<aop:config>
	<aop:pointcut id="persistenceEndpoint" expression="@within(org.springframework.transaction.annotation.Transactional)"/>
	<aop:advisor advice-ref="dormancyAspect" pointcut-ref="persistenceEndpoint" order="1000"/>
</aop:config>

<bean id="dormancyAspect" class="at.dormancy.aop.DormancyAdvisor">
	<constructor-arg ref="dormancy"/>
</bean>

<bean id="dormancy" class="at.dormancy.Dormancy"/>
```

Since the capabilities of Spring AOP and AspectJ support is a subset of AspectJ, the configuration can also be transformed to an equivalent AspectJ config.
For further information about defining pure AspectJ aspects, please refer to [The AspectJ Programming Guide](http://www.eclipse.org/aspectj/doc/released/progguide/index.html) and [The AspectJ 5 Development Kit Developer's Notebook](http://www.eclipse.org/aspectj/doc/released/adk15notebook/index.html).

## Use `DormancyAdvisor` as a `MethodInterceptor` ##

This alternative approach is easier to configure and only recommended for tests or small use cases.
The listing below creates a `ProxyFactoryBean`, which can also be defined declaratively in the `beans.xml` and applies the `DormancyAdvisor` as a `MethodInterceptor` to a target object.

```
Dormancy dormancy = ...		// obtain the Dormancy instance
ProxyFactoryBean factoryBean = new ProxyFactoryBean();
factoryBean.addAdvice(new DormancyAdvisor(dormancy));
factoryBean.setTarget(target);	// object to proxify
Object proxy = factoryBean.getObject();
```