package br.eti.erickcouto.occultflashtag;

/*
 * Copyright (C) 2015 Erick Couto
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

import android.app.Application;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Event {

	private Long eventId;

	private int activeCheckpointPosition;
	private SortedSet<Long> checkpoints;
	private SortedSet<Long> registeredTime;

	private Date startDate;
	private String body1;
	private String body2;
	private String type;
	private String status; //PR = Production Used / ER = Error by Reboot

	private Double altitude;
	private Double latitude;
	private Double longitude;

	private Boolean synced;

	private String notes;

	public Event() {
	}

	public Event(SortedSet<Long> checkpoints) {
		this.checkpoints = checkpoints;
		this.registeredTime = new TreeSet<>();
		this.activeCheckpointPosition = 0;
	}

	public SortedSet<Long> getCheckpoints() {
		return checkpoints;
	}

	public void addRegisteredTime(Long time){
		this.registeredTime.add(time);
		this.activeCheckpointPosition++;
	}

	public SortedSet<Long> getAllRegisteredTimes(){
		return registeredTime;
	}

	public Long nextCheckpointTime(){
		if(activeCheckpointPosition >= checkpoints.size()){
			return null;
		}

		return (Long) checkpoints.toArray()[activeCheckpointPosition];
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}


	public Double getAltitude() {
		return altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getBody1() {
		return body1;
	}

	public void setBody1(String body1) {
		this.body1 = body1;
	}

	public String getBody2() {
		return body2;
	}

	public void setBody2(String body2) {
		this.body2 = body2;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
	    this.status = status;
	}

	public Boolean getSynced() { return synced;	}

	public void setSynced(Boolean synced) {	this.synced = synced; }

	public String getNotes() { return notes; }

	public void setNotes(String notes) { this.notes = notes; }


}