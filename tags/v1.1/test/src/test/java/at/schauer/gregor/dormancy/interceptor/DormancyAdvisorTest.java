/*
 * Copyright 2012 Gregor Schauer
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

import at.schauer.gregor.dormancy.DormancySpringConfig;
import at.schauer.gregor.dormancy.entity.Book;
import at.schauer.gregor.dormancy.persister.EntityPersister;
import at.schauer.gregor.dormancy.service.Service;
import at.schauer.gregor.dormancy.service.ServiceImpl;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.Pointcut;
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
@Configuration
@ContextConfiguration(classes = DormancyAdvisorTest.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(value = "at.schauer.gregor.dormancy.persister",
		includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = EntityPersister.class))
@RunWith(SpringJUnit4ClassRunner.class)
public class DormancyAdvisorTest extends DormancySpringConfig {
	@Inject
	Service service;
	@Inject
	SessionFactory sessionFactory;
	@Inject
	DormancyAdvisor dormancyAdvisor;

	@Test
	@Transactional
	public void test() {
		sessionFactory.getCurrentSession().save(new Book(UUID.randomUUID().toString()));
		assertNotNull(service.load(Book.class, 1L));
	}

	@Test
	public void testPointcut() {
		Pointcut pointcut = dormancyAdvisor.getPointcut();

		assertEquals(true, pointcut.getClassFilter().matches(dormancyAdvisor.getClass()));
		assertEquals(true, pointcut.getClassFilter().matches(this.getClass()));
		assertEquals(true, pointcut.getClassFilter().matches(ServiceImpl.class));
		assertEquals(true, pointcut.getClassFilter().matches(Service.class));
		assertEquals(true, pointcut.getClassFilter().matches(Object.class));

		Method method = MethodUtils.getAccessibleMethod(ServiceImpl.class, "doNothing", ArrayUtils.EMPTY_CLASS_ARRAY);
		assertEquals(false, pointcut.getMethodMatcher().matches(method, ServiceImpl.class));
	}

	@Test
	public void testOrder() {
		assertEquals(Ordered.LOWEST_PRECEDENCE, dormancyAdvisor.getOrder());
		dormancyAdvisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
		assertEquals(Ordered.HIGHEST_PRECEDENCE, dormancyAdvisor.getOrder());
	}

	@Bean
	public DormancyAdvisorAspect dormancyInterceptor() {
		return new DormancyAdvisorAspect(dormancyAdvisor());
	}

	@Bean
	public DormancyAdvisor dormancyAdvisor() {
		DormancyAdvisor advisor = new DormancyAdvisor(dormancy());
		advisor.setAnnotationType(Transactional.class);
		return advisor;
	}

	@Bean
	public Service service() {
		return new ServiceImpl();
	}

	@Aspect
	static class DormancyAdvisorAspect {
		DormancyAdvisor delegate;

		public DormancyAdvisorAspect(DormancyAdvisor delegate) {
			this.delegate = delegate;
		}

		@Around("target(at.schauer.gregor.dormancy.service.ServiceImpl) && @target(org.springframework.transaction.annotation.Transactional)")
		public Object around(@Nonnull ProceedingJoinPoint joinPoint) throws Throwable {
			return delegate.around(joinPoint);
		}
	}
}
