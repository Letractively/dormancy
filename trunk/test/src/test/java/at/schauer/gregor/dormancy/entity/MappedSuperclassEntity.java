package at.schauer.gregor.dormancy.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * Created by gregor on 7/14/13.
 */
@Entity
public class MappedSuperclassEntity extends MappedSuperclassParent {
	MappedSuperclassEntity next;

	@OneToOne(fetch = FetchType.LAZY)
	public MappedSuperclassEntity getNext() {
		return next;
	}

	public void setNext(MappedSuperclassEntity next) {
		this.next = next;
	}
}
