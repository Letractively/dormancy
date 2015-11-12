## Changes ##



### 2.0.0 (2012-08-11) ###
  * Completely rewritten Dormancy core
  * Added JPA (Hibernate and EclipseLink) support
  * Added support for `@IdClass`
  * Bean property access now takes care of `@Access` and `@AccessType`
  * Improved support for version columns
  * Introduced `StrategyPropertyAccessor` for custom bean property access
  * Introduced `EntityCallback`
  * Removed support for Spring´s `HibernateCallback`
  * `@Transient` properties are now ignored by default
  * Removed compile time dependency to commons-beanutils
  * Updated libraries:
    * AspectJ to version 1.7.3
    * JUnit to version 4.11
  * For a detailed information about the changes, please refer to [Features 2.0](Features2x.md)

### 1.1.1 (2012-12-31) ###
  * Added configuration flag `createEmptyCollections`
  * Dormancy now creates empty collections when cloning uninitialized `PersistentCollection`s by default

### 1.1.0 (2012-11-18) ###
  * Added built-in support for arrays and `Enum`s
  * Added `EntityPersisterConfiguration` flags `flushAutomatically` and `cloneObjects`
  * Removed experimental configuration flags `deleteRemovedEntities`, `saveAssociationsProperties`, `skipTransient` and `skipFinal`
  * Dormancy can now process object graphs with infinite depth automatically
  * Enhanced closure-like `ContextFunction`s to improve the extensibility of Dormancy
  * Improved overall performance significantly
    * Replaced default bean property access layer (`PropertyAccessor`s) with `Introspector` (API change!)
    * Added self-learning algorithm for faster determination of the `EntityPersister` to use
  * Reduced memory usage by reusing objects instead of cloning them (see `cloneObjects`)
  * Restructured project and dependencies by adding Maven profiles for switching the Hibernate version more easily
  * All classes (including tests) support Hibernate 3 and Hibernate 4 simultaneously
  * Updated libraries:
    * AspectJ to version 1.7.1
    * HSQLDB to version 2.2.9
    * JPA 2.0 API to version 1.0.1.Final
    * JSR 305 to version 2.0.1
    * JUnit to version 4.10
    * log4j to version 1.2.17

### 1.0.3 (2012-08-11) ###
  * Added `DynamicEntityPersister` that determines dynmically if certain object types can be processed
  * Added `SimplePersister` that processes all objects within certain packages
  * Renamed a few `EntityPersister`s and some small fixes to ensure consistent behaviour

### 1.0.2 (2012-08-02) ###
  * Added `HibernatePropertyAccessor` and `LazyInitializerPropertyAccessor`
  * `AbstractDormancyUtils` now automatically chooses the best `PropertyAccessor`
  * Added `PropertyAccessorPersister`s and `PredicatePersister` (supporting all Commons Collections `Predicate`s)

### 1.0.1 (2012-07-02) ###
  * Added `EntityPersisterConfiguration` flags `skipTransient` and `skipFinal`
  * Introduced `GenericEntityPersisters` (`BeanPropertyPersister`, `FieldFilterEntityPersister`)
  * `ContextFunctions` replacing experimental Closures

### 1.0.0 (2012-05-29) ###
  * Initial release