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
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * Created by gregor on 7/14/13.
 */
@Entity
public class MappedSuperclassEntity extends MappedSuperclassParent {
	MappedSuperclassEntity next;

	@OneToOne(fetch = FetchType.LAZY)
	public MappedSuperclassEntity getNext() {
		return next;
	}

	public void setNext(MappedSuperclassEntity next) {
		this.next = next;
	}
}
