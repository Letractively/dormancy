package at.schauer.gregor.dormancy.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by gregor on 7/14/13.
 */
@MappedSuperclass
public abstract class MappedSuperclassParent implements Serializable {
	Long id;
	String value;

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
