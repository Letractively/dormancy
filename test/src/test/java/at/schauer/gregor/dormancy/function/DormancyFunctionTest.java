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
package at.schauer.gregor.dormancy.function;

import at.schauer.gregor.dormancy.AbstractDormancyTest;
import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.DormancySpringConfig;
import at.schauer.gregor.dormancy.entity.Employee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 * @since 1.0.1
 */
@ContextConfiguration(classes = DormancySpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class DormancyFunctionTest {
	DormancyCloneFunction<Employee> cloneFunction = new DormancyCloneFunction<Employee>();
	DormancyMergeFunction<Employee> mergeFunction = new DormancyMergeFunction<Employee>();
	@Inject
	Dormancy dormancy;
	@Inject
	SessionFactory sessionFactory;

	@Before
	public void before() {
		cloneFunction.dormancy = dormancy;
		mergeFunction.dormancy = dormancy;
	}

	@Test
	public void test() {
		Session session = sessionFactory.getCurrentSession();
		session.save(new Employee("A", null));
		session.flush();

		Employee employee = (Employee) session.get(Employee.class, 1L);
		FunctionContext<Employee> context = cloneFunction.apply(new FunctionContext<Employee>(employee));
		assertEquals(false, context.getTree().isEmpty());
		assertEquals(true, AbstractDormancyTest.isManaged(employee, session));
		assertEquals(false, AbstractDormancyTest.isProxy(context.getObj(), session));

		context = mergeFunction.apply(new FunctionContext<Employee>(context.getObj()));
		assertEquals(false, context.getTree().isEmpty());
		assertEquals(true, AbstractDormancyTest.isManaged(employee, session));
		assertEquals(true, AbstractDormancyTest.isManaged(context.getObj(), session));
	}
}
