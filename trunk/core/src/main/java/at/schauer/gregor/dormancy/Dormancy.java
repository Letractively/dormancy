package at.schauer.gregor.dormancy;

import at.schauer.gregor.dormancy.persister.AbstractEntityPersister;
import at.schauer.gregor.dormancy.util.DormancyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.*;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.impl.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.Type;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.HibernateCallback;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static at.schauer.gregor.dormancy.util.DormancyUtils.*;

/**
 * Clones Hibernate entities and merges them into a {@link org.hibernate.Session}.<br/>
 *
 * @author Gregor Schauer
 * @see at.schauer.gregor.dormancy.persister.EntityPersister
 */
public class Dormancy extends AbstractEntityPersister<Object> implements ApplicationContextAware {
	protected Map<Class, AbstractEntityPersister> persisterMap = new HashMap<Class, AbstractEntityPersister>();
	protected SessionFactory sessionFactory;
	protected EntityPersisterConfiguration config;

	/**
	 * Initializes this instance.<br/>
	 * If no {@link EntityPersisterConfiguration} is set, a default configuration is created.
	 */
	@PostConstruct
	protected void initialize() {
		if (config == null) {
			config = new EntityPersisterConfiguration();
		}
	}

	@Override
	public final <T> T clone(T dbObj) {
		return this.clone_(dbObj, createAdjacencyMap());
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		// Check if the object has already been processed
		if (tree.containsKey(dbObj)) {
			return (T) tree.get(dbObj);
		}

		// Use an EntityPersister if possible
		AbstractEntityPersister entityPersister = getEntityPersister(dbObj != null ? dbObj.getClass() : null);
		if (entityPersister != null) {
			return (T) entityPersister.clone_(dbObj, tree);
		}

		// Verify that the given object is a non-null managed entity.
		ClassMetadata metadata = getClassMetadata(dbObj, sessionFactory);
		if (metadata == null) {
			return dbObj;
		}

		// Create a new instance of the same type
		T trObj = (T) BeanUtils.instantiateClass(DormancyUtils.getClass(dbObj));

		// Add the object to the adjacency list
		tree.put(dbObj, trObj);

		// Prepare the Hibernate utilities for gathering properties
		SessionImpl session = SessionImpl.class.cast(sessionFactory.getCurrentSession());

		// Retrieve the identifier of the persistent object
		Serializable identifier = getIdentifier(metadata, null, dbObj, session);

		// If the identifier cannot be retrieved via getter, try to access it directly.
		PropertyAccessor trPropertyAccessor = DormancyUtils.forBeanPropertyAccess(trObj), dbPropertyAccessor = null;
		if (identifier == null) {
			dbPropertyAccessor = DormancyUtils.forBeanPropertyAccess(dbObj);
			identifier = getIdentifierValue(metadata, dbPropertyAccessor, dbObj, session);
			// If the identifier is still null, an exception is thrown indicating that the entity is not persistent
			trPropertyAccessor = DormancyUtils.forBeanPropertyAccess(trObj);
		}

		// Process the properties
		String[] propertyNames = metadata.getPropertyNames();
		for (String propertyName : propertyNames) {
			Object dbValue = getPropertyValue(metadata, dbPropertyAccessor, dbObj, propertyName);

			// If the property (e.g., a lazy persistent collection) is not initialized, simply ignore it
			if (!Hibernate.isInitialized(dbValue)) {
				continue;
			}

			// Traverse the persistent object graph recursively
			if (dbValue != null) {
				dbValue = clone_((T) dbValue, tree);
			}

			setPropertyValue(metadata, trPropertyAccessor, trObj, propertyName, dbValue);
		}

		// Finally, copy the identifier
		setPropertyValue(metadata, trPropertyAccessor, trObj, metadata.getIdentifierPropertyName(), identifier);

		return trObj;
	}

