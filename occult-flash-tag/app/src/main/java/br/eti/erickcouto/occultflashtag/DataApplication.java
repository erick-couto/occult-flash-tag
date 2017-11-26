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

import java.util.HashSet;
import java.util.Set;

import android.app.Application;

public class DataApplication extends Application {

	private Event event;
	private String currentEvent;
	private String checkpointBody1;
	private String checkpointBody2;
	private String checkpointEvent;
	private Long checkpoint1ntp;
	private Long checkpoint2ntp;

	
	private Long ntpTimeFromCurrentCheckpoint;
	private Long ntpTimeFromStart;
	private Long systemTimeFromStart;
	private Long systemTimeFromEnd;
	private Long systemTimeZoneDiff;

	private Integer currentCheckpointNumber = 0;

	private Long ntpFirstCheckpoint;
	private Long ntpSecondCheckpoint;

	private Long timeForStart;
	private Long timeForEnd;

	private Long timeControlUptimeStart;
	private Long timeControlCountdown;

	private Double currentAltitude;
	private Double currentLatitude;
	private Double currentLongitude;
	private Double checkpointAltitude;
	private Double checkpointLatitude;
	private Double checkpointLongitude;

	public Long getTimeForStart() {
		return timeForStart;
	}

	public void setTimeForStart(Long timeForStart) {
		this.timeForStart = timeForStart;
	}

	public Long getTimeForEnd() {
		return timeForEnd;
	}

	public void setTimeForEnd(Long timeForEnd) {
		this.timeForEnd = timeForEnd;
	}

	public void setTimeControlUptimeStart(Long time) {
		this.timeControlUptimeStart = time;
	}

	public void setTimeControlCountdown(Long time) {
		this.timeControlCountdown = time;
	}

	public void setNtpTimeFromStart(Long time) {
		this.ntpTimeFromStart = time;
	}

	public void setSystemTimeFromStart(Long time) {
		this.systemTimeFromStart = time;
	}

	public void setSystemTimeFromEnd(Long time) {
		this.systemTimeFromEnd = time;
	}

	public void setNtpTimeFromCurrentCheckpoint(Long time) {
		this.ntpTimeFromCurrentCheckpoint = time;
	}

	public void setCurrentCheckpointNumber(Integer number) {
		this.currentCheckpointNumber = number;
	}

	public void setNtpFirstCheckpoint(Long ntpFirstCheckpoint) {
		this.ntpFirstCheckpoint = ntpFirstCheckpoint;
	}

	public void setNtpSecondCheckpoint(Long ntpSecondCheckpoint) {
		this.ntpSecondCheckpoint = ntpSecondCheckpoint;
	}

	public Long getSystemTimeZoneDiff() {
		return systemTimeZoneDiff;
	}

	public Double getCurrentAltitude() {
		return currentAltitude;
	}

	public void setCurrentAltitude(Double currentAltitude) {
		this.currentAltitude = currentAltitude;
	}

	public Double getCurrentLatitude() {
		return currentLatitude;
	}

	public void setCurrentLatitude(Double currentLatitude) {
		this.currentLatitude = currentLatitude;
	}

	public Double getCurrentLongitude() {
		return currentLongitude;
	}

	public void setCurrentLongitude(Double currentLongitude) {
		this.currentLongitude = currentLongitude;
	}

	public void setCheckpointAltitude(Double checkpointAltitude) {
		this.checkpointAltitude = checkpointAltitude;
	}

	public void setCheckpointLatitude(Double checkpointLatitude) {
		this.checkpointLatitude = checkpointLatitude;
	}

	public void setCheckpointLongitude(Double checkpointLongitude) {
		this.checkpointLongitude = checkpointLongitude;
	}

	public String getCurrentEvent() {
		return currentEvent;
	}

	public void setCurrentEvent(String currentEvent) {
		this.currentEvent = currentEvent;
	}

	public void setCheckpointBody1(String checkpointBody1) {
		this.checkpointBody1 = checkpointBody1;
	}

	public void setCheckpointBody2(String checkpointBody2) {
		this.checkpointBody2 = checkpointBody2;
	}

	public void setCheckpointEvent(String checkpointEvent) {
		this.checkpointEvent = checkpointEvent;
	}

	public void setCheckpoint1ntp(Long checkpoint1ntp) {
		this.checkpoint1ntp = checkpoint1ntp;
	}

	public void setCheckpoint2ntp(Long checkpoint2ntp) {
		this.checkpoint2ntp = checkpoint2ntp;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}


}