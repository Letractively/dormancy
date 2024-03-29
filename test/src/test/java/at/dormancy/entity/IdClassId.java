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

import org.apache.commons.lang.builder.EqualsBuilder;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * @author Gregor Schauer
 */
public class IdClassId implements Serializable {
	@Column(name = "id")
	public Long id;
	@Column(name = "time")
	public Long timestamp;

	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
		return result;
	}
}
