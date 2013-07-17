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

import at.dormancy.container.Holder;
import at.dormancy.entity.Application;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static at.dormancy.AbstractDormancyTest.describe;
import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 * @since 1.0.3
 */
public class SimplePersisterTest extends AbstractPersisterTest<SimplePersister<Object>> {
	Application app = new Application("app", null, new LinkedHashSet<Employee>(), "secret");

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		app.setId(1L);
		persister = new SimplePersister<Object>(dormancy);
		persister.setPackageNames("at.dormancy.");
	}

	@Test
	@Transactional
	public void test() {
		genericService.save(app);
		app = genericService.load(Application.class, app.getId());

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
