package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.EntityPersisterConfiguration;
import org.hibernate.SessionFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Processes non Hibernate managed objects including wrappers and containers such as collections.
 *
 * @author Gregor Schauer
 */
public abstract class AbstractContainerPersister<C> extends AbstractEntityPersister<C> {
	protected SessionFactory sessionFactory;
	protected Dormancy dormancy;
	protected EntityPersisterConfiguration config;

	@Inject
	public AbstractContainerPersister(@Nonnull Dormancy dormancy) {
		this.dormancy = dormancy;
		this.config = new EntityPersisterConfiguration(dormancy.getConfig());
	}

	/**
	 * Sets the Hibernate SessionFactory that should be used to create Hibernate Sessions.
	 *
	 * @param sessionFactory the SessionFactory to use
	 */
	@Inject
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
	 * Creates an empty container of the given type.
	 *
	 * @param container the original container
	 * @return the new container
	 */
	@Nonnull
	protected abstract C createContainer(@Nonnull C container);
}
