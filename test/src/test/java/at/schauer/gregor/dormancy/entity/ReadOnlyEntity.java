package at.schauer.gregor.dormancy.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Gregor Schauer
 */
@Entity
public class ReadOnlyEntity {
	public Long id;
	public String value;

	public ReadOnlyEntity() {
	}

	public ReadOnlyEntity(Long id, String value) {
		this.id = id;
		this.value = value;
	}

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		throw new UnsupportedOperationException();
	}
}
