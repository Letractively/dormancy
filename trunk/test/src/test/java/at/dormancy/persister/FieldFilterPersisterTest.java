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
package at.dormancy.persister;

import at.dormancy.entity.Application;
import at.dormancy.entity.Employee;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static at.dormancy.AbstractDormancyTest.describe;
import static org.junit.Assert.*;

/**
 * @author Gregor Schauer
 * @since 1.0.1
 */
public class FieldFilterPersisterTest extends AbstractPersisterTest<FieldFilterPersister<Application>> {
	Application app = new Application("app", null, Collections.<Employee>emptySet(), "secret");

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new FieldFilterPersister<Application>(dormancy);
		persister.setFieldFilters(new ReflectionUtils.FieldFilter() {
			@Override
			public boolean matches(Field field) {
				return !"authKey".equals(field.getName());
			}
		});
	}

	@Test
	public void test() {
		Application clone = persister.clone(app);
		assertEquals("secret", app.getAuthKey());
		assertEquals(null, clone.getAuthKey());

		Map<String, ?> appMap = describe(app);
		appMap.put("authKey", null);
		assertEquals(appMap, describe(clone));

		Application merge = persister.merge(app);
		appMap = describe(app);
		appMap.put("authKey", null);
		assertEquals(appMap, describe(merge));
	}

	@Test
	public void testReuse() {
		Application clone = persister.clone(app);
		assertNotSame(app, clone);
		persister.setReuseObject(true);
		clone = persister.clone(app);
		assertSame(app, clone);
	}

	@Test
	public void testFilter() {
		persister.getFieldFilters().set(0, new ReflectionUtils.FieldFilter() {
			@Override
			public boolean matches(Field field) {
				return false;
			}
		});
		Application clone = persister.clone(app);
		Map<String, ?> properties = describe(clone);
		assertEquals(6, properties.size());
		for (Map.Entry<String, ?> entry : properties.entrySet()) {
			assertEquals(null, entry.getValue());
		}
	}
}
