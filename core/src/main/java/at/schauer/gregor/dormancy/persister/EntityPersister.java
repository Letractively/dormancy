package at.schauer.gregor.dormancy.persister;

import javax.annotation.Nullable;

/**
 * Contains operations for cloning Hibernate managed objects and merging them into a {@link org.hibernate.Session}.
 * <p/>
 * The term cloning includes any operations required for creating an object graph which is a copy of the given object
 * and its associations. It is neither guaranteed that the types of the constructed objects is the same as the types of
 * the given objects nor its data is exactly the same. In other words, the object graph may be altered (if necessary)
 * but the semantics of the properties must not change.<br/>
 * Merging means to create and/or load Hibernate managed entities which are pendents of the given objects and applying
 * any semantic value changes to them.
 * <p/>
 * It is recommended that merging a previously cloned entity into a {@link org.hibernate.Session} returns the entity
 * without modifications e.g., {@code entityPersister.merge(entityPersister.clone(anObject)).equals(anObject)} results
 * to {@code true}.
 * <p/>
 * Implementors must be threadsafe (preferrably immutable).
 *
 * @author Gregor Schauer
 */
public interface EntityPersister<C> {
	/**
	 * Clones the given object.
	 *
	 * @param dbObj the object to clone
	 * @return the cloned object
	 */
	@Nullable
	<T extends C> C clone(@Nullable T dbObj);

	/**
	 * Merges the given object into the current {@link org.hibernate.Session}.
	 *
	 * @param trObj the object to merge
	 * @return the merged object
	 */
	@Nullable
	<T extends C> C merge(@Nullable T trObj);

	/**
	 * Merges the given transient object with the persistent object.
	 *
	 * @param trObj the transient object
	 * @param dbObj the persistent object
	 * @return the persistent object with applied property changes
	 */
	@Nullable
	<T extends C> C merge(@Nullable T trObj, @Nullable T dbObj);
}
