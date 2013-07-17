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
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class PredicatePersisterTest extends AbstractPersisterTest<PredicatePersister<Application, Predicate>> {
	Application app = new Application("app", null, Collections.<Employee>emptySet(), "secret");

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
		persister = new PredicatePersister<Application, Predicate>();
	}

	@Test
	public void test() {
		persister.setPredicateDelegate(NoOpPersister.<Application>getInstance());
		persister.setFallbackDelegate(NullPersister.<Application>getInstance());

		persister.setPredicate(PredicateUtils.truePredicate());
		Application clone = persister.clone(app);
		Application merge = persister.merge(app);
		assertSame(app, clone);
		assertSame(app, merge);

		persister.setPredicate(PredicateUtils.falsePredicate());
		clone = persister.clone(app);
		merge = persister.merge(app);
		assertEquals(null, clone);
		assertEquals(null, merge);
	}
}
