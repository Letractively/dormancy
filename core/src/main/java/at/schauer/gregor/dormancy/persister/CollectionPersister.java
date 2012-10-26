/*
 * Copyright 2012 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import org.hibernate.Session;
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