	@Override
	public final <T> T merge(T trObj) {
		return merge_(trObj, createAdjacencyMap());
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		// Check if the object has already been processed
		if (tree.containsKey(trObj)) {
			return (T) tree.get(trObj);
		}

		// Use a EntityPersister if possible
		AbstractEntityPersister entityPersister = getEntityPersister(trObj != null ? trObj.getClass() : null);
		if (entityPersister != null) {
			return (T) entityPersister.merge_(trObj, tree);
		}

		// Verify that the given object is a non-null managed entity.
		ClassMetadata metadata = getClassMetadata(trObj, sessionFactory);
		if (metadata == null) {
			return trObj;
		}

		// Prepare the Hibernate utilities for gathering properties
		SessionImpl session = SessionImpl.class.cast(sessionFactory.getCurrentSession());

		// Retrieve the identifier of the persistent object
		Serializable identifier = getIdentifier(metadata, null, trObj, session);

		// If the identifier cannot be retrieved via getter, try to access it directly.
		if (identifier == null) {
			identifier = Serializable.class.cast(getIdentifier(metadata, DormancyUtils.forBeanPropertyAccess(trObj), trObj, session));

			// If the object has no identifier, it is considered to be new
			if (identifier == null) {
				if (config.getSaveNewEntities()) {
					// If desired, try to persist the object
					identifier = session.save(trObj);
				} else {
					// Otherwise throw an exception indicating that session.save() should be called
					throwNullIdentifierException(trObj, session);
				}
			}
		}

		// Retrieve the persistent object from the database
		T dbObj = (T) session.get(DormancyUtils.getClass(trObj), identifier);
		if (dbObj == null) {
			// Throw an exception indicating that the persistent object cannot be retrieved.
			throw new ObjectNotFoundException(identifier, DormancyUtils.getClass(trObj).getSimpleName());
		}

		return merge_(trObj, dbObj, tree);
	}

	/**
	 * Invokes the given {@link HibernateCallback} and passes its result to {@link #merge(Object, Object)}.
	 *
	 * @param trObj    the object to merge
	 * @param callback the callback to execute
	 * @return the merged object
	 * @throws SQLException if thrown by Hibernate-exposed JDBC API
	 */
	@Nullable
	public <T> T merge(@Nullable T trObj, @Nonnull HibernateCallback<T> callback) throws SQLException {
		return merge(trObj, trObj != null ? callback.doInHibernate(sessionFactory.getCurrentSession()) : null);
	}

