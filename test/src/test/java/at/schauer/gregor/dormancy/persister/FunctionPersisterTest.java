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
package at.schauer.gregor.dormancy.persister;

import at.schauer.gregor.dormancy.entity.Employee;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gregor Schauer
 */
public class FunctionPersisterTest extends PersisterTest<FunctionPersister<Employee>> {
	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new FunctionPersister<Employee>(dormancy);
		persister.setDormancy(dormancy);
	}

	@Test
	public void testClone() {
		Employee b = (Employee) sessionFactory.getCurrentSession().get(Employee.class, 2L);
		Iterable<?> e = persister.clone(b.getEmployees());
		Assert.assertEquals(1, Iterables.size(e));
	}

	@Test
	public void testMerge() {
		Employee bp = (Employee) sessionFactory.getCurrentSession().get(Employee.class, 2L);
		assertEquals(1, bp.getEmployees().size());
		Employee bt = dormancy.clone(bp);
		assertNotNull(bt);

		Employee be = Iterables.getFirst(bt.getEmployees(), null);
		assertNotNull(be);
		be.setName(UUID.randomUUID().toString());

		Iterable<Employee> e = persister.merge(bt.getEmployees());
		Employee ee = Iterables.getFirst(e, null);
		assertNotNull(ee);

		assertEquals(be.getName(), ee.getName());
	}

	@Test
	public void testMergeTogether() {
		Employee bp = (Employee) sessionFactory.getCurrentSession().get(Employee.class, 2L);
		assertEquals(1, bp.getEmployees().size());
		Employee bt = dormancy.clone(bp);
		assertNotNull(bt);

		Employee be = Iterables.getFirst(bt.getEmployees(), null);
		assertNotNull(be);
		be.setName(UUID.randomUUID().toString());

		Iterable<Employee> e = persister.merge(bt.getEmployees(), bp.getEmployees());
		Employee ee = Iterables.getFirst(e, null);
		assertNotNull(ee);

		assertEquals(be.getName(), ee.getName());
	}
}
