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

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
@Entity
public class MultipleIdEntity implements Serializable {
	@Id
	public Long id;
	@Id
	public Long time;
	public String value;

	public MultipleIdEntity() {
	}

	public MultipleIdEntity(Long id, Long time, String value) {
		this.id = id;
		this.time = time;
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

		MultipleIdEntity that = (MultipleIdEntity) obj;

		if (!id.equals(that.id)) {
			return false;
		}
		if (!time.equals(that.time)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + time.hashCode();
		return result;
	}
}
