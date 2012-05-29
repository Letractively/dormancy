package at.schauer.gregor.dormancy.container;

import at.schauer.gregor.dormancy.entity.Employee;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gregor Schauer
 */
public class Team implements Serializable {
	protected List<Employee> employees;

	public Team() {
	}

	public Team(Employee employee) {
		this.employees = new ArrayList<Employee>(Collections.singletonList(employee));
	}

	public List<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}
}
