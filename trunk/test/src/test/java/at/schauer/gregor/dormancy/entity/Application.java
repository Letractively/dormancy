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
		final StringBuilder sb = new StringBuilder();
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
