package at.schauer.gregor.dormancy.util;

import at.schauer.gregor.dormancy.persistence.PersistenceUnitProvider;
import org.springframework.transaction.annotation.Transactional;

/**
 * Callback interface for JPA code.
 *
 * @author Gregor Schauer
 * @since 2.0.0
 */
public abstract class EntityCallback<T, PU, PC, PMD> {
	/**
	 * Gets called by {@link at.schauer.gregor.dormancy.Dormancy#merge(Object, EntityCallback)} with an active
	 * persistence context.<br/>
	 * It does not need to care about activating or closing it, or handling transactions.
	 *
	 * @param persistenceUnitProvider holds the persistence unit and the current context
	 * @return the result of the invocation
	 */
	@Transactional
	public abstract T work(PersistenceUnitProvider<PU, PC, PMD> persistenceUnitProvider);
}
