package at.schauer.gregor.dormancy.entity;

import org.hibernate.annotations.CollectionOfElements;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gregor Schauer
 */
@Entity
public class CollectionEntity implements Serializable {
	Long id;
	List<Integer> integers = new ArrayList<Integer>();
	List<Book> books;
	Map<Long, Long> longMap;
	Map<Long, Book> bookMap;

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ElementCollection
	@CollectionOfElements
	public List<Integer> getIntegers() {
		return integers;
	}

	public void setIntegers(List<Integer> integers) {
		this.integers = integers;
	}

	@OneToMany
	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}

	@ElementCollection
	@CollectionOfElements
	public Map<Long, Long> getLongMap() {
		return longMap;
	}

	public void setLongMap(Map<Long, Long> longMap) {
		this.longMap = longMap;
	}

	@OneToMany @JoinTable(name="Cust_Order")
	@MapKeyColumn(name = "orders_number")
	public Map<Long, Book> getBookMap() {
		if (bookMap == null) {
			bookMap = new HashMap<Long, Book>();
		}
		return bookMap;
	}

	public void setBookMap(Map<Long, Book> bookMap) {
		this.bookMap = bookMap;
	}
}
