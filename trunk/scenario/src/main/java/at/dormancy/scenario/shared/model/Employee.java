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
package at.dormancy.scenario.shared.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Gregor Schauer
 * @since 1.0.2
 */
@Entity
public class Employee implements Serializable {
	Long id;
	String name;
	Employee boss;
	Long version;
	Set<Employee> employees = new LinkedHashSet<Employee>();
	Set<Employee> colleagues;

	public Employee() {
	}

	public Employee(String name, Employee boss) {
		this.name = name;
		this.boss = boss;
	}

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	public Employee getBoss() {
		return boss;
	}

	public void setBoss(Employee boss) {
		this.boss = boss;
	}

	@JoinTable(name = "Members",
			joinColumns = @JoinColumn(name = "employee"),
			inverseJoinColumns = @JoinColumn(name = "boss"))
	@OneToMany(fetch = FetchType.LAZY)
	public Set<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(Set<Employee> employees) {
		this.employees = employees;
	}

	@JoinTable(name = "Colleague",
			joinColumns = @JoinColumn(name = "employee"),
			inverseJoinColumns = @JoinColumn(name = "boss"))
	@OneToMany(fetch = FetchType.LAZY)
	public Set<Employee> getColleagues() {
		return colleagues;
	}

	public void setColleagues(Set<Employee> colleagues) {
		this.colleagues = colleagues;
	}

	@Version
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Employee");
		sb.append("{id=").append(id);
		sb.append(", version=").append(version);
		sb.append(", name='").append(name).append('\'');
		sb.append(", boss=").append(boss);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		Employee employee = (Employee) obj;

		return !(id == null ? employee.id != null : !id.equals(employee.id));

	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
