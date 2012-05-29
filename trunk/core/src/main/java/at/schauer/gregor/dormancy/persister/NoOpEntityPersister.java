package at.schauer.gregor.dormancy.persister;

import javax.annotation.Nullable;

/**
 * Simply returns the given entities.
 * <p/>
 * This implementation can be used for disabling {@link EntityPersister}s for a certain method.
 * <p/>
 * Example:
 * <pre class="code">
 * &#64;PersistenceEndpoint
 * public class OrderService {
 * public OrderStatus order(Order arg) {
 * // process order...
 * return orderStatus;
 * }
 * <p/>
 * &#64;PersistenceEndpoint(type = NoOpEntityPersister.class)
 * public String addCoupon(String code) {
 * // process coupon code
 * return message;
 * }
 * }
 * </pre>
 *
 * @author Gregor Schauer
 */
public class NoOpEntityPersister<C> implements EntityPersister<C> {
	/**
	 * Returns the given object.
	 *
	 * @param dbObj the object to clone
	 * @return the given object
	 */
	@Override
	public <T extends C> T clone(@Nullable T dbObj) {
		return dbObj;
	}

	/**
	 * Returns the given object.
	 *
	 * @param trObj the object to merge
	 * @return the given object
	 */
	@Override
	public <T extends C> T merge(@Nullable T trObj) {
		return trObj;
	}

	/**
	 * Returns the given transient object.
	 *
	 * @param trObj the transient object
	 * @param dbObj the persistent object
	 * @return the transient object
	 */
	@Override
	public <T extends C> T merge(@Nullable T trObj, @Nullable T dbObj) {
		return trObj;
	}
}
