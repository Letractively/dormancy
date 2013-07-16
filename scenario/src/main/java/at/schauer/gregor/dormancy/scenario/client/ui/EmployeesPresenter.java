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
package at.schauer.gregor.dormancy.scenario.client.ui;

import at.schauer.gregor.dormancy.scenario.client.service.DormancyScenarioServiceAsync;
import at.schauer.gregor.dormancy.scenario.shared.model.Employee;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.view.client.HasData;

import java.util.ArrayList;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class EmployeesPresenter {
	public interface Display {
		HasText getNameTextBox();
		HasClickHandlers getSaveButton();
		HasClickHandlers getCancelButton();
		HasClickHandlers getCreateButton();
		HasData<Employee> getTable();
		void setEmployee(Employee object);
		Employee getEmployee();
	}

	private final DormancyScenarioServiceAsync service;
	private final EmployeesView view;

	public EmployeesPresenter(final EmployeesView view, final DormancyScenarioServiceAsync service) {
		this.view = view;
		this.service = service;

		view.getSaveButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				view.getEmployee().setName(view.getNameTextBox().getText());
				service.save(view.getEmployee(), new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}

					@Override
					public void onSuccess(Void result) {
						reload();
					}
				});
			}
		});

		view.getCancelButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				view.setEmployee(null);
			}
		});

		view.getCreateButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Employee employee = new Employee();
				employee.setVersion(1L);
				view.setEmployee(employee);
			}
		});
	}

	public void reload() {
		view.setEmployee(null);
		service.listEmployees(new AsyncCallback<ArrayList<Employee>>() {
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}

			@Override
			public void onSuccess(ArrayList<Employee> result) {
				view.getTable().setRowData(0, result);
			}
		});
	}
}
