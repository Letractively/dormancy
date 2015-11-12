# Getting started with Guice #

Dormancy provides good integration with Guice.
Even though [Spring](UsingSpring.md) has better AOP support and API abstractions for common use cases, Guice´s capabilities are more than sufficient.

To make this more clearly, here is an example of how Guice can be used to serialize a custom class:

## Declaring Dormancy ##

At the very first, a `Dormancy` instance has to be configured. There are two main possibilities.

**Approach 1**: Use the JPA provider specific Dormancy implementation like `HibernateDormancy` or `JpaDormancy`.
```
public class DormancyScenarioModule extends AbstractModule {
	protected void configure() {
		// Prepare Dormancy
		bind(Dormancy.class).to(HibernateDormancy.class).in(Scopes.SINGLETON);
		// If you are using an EntityManagerFactory, use the following line instead:
		// bind(Dormancy.class).to(JpaDormancy.class).in(Scopes.SINGLETON);
	}
}
```

**Approach 2**: Declare a `Dormancy` instance as well as a `PersistenceUnitProvider` such as `HibernatePersistenceProvider` or `JpaPersistenceProvider` or use a custom implementation for providing the persistence unit:
```
		bind(EntityPersister.class).to(Dormancy.class).in(Scopes.SINGLETON);
		// Use the next line in case of Hibernate´s SessionFactory is used
		bind(PersistenceUnitProvider.class).to(HibernatePersistenceUnitProvider.class).in(Scopes.SINGLETON);
		// or use the next line in case of a EntityManagerFactory is used
		bind(PersistenceUnitProvider.class).to(JpaPersistenceUnitProvider.class).in(Scopes.SINGLETON);
```

If it might happen that the JPA provider might change over time e.g., because of an heterogenous IT landscape, the second approach is prefered. Otherwise it is recommended to use either `HibernateDormancy` if a `SessionFactory` is used or `JpaDormancy` if an `EntityManagerFactory` is used.

You can find an entire Guice Module in the sample project: [DormancyScenarioModule](http://code.google.com/p/dormancy/source/browse/trunk/scenario/src/main/java/at/dormancy/scenario/server/DormancyScenarioModule.java)