package at.schauer.gregor.dormancy.interceptor;

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.persister.EntityPersister;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Arrays;

/**
 * Intercepts calls and performs cloning and merging of Hibernate entities.
 * <p/>
 * At first, an {@link EntityPersister} is resolved in the following sequence:
 * <ol>
 * <li>Use the metadata of the {@link PersistenceEndpoint} annotation on the method to invoke</li>
 * <li>Use the metadata of the {@link PersistenceEndpoint} annotation on the object instance</li>
 * </ol>
 * If any {@link PersistenceEndpoint} annotation is present, get an {@link EntityPersister} from the {@link BeanFactory}
 * with the given {@code name}. Otherwise, get an {@link EntityPersister} of the specified {@code type} from
 * {@link Dormancy}. If no appropriate {@link EntityPersister} is registered, {@link Dormancy} will be used.
 *
 * @author Gregor Schauer
 * @see Dormancy
 * @see EntityPersister
 * @see PersistenceEndpoint
 */
public class DormancyInterceptor implements MethodInterceptor, BeanFactoryAware {
	private static final Logger logger = Logger.getLogger(DormancyInterceptor.class);
	@Nonnull
	protected Dormancy dormancy;
	protected BeanFactory beanFactory;

	@Inject
	public DormancyInterceptor(@Nonnull Dormancy dormancy) {
		this.dormancy = dormancy;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
		// If the method to invoke takes no parameters and does not return anything, directly invoke it
		if (invocation.getArguments().length == 0 && invocation.getMethod().getReturnType() == void.class) {
			return invocation.proceed();
		}

		EntityPersister entityPersister = dormancy;

		// Retrieve the PersistenceEndpoint annotation from the method (if present)
		PersistenceEndpoint persistenceEndpoint = invocation.getMethod().getAnnotation(PersistenceEndpoint.class);
		if (persistenceEndpoint == null) {
			// Retrieve the PersistenceEndpoint from the object instance (if present)
			persistenceEndpoint = invocation.getThis().getClass().getAnnotation(PersistenceEndpoint.class);
		}

		// If a PersistenceEndpoint annotation was found
		if (persistenceEndpoint != null) {
			String name = persistenceEndpoint.name();
			if (!StringUtils.isEmpty(name)) {
				// If the name attribute is set, retrieve an EntityPersister from the BeanFactory
				Assert.notNull(beanFactory, "BeanFactory must not be null");
				entityPersister = beanFactory.getBean(name, EntityPersister.class);
			} else if (persistenceEndpoint.types().length > 0) {
				// If types are set, look for an registered EntityPersister
				Class<? extends EntityPersister>[] types = persistenceEndpoint.types();
				for (Class<? extends EntityPersister> clazz : types) {
					entityPersister = dormancy.getEntityPersister(clazz);
					if (entityPersister != null) {
						break;
					}
				}
				if (entityPersister == null) {
					throw new IllegalArgumentException("No EntityPersister registered for any of the types: " + Arrays.toString(types));
				}
			}
		}

		// Process method parameters
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Using %s for method invocation %s.%s(%s)",
					entityPersister.getClass().getSimpleName(), invocation.getThis().getClass().getName(),
					invocation.getMethod().getName(), Arrays.toString(invocation.getArguments())));
		}
		Object[] arguments = invocation.getArguments();
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = entityPersister.merge(arguments[i]);
		}

		// Invoke the desired method
		Object result = invocation.proceed();

		// Process the result
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Using %s for method result %s.%s(%s) => %s",
					entityPersister.getClass().getSimpleName(), invocation.getThis().getClass().getName(),
					invocation.getMethod().getName(), Arrays.toString(invocation.getArguments()), result));
		}
		return result != null ? entityPersister.clone(result) : result;
	}

	@Override
	public void setBeanFactory(@Nonnull BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
