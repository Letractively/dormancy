package at.schauer.gregor.dormancy.interceptor;

import at.schauer.gregor.dormancy.persister.EntityPersister;

import java.lang.annotation.*;

/**
 * Provides metadata for the {@link DormancyAdvisor}.
 *
 * @author Gregor Schauer
 * @see DormancyAdvisor
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PersistenceEndpoint {
	/**
	 * The name of the {@link EntityPersister} to use.
	 *
	 * @return the entity persister name
	 */
	String name() default "";

	/**
	 * The type of the {@link EntityPersister} to use.
	 *
	 * @return the entity persister type
	 */
	Class<? extends EntityPersister>[] types() default {};
}
