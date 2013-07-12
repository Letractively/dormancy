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

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author Gregor Schauer
 * @since 1.0.1
 */
@Entity
public class Clock implements Serializable {
	@Id
	@GeneratedValue
	public Long id;
	@Temporal(TemporalType.TIMESTAMP)
	public Date date;
	public java.sql.Date sqlDate;
	public Timestamp timestamp;

	public Clock() {
		update();
	}

	public void update() {
		date = new Date();
		sqlDate = new java.sql.Date(date.getTime());
		timestamp = new Timestamp(date.getTime());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public java.sql.Date getSqlDate() {
		return sqlDate;
	}

	public void setSqlDate(java.sql.Date sqlDate) {
		this.sqlDate = sqlDate;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		Clock clock = (Clock) obj;

		return date.equals(clock.date) && id.equals(clock.id)
				&& sqlDate.equals(clock.sqlDate) && timestamp.equals(clock.timestamp);

	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : System.identityHashCode(this);
	}
}
