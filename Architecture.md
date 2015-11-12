# Architecture #

Dormancy is designed to provide a compact and easy to use solution for cloning graphs of persistent entities as well as merging transient entities with their persistent pendants. However, it also has great capabilities in terms of fine-tuning and customization for complex business scenarios. Thus custom `EntityPersisters` can be registered for processing certain entities. These implementations even may perform conditional cloning and merging depending on the properties of the entities or any external context.

Further details about implementing `EntityPersister`s for specific entities can be found in the chapter [Custom EntityPersisters](CustomEntityPersisters.md).

## Typical Scenario ##

When the GWT `RemoteServiceServlet` receives a client request, it creates Java objects for the parameters and invokes the desired method of a `RemoteService`. For every JPA entity received, Dormancy retrieves the persistent entity from the database (if possible) and seamlessly merges both object graphs by copying the modified properties. Afterwards, the objects can be processed in the business logic layer.

Since the objects are attached JPA entities, they may be proxies or contain proxies that do the lazy loading of persistent collections if necessary. Moreover, any changes made to the objects are committed as soon as the Hibernate `Session` or the `EntityManager` containing the persistent entities is flushed successfully.

In the end, if some JPA entities should be returned to the client, Dormancy transparently does all necessary operations i.e., cloning object graphs recursively and removing unserializable Hibernate proxies, so that they can be serialized to the client as usual.

The following picture outlines the previously mentioned steps:

![http://dormancy.googlecode.com/svn/wiki/images/architecture.png](http://dormancy.googlecode.com/svn/wiki/images/architecture.png)