	@Override
	public final <T> T merge(T trObj, T dbObj) {
		return merge_(trObj, dbObj, createAdjacencyMap());
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		// Check if the object has already been processed
		if (tree.containsKey(trObj)) {
			return (T) tree.get(trObj);
		}

		// Use an EntityPersister if possible
		AbstractEntityPersister entityPersister = getEntityPersister(trObj != null ? trObj.getClass() : null);
		if (entityPersister != null) {
			return (T) entityPersister.merge_(trObj, dbObj, tree);
		}

		// Verify that the given object is a non-null managed entity.
		ClassMetadata metadata = getClassMetadata(trObj, sessionFactory);
		if (metadata == null) {
			return trObj;
		}

		// Add the object to the adjacency list
		tree.put(trObj, dbObj);

		// Prepare the Hibernate utilities for gathering properties
		SessionImpl session = SessionImpl.class.cast(sessionFactory.getCurrentSession());

		// Retrieve the identifier of the persistent object
		Serializable identifier = getIdentifier(metadata, null, dbObj, session);

		// If the identifier cannot be retrieved via getter, try to access it directly.
		PropertyAccessor trPropertyAccessor = null, dbPropertyAccessor = null;
		if (identifier == null) {
			dbPropertyAccessor = DormancyUtils.forBeanPropertyAccess(dbObj);
			identifier = getIdentifierValue(metadata, dbPropertyAccessor, dbObj, session);
			// If the identifier is still null, an exception is thrown indicating that the entity is not persistent
			trPropertyAccessor = DormancyUtils.forBeanPropertyAccess(trObj);
		}

		// Compare the version property (if present and enabled)
		String[] propertyNames = metadata.getPropertyNames();
		if (config.getVersionChecking() && metadata.isVersioned()) {
			Object dbValue = getPropertyValue(metadata, dbPropertyAccessor, dbObj, propertyNames[metadata.getVersionProperty()]);
			Object trValue = getPropertyValue(metadata, trPropertyAccessor, trObj, propertyNames[metadata.getVersionProperty()]);
			if (dbValue != null && !dbValue.equals(trValue)) {
				throw new StaleObjectStateException(metadata.getEntityName(), identifier);
			}
		}

		// Process the properties
		for (int i = 0; i < propertyNames.length; i++) {
			// Do not apply the version property if version checking is enabled
			if (metadata.getVersionProperty() == i && config.getVersionChecking()) {
				continue;
			}

			String propertyName = propertyNames[i];
			Object trValue = getPropertyValue(metadata, trPropertyAccessor, trObj, propertyName);
			Object dbValue = getPropertyValue(metadata, dbPropertyAccessor, dbObj, propertyName);
			Type type = metadata.getPropertyType(propertyName);

			// Lazily loaded collections are not copied
			if (type.isCollectionType() && dbValue instanceof AbstractPersistentCollection) {
				AbstractPersistentCollection persistentCollection = AbstractPersistentCollection.class.cast(dbValue);
				if (!persistentCollection.wasInitialized()) {
					// If property is loaded lazily, the value of the given object must be null or empty
					if (trValue != null && CollectionUtils.size(trValue) > 0) {
						throw new PropertyValueException("Property is loaded lazily. Therefore it must be null but was: " + trValue, metadata.getEntityName(), propertyName);
					}
					continue;
				}
				trValue = merge_(trValue, dbValue, tree);
			}

			// Lazily loaded properties are not copied
			if (type.isAssociationType()) {
				// If the persistent value is a Hibernate proxy, it might be loaded lazily
				if (dbValue instanceof HibernateProxy) {
					HibernateProxy hibernateProxy = HibernateProxy.class.cast(dbValue);
					LazyInitializer lazyInitializer = hibernateProxy.getHibernateLazyInitializer();
					if (lazyInitializer.isUninitialized()) {
						// If property is loaded lazily, the value of the given object must be null
						if (trValue != null) {
							throw new PropertyValueException("Property is loaded lazily. Therefore it must be null but was: " + trValue, metadata.getEntityName(), propertyName);
						}
						continue;
					}
				} else if (trValue == dbValue) {
					continue;
				} else if (!config.getSaveAssociationsProperties()) {
					// Get the identifier of the associated transient object
					ClassMetadata valueMetadata = getClassMetadata(dbValue, sessionFactory);
					PropertyAccessor trValuePropertyAccessor = DormancyUtils.forBeanPropertyAccess(trValue);
					Serializable trValueId = Serializable.class.cast(getIdentifier(valueMetadata, trValuePropertyAccessor, trValue, session));
					// Get the identifier of the associated persistent object
					PropertyAccessor dbValuePropertyAccessor = DormancyUtils.forBeanPropertyAccess(dbValue);
					Serializable dbValueId = Serializable.class.cast(getIdentifier(valueMetadata, dbValuePropertyAccessor, dbValue, session));

					// If the transient object is new
					if (trValueId == null) {
						if (config.getSaveNewEntities()) {
							// If desired, try to persist the object
							trValueId = session.save(trValue);
							trValue = session.get(trValue.getClass(), trValueId);
						} else {
							// Otherwise throw an exception indicating that session.save() should be called
							throw new TransientObjectException("object references an unsaved transient instance - save the transient instance before flushing: " + DormancyUtils.getClass(trValue).getName());
						}
					} else if (!trValueId.equals(dbValueId)) {
						// Load the entity with the given identifier
						trValue = session.load(dbValue.getClass(), trValueId);
					} else {
						// Use the persistent value because the object identities are equals
						trValue = dbValue;
					}
				}
			}

			setPropertyValue(metadata, dbPropertyAccessor, dbObj, propertyName, trValue);
		}

		return dbObj;
	}

