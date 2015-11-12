# Getting started with Spring #

Dormancy provides great integration with Spring.
Another huge benefit is that Spring can initialize all `EntityPersister`s as well as a `SessionFactory` or `EntityManagerFactory`.

To make this more clearly, here is an example of how Spring can be used to serialize a custom class:

## Declaring Dormancy ##

Spring scans for `EntityPersister`s in the classpath and registers them for the appropriate entity types:

```
<ctx:component-scan base-package="org.acme" use-default-filters="false">
	<ctx:include-filter type="assignable" expression="at.dormancy.persister.EntityPersister"/>
</ctx:component-scan>
```

Next, a `Dormancy` instance has to be configured. There are two main possibilities.

**Approach 1**: Use the JPA provider specific Dormancy implementation like `HibernateDormancy` or `JpaDormancy`.
```
<bean class="at.dormancy.HibernateDormancy"/>	
<!-- If you are using an EntityManagerFactory, use the following line instead: -->
<!-- <bean class="at.dormancy.JpaDormancy"/> -->
```

**Approach 2**: Declare a `Dormancy` instance as well as a `PersistenceUnitProvider` such as `HibernatePersistenceProvider` or `JpaPersistenceProvider` or use a custom implementation for providing the persistence unit:
```
<bean class="at.dormancy.Dormancy"/>

<!-- Use the next line in case of Hibernate´s SessionFactory is used -->
<bean class="at.dormancy.persistence.HibernatePersistenceUnitProvider"/>
<!-- or use the next line in case of a EntityManagerFactory is used -->
<bean class="at.dormancy.persistence.JpaPersistenceUnitProvider"/>
```

If it might happen that the JPA provider might change over time e.g., because of an heterogenous IT landscape, the second approach is prefered. Otherwise it is recommended to use either `HibernateDormancy` if a `SessionFactory` is used or `JpaDormancy` if an `EntityManagerFactory` is used.

## Configuration ##

Several properties can also be set declarativly if necessary.

The following listing outlines a alternative configuration:

```
<bean class="at.dormancy.Dormancy">
	<property name="config">
		<bean class="at.dormancy.EntityPersisterConfiguration">
			<!-- Advice Dormancy to ignore the version attribute -->
			<property name="versionChecking" value="false"/>
		</bean>
	</property>
	<!-- By default, the default EntityPersisters are registered allowing traversal of collections and maps -->
	<property name="registerDefaultEntityPersisters" value="false"/>
	<!-- Setting the SessionFactory is mandatory only if multiple SessionFactories exist -->
	<property name="sessionFactory" ref="sessionFactory"/>
</bean>
```

Please refer to the [Entity Persister Configuration](EntityPersisterConfiguration.md) section for a brief enumeration of all options.

Furthermore, the initial entity persister mapping can be defined by declaring a map with the object type as the key and the `EntityPersister` as the value.

```
<bean class="at.dormancy.Dormancy">
	<property name="persisterMap">
		<map>
			<entry key="org.acme.model.Customer">
				<bean class="org.acme.persister.CustomerPersister"/>
			</entry>
		</map>
	</property>
</bean>
```