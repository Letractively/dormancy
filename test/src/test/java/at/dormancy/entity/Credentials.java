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

import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
@Entity
public class Credentials implements Serializable {
	String username;
	String password;

	private Credentials() {
	}

	public Credentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Id
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@javax.persistence.Access(AccessType.FIELD)
	@SuppressWarnings("deprecation")
	@org.hibernate.annotations.AccessType("field")
	@Transient
	public String getPassword() {
		return password;
	}
}
