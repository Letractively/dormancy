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

import at.dormancy.access.AccessType;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.util.*;

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

	Calendar calendar;
	TimeZone timeZone;
	Currency currency;
	Locale locale;
	Class<?> clazz;
	AccessType accessType;
	URL url;
	UUID uuid;
	Blob blob;
	Clob clob;

	public DataTypes() {
	}

	public DataTypes(Long longWrapper, long longValue, Boolean booleanWrapper, boolean booleanValue, String string,
					 Date date, Timestamp timestamp, Calendar calendar, TimeZone timeZone,
					 Currency currency, Locale locale,
					 Class<?> clazz, AccessType accessType, URL url, UUID uuid,
					 Blob blob, Clob clob) {
		this.longWrapper = longWrapper;
		this.longValue = longValue;
		this.booleanWrapper = booleanWrapper;
		this.booleanValue = booleanValue;
		this.string = string;
		this.date = date;
		this.timestamp = timestamp;
		this.calendar = calendar;
		this.timeZone = timeZone;
		this.currency = currency;
		this.locale = locale;
		this.clazz = clazz;
		this.accessType = accessType;
		this.url = url;
		this.uuid = uuid;
		this.blob = blob;
		this.clob = clob;
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

	@Temporal(TemporalType.TIMESTAMP)
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

	@Temporal(TemporalType.DATE)
	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public AccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(AccessType accessType) {
		this.accessType = accessType;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	@Lob
	public Blob getBlob() {
		return blob;
	}

	public void setBlob(Blob blob) {
		this.blob = blob;
	}

	@Lob
	public Clob getClob() {
		return clob;
	}

	public void setClob(Clob clob) {
		this.clob = clob;
	}
}
