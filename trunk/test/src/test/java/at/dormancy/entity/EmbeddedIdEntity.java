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
package at.dormancy.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
@Entity
public class EmbeddedIdEntity implements Serializable {
	@EmbeddedId
	private EmbeddableEntity embeddableEntity;
	private String value;

	public EmbeddableEntity getEmbeddableEntity() {
		return embeddableEntity;
	}

	public void setEmbeddableEntity(EmbeddableEntity embeddableEntity) {
		this.embeddableEntity = embeddableEntity;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("EmbeddedIdEntity");
		sb.append("{embeddableEntity=").append(embeddableEntity);
		sb.append(", value='").append(value).append('\'');
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

		EmbeddedIdEntity that = (EmbeddedIdEntity) o;

		if (!embeddableEntity.equals(that.embeddableEntity)) {
			return false;
		}
		if (!value.equals(that.value)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = embeddableEntity.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}
}
