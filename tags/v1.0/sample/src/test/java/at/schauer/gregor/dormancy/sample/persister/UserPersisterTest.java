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
package at.schauer.gregor.dormancy.sample.persister;

import at.schauer.gregor.dormancy.Dormancy;
import at.schauer.gregor.dormancy.sample.SampleSpringConfig;
import at.schauer.gregor.dormancy.sample.model.User;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
@ContextConfiguration(classes = SampleSpringConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class UserPersisterTest {
	@Inject
	Dormancy dormancy;
	@Inject
	UserPersister userPersister;
	@Inject
	SessionFactory sessionFactory;

	@Test
	@Transactional
	public void test() throws Exception {
		User user = new User("johndeere", "john.deere@acme.org", "secret");
		sessionFactory.getCurrentSession().save(user);
		sessionFactory.getCurrentSession().flush();

		User clone = dormancy.clone(user);

		assertEquals(user.getUsername(), clone.getUsername());
		assertEquals(user.getMail(), clone.getMail());
		assertEquals(null, clone.getPassword());

		// clone.setUsername("jdeere");
		clone.setMail("jdeere@acme.org");
		clone.setPassword("public");

		User merge = dormancy.merge(clone);
		assertEquals(user.getUsername(), merge.getUsername());
		assertEquals(clone.getMail(), merge.getMail());
		assertEquals(user.getPassword(), merge.getPassword());

		clone.setUsername("jdeere");
		clone.setMail("jdeere@acme.org");
		clone.setPassword("public");

		merge = (User) dormancy.merge(clone, sessionFactory.getCurrentSession().get(User.class, user.getUsername()));
		assertEquals(user.getUsername(), merge.getUsername());
		assertEquals(clone.getMail(), merge.getMail());
		assertEquals(user.getPassword(), merge.getPassword());
	}

	@Test
	public void testNull() {
		assertEquals(null, userPersister.clone(null));
		assertEquals(null, userPersister.merge(null));
		assertEquals(null, userPersister.merge(null, null));
	}
}
