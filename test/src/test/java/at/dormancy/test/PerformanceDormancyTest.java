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
package at.dormancy.test;

import at.dormancy.AbstractDormancyTest;
import at.dormancy.Dormancy;
import at.dormancy.aop.DormancyAdvisor;
import at.dormancy.entity.Employee;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.*;

import static org.apache.commons.lang.ArrayUtils.EMPTY_CLASS_ARRAY;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gregor Schauer
 */
public class PerformanceDormancyTest extends AbstractDormancyTest {
	protected static final Logger logger = Logger.getLogger(PerformanceDormancyTest.class);
	protected Level level = Logger.getLogger(Dormancy.class).getLevel();

	@Before
	public void beforeClass() {
		Logger.getLogger(Dormancy.class).setLevel(Level.WARN);
		Logger.getLogger(DormancyAdvisor.class).setLevel(Level.WARN);
	}

	@After
	public void after() {
		Logger.getLogger(Dormancy.class).setLevel(level);
		Logger.getLogger(DormancyAdvisor.class).setLevel(level);
	}

	@Perform(name = "PersistenceContext.get(Object, Serializable)", n = 100)
	@Test(timeout = 1000)
	public void testGet() {
		Perform perform = getAnnotation();
		assertNotNull(perform);

		long start = System.currentTimeMillis();
		for (int i = 0; i < perform.n(); i++) {
			genericService.get(Employee.class, refB.getId());
			persistenceContextHolder.clear();
		}

		log(perform, start);
	}

	@Perform(name = "Dormancy.disconnect(Object)", n = 100)
	@Test(timeout = 1000)
	public void testDisconnect() {
		Perform perform = getAnnotation();
		Employee b = genericService.get(Employee.class, refB.getId());
		assertNotNull(b);

		long start = System.currentTimeMillis();
		for (int i = 0; i < perform.n(); i++) {
			dormancy.disconnect(b);
		}

		log(perform, start);
	}

	@Perform(name = "Dormancy.apply(Object)", n = 100)
	@Test(timeout = 5000)
	public void testApply() {
		Perform perform = getAnnotation();
		Employee b = service.get(Employee.class, refB.getId());
		assertNotNull(b);


		long start = System.currentTimeMillis();
		for (int i = 0; i < perform.n(); i++) {
			Employee merged = dormancy.apply(b);
			persistenceContextHolder.clear();
		}

		log(perform, start);
	}

	@Perform(name = "Dormancy.apply(Object, Object)", n = 100)
	@Test(timeout = 1000)
	public void testMergeTogether() {
		Perform perform = getAnnotation();
		Employee bp = genericService.get(Employee.class, refB.getId());
		Employee bt = service.get(Employee.class, refB.getId());
		assertNotNull(bp);

		long start = System.currentTimeMillis();
		for (int i = 0; i < perform.n(); i++) {
			Employee merged = dormancy.apply(bt, bp);
			persistenceContextHolder.clear();
		}

		log(perform, start);
	}

	private void log(Perform perform, long start) {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Executed %d times %s in %d ms",
					perform.n(), perform.name(), System.currentTimeMillis() - start));
		}
	}

	@Documented
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Perform {
		int n();

		String name();
	}

	protected Perform getAnnotation() {
		String methodName = getMethodName();
		return MethodUtils.getAccessibleMethod(getClass(), methodName, EMPTY_CLASS_ARRAY).getAnnotation(Perform.class);
	}

	protected static String getMethodName() {
		return Thread.currentThread().getStackTrace()[3].getMethodName();
	}
}
