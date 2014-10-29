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
package at.dormancy.handler;

import at.dormancy.Dormancy;
import at.dormancy.container.Team;
import at.dormancy.entity.Employee;
import at.dormancy.util.DormancyContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

/**
 * @author Gregor Schauer
 */
public class TeamHandler implements ObjectHandler<Team> {
	Dormancy<Object, Object, Object> dormancy;

	@Inject
	public TeamHandler(Dormancy<Object, Object, Object> dormancy) {
		this.dormancy = dormancy;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <O extends Team> O createObject(@Nonnull O obj) {
		return (O) new Team();
	}

	@Nullable
	@Override
	public <R extends Team, O extends R> R disconnect(O dbObj, @Nonnull DormancyContext ctx) {
		List<Employee> disconnected = dormancy.asObjectHandler().disconnect(dbObj.getEmployees(), ctx);
		dbObj.setEmployees(disconnected);
		return dbObj;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <O extends Team, R extends O> R apply(O trObj, R dbObj, @Nonnull DormancyContext ctx) {
		List<Employee> merged = dbObj == null
				? dormancy.asObjectHandler().apply(trObj.getEmployees(), ctx)
				: dormancy.asObjectHandler().apply(trObj.getEmployees(), dbObj.getEmployees(), ctx);
		trObj.setEmployees(merged);
		return (R) trObj;
	}
}
