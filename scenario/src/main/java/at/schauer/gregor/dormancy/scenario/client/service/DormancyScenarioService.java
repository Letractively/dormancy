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
package at.schauer.gregor.dormancy.scenario.client.service;

import at.schauer.gregor.dormancy.scenario.shared.model.Employee;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.ArrayList;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
@RemoteServiceRelativePath("DormancyScenarioService")
public interface DormancyScenarioService extends RemoteService {
	class App {
		private static final DormancyScenarioServiceAsync instance = (DormancyScenarioServiceAsync) GWT.create(DormancyScenarioService.class);
		public static DormancyScenarioServiceAsync getInstance() {
			return instance;
		}
	}
	ArrayList<Employee> listEmployees();

	void save(Employee employee);
}
