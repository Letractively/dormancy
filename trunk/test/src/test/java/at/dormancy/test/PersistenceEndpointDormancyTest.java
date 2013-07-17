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
package at.dormancy.test;

import at.dormancy.AbstractDormancyTest;
import at.dormancy.Dormancy;
import at.dormancy.container.Team;
import at.dormancy.entity.Book;
import at.dormancy.entity.Employee;
import at.dormancy.persister.CollectionPersister;
import at.dormancy.persister.NoOpPersister;
import at.dormancy.persister.TeamPersister;
import org.junit.Test;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author Gregor Schauer
 */
public class PersistenceEndpointDormancyTest extends AbstractDormancyTest implements ApplicationContextAware {
	@Test
	public void testPersisterTypePersistenceEndpoint() {
		dormancy.getPersisterMap().clear();
		dormancy.addEntityPersister(new TeamPersister(dormancy), Team.class);
		dormancy.addEntityPersister(new CollectionPersister<List<?>>(dormancy), List.class);
		dormancy.addEntityPersister(NoOpPersister.getInstance());

		genericService.save(new Book(UUID.randomUUID().toString()));
		Team team = new Team(service.get(Employee.class, refA.getId()));

		Team pass = service.next(team);
		assertEquals(Collections.<Employee>emptySet(), pass.getEmployees().get(0).getEmployees());
	}

	@Test
	public void testPersisterNamePersistenceEndpoint() {
		dormancy.getPersisterMap().clear();
		dormancy.getPersisterMap().put(TeamPersister.class, new TeamPersister(dormancy));
		dormancy.getPersisterMap().put(List.class, new CollectionPersister<List<?>>(dormancy));
		dormancy.addEntityPersister(NoOpPersister.getInstance());

		genericService.save(new Book(UUID.randomUUID().toString()));
		Team team = new Team(service.get(Employee.class, refA.getId()));

		Team pass = service.pass(team);
		assertEquals(Collections.<Employee>emptySet(), pass.getEmployees().get(0).getEmployees());
		assertNotSame(team.getEmployees().get(0), pass.getEmployees().get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnavailablePersisterPersistenceEndpoint() {
		if (isJpa()) {
			throw new IllegalArgumentException();
		}

		dormancy.getPersisterMap().clear();
		dormancy.addEntityPersister(NoOpPersister.getInstance());
		genericService.save(new Book(UUID.randomUUID().toString()));

		service.next(new Team(service.get(Employee.class, refA.getId())));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		GenericApplicationContext ctx = GenericApplicationContext.class.cast(applicationContext);
		ConstructorArgumentValues values = new ConstructorArgumentValues();
		values.addIndexedArgumentValue(0, ctx.getBean(Dormancy.class));
		ctx.registerBeanDefinition("teamPersister", new RootBeanDefinition(TeamPersister.class, values, null));
	}
}
