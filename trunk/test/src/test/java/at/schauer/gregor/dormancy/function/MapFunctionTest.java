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

import at.schauer.gregor.dormancy.entity.Employee;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 * @since 1.0.1
 */
public class MapFunctionTest {
	@Test
	public void test() throws Exception {
		DelegateFunction<Long, Long> keyDelegate = new DelegateFunction<Long, Long>() {
			@Override
			public FunctionContext<Long> apply(@Nullable FunctionContext<Long> input) {
				return input;
			}
		};
		DelegateFunction<Employee, Employee> valueDelegate = new DelegateFunction<Employee, Employee>() {
			@Nonnull
			@Override
			public FunctionContext<Employee> apply(@Nonnull FunctionContext<Employee> input) {
				input.getObj().setId(input.getObj().getId() != null ? input.getObj().getId() + 1 : 1);
				return input;
			}
		};
		MapFunction<Long, Employee, Map<Long, Employee>> function = new MapFunction<Long, Employee, Map<Long, Employee>>();
		function.keyDelegate = keyDelegate;
		function.valueDelegate = valueDelegate;

		Employee employee = new Employee();
		Employee clone = (Employee) BeanUtils.cloneBean(employee);
		employee.setId(1L);

		Map<Long, Employee> map = Collections.singletonMap(1L, clone);
		assertEquals(Collections.singletonMap(1L, employee), function.apply(new FunctionContext<Map<Long, Employee>>(map)).getObj());
	}

	@Test
	public void testWithoutDelegate() throws Exception {
		MapFunction<Long, Employee, Map<Long, Employee>> function = new MapFunction<Long, Employee, Map<Long, Employee>>();

		Employee employee = new Employee();
		Employee clone = (Employee) BeanUtils.cloneBean(employee);
		employee.setId(1L);

		Map<Long, Employee> map = Collections.singletonMap(1L, clone);
		Map<Long, Employee> expected = Collections.singletonMap(1L, employee);
		Map<Long, Employee> actual = function.apply(new FunctionContext<Map<Long, Employee>>(map)).getObj();
		assertEquals(false, expected.equals(actual));
	}
}
