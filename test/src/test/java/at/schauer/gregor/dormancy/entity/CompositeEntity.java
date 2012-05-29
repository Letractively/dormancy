package at.schauer.gregor.dormancy.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
@Entity
public class CompositeEntity implements Serializable {
	@OneToOne
	@Id
	public Employee employee;
	@OneToOne
	@Id
	public Book book;
	public String value;

	public CompositeEntity() {
	}

	public CompositeEntity(Employee employee, Book book, String value) {
		this.employee = employee;
		this.book = book;
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CompositeEntity that = (CompositeEntity) o;

		if (!book.equals(that.book)) {
			return false;
		}
		if (!employee.equals(that.employee)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = employee.hashCode();
		result = 31 * result + book.hashCode();
		return result;
	}
}
