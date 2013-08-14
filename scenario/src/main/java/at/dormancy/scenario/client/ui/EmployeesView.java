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
package at.dormancy.scenario.client.ui;

import at.dormancy.scenario.shared.model.Employee;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.HasData;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
public class EmployeesView extends Composite implements EmployeesPresenter.Display {
	private static EmployeesUiBinder uiBinder = GWT.create(EmployeesUiBinder.class);
	interface EmployeesUiBinder extends UiBinder<HTMLPanel, EmployeesView> {
	}

	@UiField
	SimplePanel mainPanel;
	@UiField
	TextBox idTextBox;
	@UiField
	TextBox nameTextBox;
	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Button createButton;

	CellTable<Employee> table;
	Employee employee;

	public EmployeesView() {
		initWidget(uiBinder.createAndBindUi(this));
		table = new CellTable<Employee>();
		mainPanel.setWidget(table);

		table.addColumn(new TextColumn<Employee>() {
			@Override
			public String getValue(Employee object) {
				return String.valueOf(object.getId());
			}
		}, "ID");
		table.addColumn(new TextColumn<Employee>() {
			@Override
			public String getValue(Employee object) {
				return object.getName();
			}
		}, "Name");
		Column<Employee, String> categoryColumn = new Column<Employee, String>(new ButtonCell()) {
			@Override
			public String getValue(Employee object) {
				return "Edit";
			}
		};
		categoryColumn.setFieldUpdater(new FieldUpdater<Employee, String>() {
			@Override
			public void update(int index, Employee object, String value) {
				setEmployee(object);
			}
		});
		table.addColumn(categoryColumn, "Edit");
	}

	@Override
	public void setEmployee(Employee object) {
		boolean enabled = object != null;
		employee = object;

		idTextBox.setEnabled(enabled);
		nameTextBox.setEnabled(enabled);
		saveButton.setEnabled(enabled);
		cancelButton.setEnabled(enabled);

		if (enabled) {
			idTextBox.setValue(object.getId() != null ? String.valueOf(object.getId()) : "");
			nameTextBox.setValue(object.getName());
		} else {
			idTextBox.setValue("");
			nameTextBox.setValue("");
		}
	}

	@Override
	public HasText getNameTextBox() {
		return nameTextBox;
	}

	@Override
	public HasClickHandlers getSaveButton() {
		return saveButton;
	}

	@Override
	public HasClickHandlers getCancelButton() {
		return cancelButton;
	}

	@Override
	public HasClickHandlers getCreateButton() {
		return createButton;
	}

	@Override
	public HasData<Employee> getTable() {
		return table;
	}

	@Override
	public Employee getEmployee() {
		return employee;
	}
}
