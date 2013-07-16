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
package at.schauer.gregor.dormancy.function;

import at.schauer.gregor.dormancy.entity.Employee;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 * @since 1.0.1
 */
public class CollectionFunctionTest {
	@Test
	public void test() throws Exception {
		DelegateFunction<Employee, Employee> delegateFunction = new DelegateFunction<Employee, Employee>() {
			@Nonnull
			@Override
			public FunctionContext<Employee> apply(@Nullable FunctionContext<Employee> input) {
				input.getObj().setVersion(input.getObj().getVersion() != null ? input.getObj().getVersion() + 1 : 1);
				return input;
			}
		};
		CollectionFunction<List<Employee>, Employee> function = new CollectionFunction<List<Employee>, Employee>();
		function.delegate = delegateFunction;

		Employee employee = new Employee();
		Employee clone = (Employee) BeanUtils.cloneBean(employee);
		employee.setVersion(1L);

		List<Employee> list = Collections.singletonList(clone);
		assertEquals(Collections.singletonList(clone), function.apply(new FunctionContext<List<Employee>>(list)).getObj());
	}

	@Test
	public void testEmpty() {
		CollectionFunction<List<Number>, Number> function = new CollectionFunction<List<Number>, Number>();
		assertNull(function.apply(null));

		FunctionContext<List<Number>> context = function.apply(new FunctionContext<List<Number>>());
		assertNotNull(context);
		assertNotNull(context.getTree());
		assertNull(context.getObj());
	}
}
