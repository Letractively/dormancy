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
import at.dormancy.DormancySpringConfig;
import at.dormancy.entity.Book;
import at.dormancy.handler.ObjectHandler;
import at.dormancy.service.Service;
import at.dormancy.util.ClassLookup;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.Pointcut;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gregor Schauer
 */
@ContextConfiguration(classes = DormancyAdvisorTest.Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DormancyAdvisorTest {
	@Inject
	Service service;
	@Inject
	DormancyAdvisor dormancyAdvisor;

	@Test
	@Transactional
	public void test() {
		Book refBook = new Book(UUID.randomUUID().toString());
		dormancyAdvisor.dormancy.getUtils().persist(refBook);
		assertNotNull(service.get(Book.class, refBook.getId()));
	}

	@Test(expected = UnsupportedOperationException.class)
	@Transactional
	public void testException() {
		service.throwException();
	}

	@Test
	public void testPointcut() {
		Pointcut pointcut = dormancyAdvisor.getPointcut();
		Class<Object> serviceImpl = ClassLookup.find(
				"at.dormancy.service.HibernateServiceImpl",
				"at.dormancy.service.JpaServiceImpl").get();

		assertEquals(true, pointcut.getClassFilter().matches(dormancyAdvisor.getClass()));
		assertEquals(true, pointcut.getClassFilter().matches(this.getClass()));
		assertEquals(true, pointcut.getClassFilter().matches(serviceImpl));
		assertEquals(true, pointcut.getClassFilter().matches(Service.class));
		assertEquals(true, pointcut.getClassFilter().matches(Object.class));

		Method method = MethodUtils.getAccessibleMethod(serviceImpl, "doNothing", ArrayUtils.EMPTY_CLASS_ARRAY);
		assertEquals(false, pointcut.getMethodMatcher().matches(method, serviceImpl));
	}

	@Test
	public void testOrder() {
		assertEquals(Ordered.LOWEST_PRECEDENCE, dormancyAdvisor.getOrder());
		dormancyAdvisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
		assertEquals(Ordered.HIGHEST_PRECEDENCE, dormancyAdvisor.getOrder());
	}

	@Configuration
	@Import(DormancySpringConfig.class)
	@EnableTransactionManagement(proxyTargetClass = true)
	@ComponentScan(value = "at.dormancy.handler",
			includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ObjectHandler.class))
	static class Config {
		@Inject
		ApplicationContext ctx;

		@Bean
		public DormancyAdvisorAspect dormancyInterceptor() {
			return new DormancyAdvisorAspect();
		}

		@Bean
		public DormancyAdvisor dormancyAdvisor() {
			DormancyAdvisor advisor = new DormancyAdvisor(ctx.getBean(Dormancy.class));
			advisor.setAnnotationType(Transactional.class);
			return advisor;
		}

		@Aspect
		static class DormancyAdvisorAspect {
			@Inject
			DormancyAdvisor delegate;

			@Around("execution(* at.dormancy.service.*ServiceImpl.*(..)) "
					+ "&& @target(org.springframework.transaction.annotation.Transactional)")
			public Object around(@Nonnull ProceedingJoinPoint joinPoint) throws Throwable {
				return delegate.around(joinPoint);
			}
		}
	}
}
