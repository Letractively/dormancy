package at.schauer.gregor.dormancy.entity;

import org.apache.commons.lang.builder.EqualsBuilder;

import java.io.Serializable;

/**
 * Created by gregor on 7/14/13.
 */
public class IdClassPk implements Serializable {
	public Long id;
	public Long timestamp;

	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
		return result;
	}
}
