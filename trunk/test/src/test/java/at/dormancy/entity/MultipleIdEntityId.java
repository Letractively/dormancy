package at.dormancy.entity;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
public class MultipleIdEntityId implements Serializable {
	@Id
	public Long id;
	@Id
	public Long time;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MultipleIdEntityId that = (MultipleIdEntityId) o;
		return id.equals(that.id) && time.equals(that.time);
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + time.hashCode();
		return result;
	}
}
