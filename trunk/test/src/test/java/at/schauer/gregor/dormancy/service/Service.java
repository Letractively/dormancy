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
package at.schauer.gregor.dormancy.service;

import at.schauer.gregor.dormancy.container.Team;
import at.schauer.gregor.dormancy.entity.Application;
import at.schauer.gregor.dormancy.interceptor.PersistenceEndpoint;
import at.schauer.gregor.dormancy.persister.TeamPersister;

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

	@PersistenceEndpoint(types = TeamPersister.class)
	Team next(Team team);

	Team prev(Team team);

	@PersistenceEndpoint(name = "teamPersister")
	Team pass(Team team);
}
