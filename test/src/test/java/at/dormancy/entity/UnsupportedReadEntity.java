/*
 * Copyright 2014 Gregor Schauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.dormancy.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Gregor Schauer
 */
@Entity
public class UnsupportedReadEntity {
	public Long id;
	public String value;

	public UnsupportedReadEntity() {
	}

	public UnsupportedReadEntity(Long id, String value) {
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
		throw new UnsupportedOperationException();
	}

	public void setValue(String value) {
		this.value = value;
	}
}
