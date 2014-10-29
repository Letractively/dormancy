/*
 * Copyright 2014 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.aop;

import at.dormancy.Dormancy;
import at.dormancy.handler.ObjectHandler;
import at.dormancy.util.DormancyContext;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.base.Throwables.propagateIfPossible;

/**
 * Intercepts calls and performs cloning and merging of JPA entities.
 *
 * @author Gregor Schauer
 * @see Dormancy
 * @see ObjectHandler
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE) // make sure this is invoked after other aspects e.g., transaction or validation
public class DormancyAdvisor extends AbstractPointcutAdvisor implements MethodInterceptor {
	protected static final Logger logger = Logger.getLogger(DormancyAdvisor.class);

	public enum Mode {
		/**
		 * Processes parameters of intercepted methods only.
		 */
		PARAMETERS,
		/**
		 * Processes results of intercepted methods only.
		 */
		RESULT,
		/**
		 * Processes parameters as well as results of intercepted methods.
		 */
		BOTH
	}

	protected Class<? extends Annotation> annotationType = PersistenceEndpoint.class;
	protected Dormancy dormancy;
	protected Mode mode = Mode.RESULT;
	protected Pointcut pointcut;
	protected Integer order;

	@Inject
	public DormancyAdvisor(@Nonnull Dormancy dormancy) {
		this.dormancy = dormancy;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private Object process(@Nonnull Object[] args, @Nonnull Method method, @Nonnull Object target,
						   @Nonnull Callable<?> callable) throws Throwable {
		// Process method parameters (if enabled)
		ObjectHandler<Object> handler = dormancy.asObjectHandler();
		if (args.length > 0 && (mode == Mode.PARAMETERS || mode == Mode.BOTH)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Using %s for method invocation %s.%s(%s)",
						handler.getClass().getSimpleName(), target.getClass().getName(),
						method.getName(), Arrays.toString(args)));
			}
			for (int i = 0; i < args.length; i++) {
				args[i] = dormancy.asObjectHandler().apply(args[i], new DormancyContext());
			}
		}

		// Invoke the desired method
		Object result = callable.call();

		// Process the result (if enabled)
		if (method.getReturnType() != void.class && result != null && (mode == Mode.RESULT || mode == Mode.BOTH)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Using %s for method result %s.%s(%s) => %s",
						handler.getClass().getSimpleName(), target.getClass().getName(),
						method.getName(), Arrays.toString(args), result));
			}
			result = handler.disconnect(result, new DormancyContext());
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
				} catch (Throwable throwable) {
					propagateIfPossible(throwable, Exception.class);
					throw propagate(throwable);
				}
			}
		});
	}

	@Nullable
	@Override
	public Object invoke(@Nonnull final MethodInvocation invocation) throws Throwable {
		return process(invocation.getArguments(), invocation.getMethod(), invocation.getThis(),
				new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							return invocation.proceed();
						} catch (Throwable throwable) {
							propagateIfPossible(throwable, Exception.class);
							throw propagate(throwable);
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
						public boolean matches(@Nonnull Method method, @Nonnull Class targetClass) {
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
