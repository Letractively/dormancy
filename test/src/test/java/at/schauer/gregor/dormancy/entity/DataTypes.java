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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Gregor Schauer
 */
@Entity
public class DataTypes implements Serializable {
	Long id;
	Long longWrapper;
	long longValue;
	Boolean booleanWrapper;
	boolean booleanValue;
	String string;
	Date date;
	Timestamp timestamp;
	int[] intArray;
	Integer[] integerArray;

	public DataTypes() {
	}

	public DataTypes(Long longWrapper, long longValue, Boolean booleanWrapper, boolean booleanValue, String string, Date date, Timestamp timestamp, int[] intArray, Integer[] integerArray) {
		this.longWrapper = longWrapper;
		this.longValue = longValue;
		this.booleanWrapper = booleanWrapper;
		this.booleanValue = booleanValue;
		this.string = string;
		this.date = date;
		this.timestamp = timestamp;
		this.intArray = intArray;
		this.integerArray = integerArray;
	}

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLongWrapper() {
		return longWrapper;
	}

	public void setLongWrapper(Long longWrapper) {
		this.longWrapper = longWrapper;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	public Boolean getBooleanWrapper() {
		return booleanWrapper;
	}

	public void setBooleanWrapper(Boolean booleanWrapper) {
		this.booleanWrapper = booleanWrapper;
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public int[] getIntArray() {
		return intArray;
	}

	public void setIntArray(int[] intArray) {
		this.intArray = intArray;
	}

	public Integer[] getIntegerArray() {
		return integerArray;
	}

	public void setIntegerArray(Integer[] integerArray) {
		this.integerArray = integerArray;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("DataTypes");
		sb.append("{id=").append(id);
		sb.append(", longWrapper=").append(longWrapper);
		sb.append(", longValue=").append(longValue);
		sb.append(", booleanWrapper=").append(booleanWrapper);
		sb.append(", booleanValue=").append(booleanValue);
		sb.append(", string='").append(string).append('\'');
		sb.append(", date=").append(date);
		sb.append(", timestamp=").append(timestamp);
		sb.append(", intArray=").append(intArray == null ? "null" : Arrays.asList(intArray).toString());
		sb.append(", integerArray=").append(integerArray == null ? "null" : Arrays.asList(integerArray).toString());
		sb.append('}');
		return sb.toString();
	}
}
