/*
 * Copyright 2013 Gregor Schauer
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
package at.schauer.gregor.dormancy.interceptor;

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.persister.EntityPersister;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Intercepts calls and performs cloning and merging of Hibernate entities.
 * <p/>
 * At first, an {@link at.schauer.gregor.dormancy.persister.EntityPersister} is resolved in the following sequence:
 * <ol>
 * <li>Use the metadata of the {@link PersistenceEndpoint} annotation on the method to invoke</li>
 * <li>Use the metadata of the {@link PersistenceEndpoint} annotation on the object instance</li>
 * </ol>
 * If any {@link PersistenceEndpoint} annotation is present, get an {@link at.schauer.gregor.dormancy.persister.EntityPersister} from the {@link org.springframework.beans.factory.BeanFactory}
 * with the given {@code name}. Otherwise, get an {@link at.schauer.gregor.dormancy.persister.EntityPersister} of the specified {@code type} from
 * {@link at.schauer.gregor.dormancy.Dormancy}. If no appropriate {@link at.schauer.gregor.dormancy.persister.EntityPersister} is registered, {@link at.schauer.gregor.dormancy.Dormancy} will be used.
 *
 * @author Gregor Schauer
 * @see at.schauer.gregor.dormancy.Dormancy
 * @see at.schauer.gregor.dormancy.persister.EntityPersister
 * @see PersistenceEndpoint
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE) // make sure this is invoked after other aspects e.g., transaction or validation
public class DormancyAdvisor extends AbstractPointcutAdvisor implements MethodInterceptor, BeanFactoryAware {
	protected static final Logger logger = Logger.getLogger(DormancyAdvisor.class);
	protected Class<? extends Annotation> annotationType = PersistenceEndpoint.class;
	protected Pointcut pointcut;
	protected Integer order;

	public enum Mode {
		PARAMETERS, RESULT, BOTH
	}

	protected Dormancy dormancy;
	protected Mode mode = Mode.RESULT;
	protected BeanFactory beanFactory;

	@Inject
	public DormancyAdvisor(@Nonnull Dormancy dormancy) {
		this.dormancy = dormancy;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private Object process(@Nonnull Object[] args, @Nonnull Method method, @Nonnull Object target, @Nonnull Callable<?> callable) throws Throwable {
		// If the method to invoke takes no parameters and does not return anything, directly invoke it
		if (args.length == 0 && method.getReturnType() == void.class) {
			return callable.call();
		}

		EntityPersister entityPersister = dormancy;

		// Retrieve the persistence endpoint annotation from the method (if present)
		Annotation annotation = method.getAnnotation(annotationType);
		if (annotation == null) {
			// Retrieve the annotation from the object instance (if present)
			annotation = target.getClass().getAnnotation(annotationType);
		}

		// If a persistence endpoint annotation was found
		if (annotation != null && annotation.annotationType() == PersistenceEndpoint.class) {
			PersistenceEndpoint persistenceEndpoint = PersistenceEndpoint.class.cast(annotation);
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

		// Process method parameters (if enabled)
		if (mode == Mode.PARAMETERS || mode == Mode.BOTH) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Using %s for method invocation %s.%s(%s)",
						entityPersister.getClass().getSimpleName(), target.getClass().getName(),
						method.getName(), Arrays.toString(args)));
			}
			for (int i = 0; i < args.length; i++) {
				args[i] = entityPersister.merge(args[i]);
			}
		}

		// Invoke the desired method
		Object result = callable.call();

		// Process the result (if enabled)
		if (mode == Mode.RESULT || mode == Mode.BOTH) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Using %s for method result %s.%s(%s) => %s",
						entityPersister.getClass().getSimpleName(), target.getClass().getName(),
						method.getName(), Arrays.toString(args), result));
			}
			result = result != null ? entityPersister.clone(result) : result;
		}
		return result;
	}

	@Nullable
	public Object around(@Nonnull final ProceedingJoinPoint joinPoint) throws Throwable {
		Method method = MethodSignature.class.cast(joinPoint.getSignature()).getMethod();
		return process(joinPoint.getArgs(), method, joinPoint.getTarget(), new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				try {
					return joinPoint.proceed();
				} catch (Exception e) {
					throw e;
				} catch (Throwable throwable) {
					throw new RuntimeException(throwable);
				}
			}
		});
	}

	@Nullable
	@Override
	public Object invoke(@Nonnull final MethodInvocation invocation) throws Throwable {
		return process(invocation.getArguments(), invocation.getMethod(), invocation.getThis(), new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				try {
					return invocation.proceed();
				} catch (Exception e) {
					throw e;
				} catch (Throwable throwable) {
					throw new RuntimeException(throwable);
				}
			}
		});
	}

	@Nonnull
	@Override
	public Pointcut getPointcut() {
		if (pointcut == null) {
			pointcut = new AnnotationMatchingPointcut(null, annotationType) {
				@Nonnull
				@Override
				public MethodMatcher getMethodMatcher() {
					return new AnnotationMethodMatcher(annotationType) {
						@Override
						public boolean matches(Method method, Class targetClass) {
							return super.matches(method, targetClass)
									|| AopUtils.getTargetClass(targetClass).isAnnotationPresent(annotationType);
						}
					};
				}
			};
		}
		return pointcut;
	}

	@Nonnull
	@Override
	public Advice getAdvice() {
		return this;
	}

	@Override
	public int getOrder() {
		if (order != null) {
			return order;
		}
		Advice advice = getAdvice();
		if (advice instanceof Ordered && advice != this) {
			return ((Ordered) advice).getOrder();
		}
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public void setBeanFactory(@Nonnull BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Sets the Mode that should be used to process method parameters and return values, respectively.
	 *
	 * @param mode the mode to operate
	 */
	public void setMode(@Nonnull Mode mode) {
		this.mode = mode;
	}

	/**
	 * Sets the annotation type that indicates a persistence endpoint.
	 *
	 * @param annotationType the type of the annotation
	 */
	public void setAnnotationType(@Nonnull Class<? extends Annotation> annotationType) {
		this.annotationType = annotationType;
	}
}
