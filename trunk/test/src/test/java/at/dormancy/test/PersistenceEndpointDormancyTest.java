/*
 * Copyright 2014 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import at.dormancy.handler.BasicTypeHandler;
import at.dormancy.handler.CollectionHandler;
import at.dormancy.handler.ObjectHandler;
import at.dormancy.handler.TeamHandler;
import org.junit.Test;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.springframework.test.util.ReflectionTestUtils.getField;

/**
 * @author Gregor Schauer
 */
public class PersistenceEndpointDormancyTest extends AbstractDormancyTest implements ApplicationContextAware {
	@SuppressWarnings("unchecked")
	@Test
	public void testObjectHandlerTypePersistenceEndpoint() {
		((Map<Class<?>, ObjectHandler<?>>) getField(dormancy.getRegistry(), "handlerMap")).clear();
		dormancy.getRegistry().addObjectHandler(new TeamHandler(dormancy), Team.class);
		dormancy.getRegistry().addObjectHandler(new CollectionHandler<List<?>>(dormancy), List.class);
		dormancy.getRegistry().addObjectHandler(new BasicTypeHandler<Object>());

		genericService.save(new Book(UUID.randomUUID().toString()));
		Team team = new Team(service.get(Employee.class, refA.getId()));

		Team pass = service.next(team);
		assertEquals(Collections.<Employee>emptySet(), pass.getEmployees().get(0).getEmployees());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testObjectHandlerNamePersistenceEndpoint() {
		Map<Class<?>, ObjectHandler<?>> handlerMap =
				(Map<Class<?>, ObjectHandler<?>>) getField(dormancy.getRegistry(), "handlerMap");
		handlerMap.clear();
		handlerMap.put(TeamHandler.class, new TeamHandler(dormancy));
		handlerMap.put(List.class, new CollectionHandler<Collection>(dormancy));
		dormancy.getRegistry().addObjectHandler(new BasicTypeHandler<Object>());

		genericService.save(new Book(UUID.randomUUID().toString()));
		Team team = new Team(service.get(Employee.class, refA.getId()));

		Team pass = service.pass(team);
		assertEquals(Collections.<Employee>emptySet(), pass.getEmployees().get(0).getEmployees());
		assertNotSame(team.getEmployees().get(0), pass.getEmployees().get(0));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		GenericApplicationContext ctx = GenericApplicationContext.class.cast(applicationContext);
		ConstructorArgumentValues values = new ConstructorArgumentValues();
		values.addIndexedArgumentValue(0, ctx.getBean(Dormancy.class));
		ctx.registerBeanDefinition("teamHandler", new RootBeanDefinition(TeamHandler.class, values, null));
	}
}
