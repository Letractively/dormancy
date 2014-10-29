package at.dormancy.entity;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
public class CompositeEntityId implements Serializable {
	@Id
	public Long employee;
	@Id
	public Long book;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CompositeEntityId that = (CompositeEntityId) o;
		return book.equals(that.book) && employee.equals(that.employee);
	}

	@Override
	public int hashCode() {
		int result = employee.hashCode();
		result = 31 * result + book.hashCode();
		return result;
	}
}
