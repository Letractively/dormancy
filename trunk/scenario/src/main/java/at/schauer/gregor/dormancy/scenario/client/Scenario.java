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
package at.dormancy.scenario.client;

import at.dormancy.scenario.client.service.DormancyScenarioService;
import at.dormancy.scenario.client.service.DormancyScenarioServiceAsync;
import at.dormancy.scenario.client.ui.EmployeesPresenter;
import at.dormancy.scenario.client.ui.EmployeesView;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class Scenario implements EntryPoint {
	@Override
	public void onModuleLoad() {
		DormancyScenarioServiceAsync service = GWT.create(DormancyScenarioService.class);
		EmployeesView view = new EmployeesView();
		RootPanel.get().add(view);
		EmployeesPresenter presenter = new EmployeesPresenter(view, service);
		presenter.reload();
	}
}
