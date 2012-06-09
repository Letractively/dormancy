package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.core.CollectionFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/**
 * Processes all types of {@link Collection}s by traversing them and invoking the desired operation of the appropriate
 * {@link EntityPersister} for all elements.
 *
 * @author Gregor Schauer
 */
public class CollectionPersister<C extends Collection> extends AbstractContainerPersister<C> {
	@Inject
	public CollectionPersister(@Nonnull Dormancy dormancy) {
		super(dormancy);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C clone_(@Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (dbObj == null || tree.containsKey(dbObj)) {
			return (C) tree.get(dbObj);
		}
		C container = createContainer(dbObj);
		tree.put(dbObj, container);

		for (Object dbElement : dbObj) {
			container.add(dormancy.clone_(dbElement, tree));
		}
		return container;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		C container = createContainer(trObj);
		tree.put(trObj, container);

		for (Object trElement : trObj) {
			container.add(dormancy.merge_(trElement, tree));
		}
		return container;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends C> C merge_(@Nullable T trObj, @Nullable T dbObj, @Nonnull Map<Object, Object> tree) {
		if (trObj == null || dbObj == null || tree.containsKey(trObj)) {
			return (C) tree.get(trObj);
		}
		C container = createContainer(dbObj);
		tree.put(trObj, dbObj);

		// Create a modifiable copy of the persistent collection
		C dbCopy = createContainer(dbObj);
		dbCopy.addAll(dbObj);

		Session session = sessionFactory.getCurrentSession();

		for (Object trElem : trObj) {
			// For every transient element, find a persistent element
			Object dbElem = dormancy.getUtils().findPendant(trElem, dbCopy, session);

			if (dbElem == null) {
				container.add(dormancy.merge_(trElem, tree));
			} else {
				container.add(dormancy.merge_(trElem, dbElem, tree));
			}
		}

		if (getConfig().getDeleteRemovedEntities()) {
			ClassMetadata metadata = null;
			// For every element that is left in the collection, check if it is a Hibernate managed entity and delete it
			for (Object deleted : dbCopy) {
				metadata = metadata != null && dormancy.getUtils().getMappedClass(metadata) == deleted.getClass()
						? metadata : dormancy.getUtils().getClassMetadata(deleted, sessionFactory);
				if (metadata != null) {
					sessionFactory.getCurrentSession().delete(deleted);
				}
			}
		}

		// Add the processed entities to the persistent collection
		dbObj.clear();
		dbObj.addAll(container);
		return dbObj;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	protected C createContainer(@Nonnull C container) {
		return (C) CollectionFactory.createApproximateCollection(container, container.size());
	}

	@Override
	public Class<?>[] getSupportedTypes() {
		return new Class[]{Collection.class};
	}
}
