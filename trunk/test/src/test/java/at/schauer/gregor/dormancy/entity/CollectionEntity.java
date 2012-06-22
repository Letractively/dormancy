/*
 * Copyright 2012 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
