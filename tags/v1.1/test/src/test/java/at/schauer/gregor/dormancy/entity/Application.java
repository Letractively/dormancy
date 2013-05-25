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
package at.schauer.gregor.dormancy.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * @author Gregor Schauer
 */
@Entity
public class Application implements Serializable {
	Long id;
	Long lastUpdate;
	String name;
	Employee responsibleUser;
	Set<Employee> employees;
	String authKey;

	public Application() {
	}

	public Application(String name, Employee responsibleUser, Set<Employee> employees, String authKey) {
		this.name = name;
		this.responsibleUser = responsibleUser;
		this.employees = employees;
		this.authKey = authKey;
	}

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Version
	public Long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public Employee getResponsibleUser() {
		return responsibleUser;
	}

	public void setResponsibleUser(Employee responsibleUser) {
		this.responsibleUser = responsibleUser;
	}

	@OneToMany
	public Set<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(Set<Employee> employees) {
		this.employees = employees;
	}

	@Transient
	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Application");
		sb.append("{id=").append(id);
		sb.append(", lastUpdate=").append(lastUpdate);
		sb.append(", name='").append(name).append('\'');
		sb.append(", responsibleUser=").append(responsibleUser);
		sb.append(", employees=").append(employees);
		sb.append(", authKey='").append(authKey).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
