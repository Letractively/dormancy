/*
 * Copyright 2013 Gregor Schauer
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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
@Entity
public class CompositeEntity implements Serializable {
	@OneToOne
	@Id
	public Employee employee;
	@OneToOne
	@Id
	public Book book;
	public String value;

	public CompositeEntity() {
	}

	public CompositeEntity(Employee employee, Book book, String value) {
		this.employee = employee;
		this.book = book;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		CompositeEntity that = (CompositeEntity) obj;

		if (!book.equals(that.book)) {
			return false;
		}
		if (!employee.equals(that.employee)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = employee.hashCode();
		result = 31 * result + book.hashCode();
		return result;
	}
}
