# Complex Queries #

In typical business use cases, there might be sequences of queries, which build an object graph by performing excessive batch processing or accumulative data operations.
Those operations should be executed atomically before the objects are passed to the next processing step or are returned to the user who sent the request.
Dormancy is capable of dealing with such requests. Furthermore, the performance can be improved significantly by providing a custom `EntityCallback` when huge object graphs have to be processed.

The listing below contains a service method retrieving and returning an `Employee` and any number of colleagues (a `Set` of `Employees`), which would be loaded lazily by default:

```
public Employee getEmployee(final Employee e) {
	EntityCallback<Employee, SessionFactory, Session, ClassMetadata> callback = new EntityCallback<Employee, SessionFactory, Session, ClassMetadata>() {
		@Override
		@Transactional
		public Employee work(PersistenceUnitProvider<SessionFactory, Session, ClassMetadata> persistenceUnitProvider) {
			Session session = persistenceUnitProvider.getPersistenceContextProvider().getPersistenceContext();
			Query query = session.createQuery(
					"SELECT e FROM Employee e " +
					"LEFT JOIN FETCH e.colleagues " +
					"WHERE e.id = :id");
			return (Employee) query.setLong("id", e.getId()).uniqueResult();
		}
	};

	Employee merged = dormancy.merge(e, callback);
	// Now the merged employee is the instance loaded by the EntityCallback
	// Its properties are the values of the provided employee e

	// Do some additional operations if necessary...

	// Return a copy of the employee, which has no uninitialized PersistentCollections
	return dormancy.clone(merged);
}
```