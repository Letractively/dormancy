package at.schauer.gregor.dormancy.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Gregor Schauer
 */
@Entity
public class InvalidEntity {
	@Id
	public int id;

	public InvalidEntity(boolean arg) {
	}
}
