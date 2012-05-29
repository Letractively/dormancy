package at.schauer.gregor.dormancy.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
@Entity
public class Book implements Serializable {
	@Id
	@GeneratedValue
	Long id;
	@Basic
	String title;

	public Book() {
	}

	public Book(String title) {
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Book");
		sb.append("{id=").append(id);
		sb.append(", title='").append(title).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
