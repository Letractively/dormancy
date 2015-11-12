# EntityPersister Configuration #

Every `EntityPersister` (even `Dormancy` itself) might have an `EntityPersisterConfiguration` that contains the various configuration options for changing the behaviour massively.

| **Option**						| **Default Value**	| **Description**																						|
|:----------------|:------------------|:-------------------------------------|
| `saveNewEntities`			| `false`			        | Enables saving of new Hibernate entities without identifier											|
| `checkVersion`				| `true`			         | Enables version checking for Hibernate entities														|
| `flushAutomatically`			| `false`			        | Automatically flushes the current session after cloning												|
| `cloneObjects`				| `false`			        | Enables cloning of objects instead of modifying them													|
| `createEmptyCollections`		| `true`			         | Creates new empty collections when cloning entities with uninitialized persistent collections		|

**Note that the first option is fully supported but enabling them may lead to unexpected results and some side-effects may be difficult to discover. Use them with care!**