	/**
	 * Sets the Hibernate SessionFactory that should be used to create Hibernate Sessions.
	 *
	 * @param sessionFactory the SessionFactory to use
	 */
	@Inject
	@Named("sessionFactory")
	public void setSessionFactory(@Nonnull SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Returns the EntityPersisterConfiguration that should be used.
	 *
	 * @return the EntityPersisterConfiguration to use
	 */
	@Nonnull
	public EntityPersisterConfiguration getConfig() {
		return config;
	}

	/**
	 * Sets the EntityPersisterConfiguration that should be used.
	 *
	 * @param config the EntityPersisterConfiguration to use
	 */
	public void setConfig(@Nonnull EntityPersisterConfiguration config) {
		this.config = config;
	}

	/**
	 * Returns a modifiable map containing all registered EntityPersisters.
	 *
	 * @return the registered EntityPersisters
	 */
	@Nonnull
	public Map<Class, AbstractEntityPersister> getPersisterMap() {
		return persisterMap;
	}

	/**
	 * Returns an {@link at.schauer.gregor.dormancy.persister.EntityPersister} that is capable of processing instances
	 * of the given type.
	 *
	 * @param clazz the type of the object to process
	 * @return the EntityPersister or {@code null} if there is none available.
	 */
	@Nullable
	public AbstractEntityPersister getEntityPersister(@Nullable Class clazz) {
		AbstractEntityPersister entityPersister = persisterMap.get(clazz);
		if (entityPersister == null && clazz != null) {
			for (Map.Entry<Class, AbstractEntityPersister> entry : persisterMap.entrySet()) {
				if (entry.getKey().isAssignableFrom(clazz)) {
					entityPersister = entry.getValue();
					persisterMap.put(clazz, entityPersister);
					break;
				}
			}
		}
		return entityPersister;
	}

	@Override
	public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
		initialize();
		// Retrieve all AbstractEntityPersisters from the application context and register them
		Map<String, AbstractEntityPersister> map = applicationContext.getBeansOfType(AbstractEntityPersister.class);
		for (AbstractEntityPersister entityPersister : map.values()) {
			addEntityPersister(entityPersister);
		}
	}

	/**
	 * Registers the given {@link AbstractEntityPersister} for certain types.<br/>
	 * The {@link AbstractEntityPersister} is registered for every type returned by
	 * {@link at.schauer.gregor.dormancy.persister.AbstractEntityPersister#getSupportedTypes()} and the parameter types.
	 * Furthermore, the type of the {@link AbstractEntityPersister} itself is registered so it can be used by in
	 * {@link at.schauer.gregor.dormancy.interceptor.PersistenceEndpoint#types()}.
	 *
	 * @param entityPersister the EntityPersister to register
	 * @param types           the types of objects supported by the EntityPersister (may be {@code null})
	 */
	public void addEntityPersister(@Nonnull AbstractEntityPersister entityPersister, @Nullable Class... types) {
		if (entityPersister.getSupportedTypes() != null) {
			for (Class type : entityPersister.getSupportedTypes()) {
				persisterMap.put(type, entityPersister);
			}
		}
		if (types != null) {
			// Register the given types for advanced customization
			for (Class type : types) {
				persisterMap.put(type, entityPersister);
			}
		}
		// Register the unproxified persister itself to make it available for PersistenceEndpoint
		persisterMap.put(AopUtils.getTargetClass(entityPersister), entityPersister);
	}

	/**
	 * Throws a {@link TransientObjectException} indicating that the object has no valid identifier.
	 *
	 * @param object  the object
	 * @param session the session used for accessing the object
	 */
	protected static void throwNullIdentifierException(Object object, SessionImpl session) {
		throw new TransientObjectException("The given object has a null identifier: " + session.getEntityName(object));
	}

	/**
	 * Throws {@link TransientObjectException} indicating that the object must be saved manually before continuing.
	 *
	 * @param object  the object
	 * @param session the session used for accessing the object
	 */
	protected static void throwUnsavedTransientInstanceException(Object object, SessionImpl session) {
		throw new TransientObjectException(
				"object references an unsaved transient instance - save the transient instance before flushing: " +
						session.guessEntityName(object)
		);
	}
}
