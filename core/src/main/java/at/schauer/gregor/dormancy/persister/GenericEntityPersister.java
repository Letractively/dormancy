package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.Dormancy;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Determines dynamically the properties to process.
 *
 * @author Gregor Schauer
 * @since 1.0.1
 */
public abstract class GenericEntityPersister<C> extends AbstractEntityPersister<C> {
	protected boolean reuseObject;
	protected Dormancy dormancy;

	@Inject
	public GenericEntityPersister(@Nonnull Dormancy dormancy) {
		this.dormancy = dormancy;
	}

	/**
	 * Creates a new instance of the given object type.<br/>
	 * If {@link #reuseObject} is {@code true}, the given object is returned directly. Otherwise, a new instances is
	 * created if possible.
	 * Note that the class must have a no-arg constructor, which can be declared {@code private} because this method
	 * tries to set it accessible.
	 *
	 * @param trObj the object
	 * @param <T>   the type of the object
	 * @return the instance
	 * @see org.springframework.beans.BeanUtils#instantiateClass(Class)
	 */
	@SuppressWarnings("unchecked")
	protected <T extends C> T createObject(T trObj) {
		return reuseObject ? trObj : BeanUtils.instantiateClass((Class<T>) trObj.getClass());
	}

	/**
	 * Sets whether the results should be written to the given objects directly or if a new instance has to be created.
	 *
	 * @param reuseObject {@code true} if the given objects should be reused, {@code false} otherwise
	 */
	public void setReuseObject(boolean reuseObject) {
		this.reuseObject = reuseObject;
	}
}
