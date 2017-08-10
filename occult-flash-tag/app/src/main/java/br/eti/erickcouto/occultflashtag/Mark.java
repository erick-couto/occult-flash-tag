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

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

public class Mark {

	private Long id;
	private Long eventId;
	private Long elapsedTime;
	private Long auditedTime;
	private Integer bootCounter;

	public Mark(Long id) {
		this.id = id;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public Long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(Long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public Long getAuditedTime() {
		return auditedTime;
	}

	public void setAuditedTime(Long auditedTime) {
		this.auditedTime = auditedTime;
	}

	public Integer getBootCounter() { return bootCounter; }

	public void setBootCounter(Integer bootCounter) { this.bootCounter = bootCounter; }

	public Long getId() { return id; }

}