package at.schauer.gregor.dormancy.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Gregor Schauer
 */
@Entity
public class Employee implements Serializable {
	Long id;
	String name;
	Employee boss;
	Long version;
	transient Serializable serializable;
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

	public Serializable getSerializable() {
		return serializable;
	}

	public void setSerializable(Serializable serializable) {
		this.serializable = serializable;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Employee");
		sb.append("{id=").append(id);
		sb.append(", version=").append(version);
		sb.append(", name='").append(name).append('\'');
		sb.append(", boss=").append(boss);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Employee employee = (Employee) o;

		if (id != null ? !id.equals(employee.id) : employee.id != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
