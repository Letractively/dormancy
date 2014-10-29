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
package at.dormancy.service;

import at.dormancy.aop.PersistenceEndpoint;
import at.dormancy.container.Team;
import at.dormancy.entity.Application;
import at.dormancy.handler.TeamHandler;

import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
public interface Service {
	void doNothing();

	Serializable save(Serializable obj);

	Application loadApp(Long id);

	<T extends Serializable> T get(Class<T> type, Long id);

	<T extends Serializable> T load(Class<T> type, Long id);

	@PersistenceEndpoint(types = TeamHandler.class)
	Team next(Team team);

	Team prev(Team team);

	@PersistenceEndpoint(types = TeamHandler.class)
	Team pass(Team team);

	void throwException();
}
