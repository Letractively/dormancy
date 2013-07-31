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
package at.dormancy.persister.function;

import at.dormancy.entity.Employee;
import at.dormancy.persister.function.ConstantValueFunction;
import at.dormancy.persister.function.FunctionContext;
import at.dormancy.persister.function.NullFunction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 * @since 1.0.1
 */
public class NullFunctionTest {
	@Test
	public void test() {
		ConstantValueFunction<Object> function = new NullFunction<Object>();
		doApply(function);
	}

	@Test
	public void testConstantValue() {
		ConstantValueFunction<Object> function = new ConstantValueFunction<Object>();
		doApply(function);
	}

	public void doApply(ConstantValueFunction<Object> function) {
		assertEquals(null, function.apply(new FunctionContext<Object>()).getObj());
		assertEquals(null, function.apply(new FunctionContext<Object>("")).getObj());
		assertEquals(null, function.apply(new FunctionContext<Object>(new Employee())).getObj());
	}
}