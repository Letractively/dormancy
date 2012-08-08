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

import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.entity.Employee;
import at.schauer.gregor.dormancy.container.Holder;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static at.schauer.gregor.dormancy.AbstractDormancyTest.describe;
import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 * @since 1.0.3
 */
public class SimplePersisterTest extends PersisterTest<SimplePersister<Object>> {
	Application app = new Application("app", null, Collections.<Employee>emptySet(), "secret");

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		app.setId(1L);
		persister = new SimplePersister<Object>(dormancy);
		persister.setPackageNames("at.schauer.gregor.dormancy.");
	}

	@Test
	@Transactional
	public void test() {
		sessionFactory.getCurrentSession().save(app);


		Object clone = persister.clone(app);
		assertEquals(describe(app), describe(clone));

		Object merge = persister.merge(app);
		assertEquals(describe(app), describe(merge));


		List<Application> applications = new ArrayList<Application>();
		clone = persister.clone(applications);
		assertEquals(applications, clone);

		merge = persister.merge(applications);
		assertEquals(describe(applications), describe(merge));


		app.setAuthKey(null);
		Holder<Object> holder = new Holder<Object>(app);
		clone = persister.clone(holder);
		assertEquals(describe(holder), describe(clone));

		merge = persister.merge(holder);
		assertEquals(describe(holder), describe(merge));
	}
}
