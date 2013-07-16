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

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
@Embeddable
public class EmbeddableEntity implements Serializable {
	static transient long counter;
	private Long id = ++counter;
	private Long timestamp = System.currentTimeMillis();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("EmbeddableEntity");
		sb.append("{id=").append(id);
		sb.append(", timestamp=").append(timestamp);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		EmbeddableEntity that = (EmbeddableEntity) o;

		if (!id.equals(that.id)) {
			return false;
		}
		if (!timestamp.equals(that.timestamp)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + timestamp.hashCode();
		return result;
	}
}
