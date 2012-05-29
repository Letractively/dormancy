package at.schauer.gregor.dormancy.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
@Entity
public class CompositeKey implements Serializable {
	@Id
	public Long id;
	@Id
	public Long time;
	public String value;

	public CompositeKey() {
	}

	public CompositeKey(Long id, Long time, String value) {
		this.id = id;
		this.time = time;
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

		CompositeKey that = (CompositeKey) o;

		if (!id.equals(that.id)) {
			return false;
		}
		if (!time.equals(that.time)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + time.hashCode();
		return result;
	}
}
