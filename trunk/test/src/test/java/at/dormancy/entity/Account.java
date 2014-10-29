package at.dormancy.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Account implements Serializable {
	@Id
	@OneToOne
	@JoinColumns({
			@JoinColumn(name = "employee_id", referencedColumnName = "id")
	})
	Employee employee;

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Account account = (Account) o;
		return employee != null ? employee.equals(account.employee) : account.employee == null;
	}

	@Override
	public int hashCode() {
		return employee != null ? employee.hashCode() : 0;
	}
}
