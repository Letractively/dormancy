# Features Dormancy 2.0 #

## Completely rewritten Dormancy core ##
The entire core of Dormancy has been rewritten so that the algorithm for traversing object graphs is located within the class `Dormancy`, which is shared among all modules. This enables the opportunity of providing implementation-specific utilities i.e., `DormancyUtils`. The later derives some basic functionality from `AbstractDormancyUtils`, which is also shared across modules. As a consequence, the Dormancy core does not depend on Hibernate anymore.

Furthermore, Dormancy makes use of JPA´s metamodel, which enables two great opportunities.
Firstly, it is used for increasing the compatibility to JPA and supporting additional features like `@IdClass`.
Secondly, the metadata can be obtained directly from the core and used to customize the processing of entities extensively e.g., by implementing `EntityPersister`s that rely on it.


## Added JPA (Hibernate and EclipseLink) support ##
The Hibernate 4 module was ported to JPA using Hibernate as persistence provider. In other words, Dormancy can be configured to use a `EntityManagerFactory` instead of a `SessionFactory`.
In a further step, the `jpa-hibernate` module was migrated to EclipseLink.

Note that the `jpa-eclipselink` module fulfills the same quality standards than the other modules i.e., all tests that do not rely on Hibernate features have been ported to JPA.


## Bean property access now takes care of `@Access` and `@AccessType` ##
Dormancy considers annotations applied to an entity class or property used to specify the access type. In particular, the annotations depend on the persistence provider:

  * Hibernate 3: `org.hibernate.annotations.AccessType`
  * Hibernate 4: `org.hibernate.annotations.AccessType` or `javax.persistence.Access`
  * JPA (Hibernate): `javax.persistence.Access`
  * JPA (EclipseLink): `javax.persistence.Access`


## Introduced `StrategyPropertyAccessor` for custom bean property access ##
The so called `StrategyPropertyAccessor` provides an easy way to implement custom entity handling by using an underlaying `PropertyAccessStrategy`. Dormancy is shipped with several strategies for the supported JPA providers to simplify the handling of properties and the way to access them. In other words, when an entity type is processed the first time, Dormancy scans it for annotations and collects the metadata such as `@Transient` so that the `StrategyPropertyAccessor` can process them differently.


## Improved support for version columns ##
The introduction of a persistence metadata facility in combination with `StrategyPropertyAccessor` enables out-of-the-box support of `@Version` columns. In particular, properties in the following form a supported without the need of implementing a custom `EntityPersister`:
```
@Access(javax.persistence.AccessType.FIELD)	// use either @Access or @AccessType
@AccessType("field")				// depending on the JPA provider
@Version
public Long getLastUpdate() {
	return lastUpdate;
}
```


## Introduced `EntityCallback` ##
Due to the modularization of the Dormancy core, the support for Spring´s `HibernateCallback` was dropped. Instead, a more generic `EntityCallback` was introduced, which can be used independently of the persistence provider.
```
EntityCallback<Employee, SessionFactory, Session, ClassMetadata> callback =
		new EntityCallback<Employee, SessionFactory, Session, ClassMetadata>() {
	@Override
	public Employee work(PersistenceUnitProvider<SessionFactory, Session, ClassMetadata> persistenceUnitProvider) {
		Session session = persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();
		return (Employee) session.createQuery("SELECT e FROM Employee e LEFT JOIN FETCH e.employees "
				+ "LEFT JOIN FETCH e.colleagues WHERE e.id = 1").uniqueResult();
	}
};

// The callback can be used standalone...
Employee persistentEmployee = callback.work(persistenceUnitProvider);
Employee transientEmployee = dormancy.clone(persistentEmployee);

// ...

// as well as passed to Dormancy
Employee merged = dormancy.merge(e, callback);
```
