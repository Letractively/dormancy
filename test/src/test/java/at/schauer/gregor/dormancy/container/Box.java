package at.schauer.gregor.dormancy.container;

import at.schauer.gregor.dormancy.entity.Book;

import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
public class Box implements Serializable {
	protected Book book;

	public Box(Book book) {
		this.book = book;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}
}
