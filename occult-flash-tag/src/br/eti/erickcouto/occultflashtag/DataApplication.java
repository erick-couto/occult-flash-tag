package br.eti.erickcouto.occultflashtag;

import java.util.Set;

import android.app.Application;

public class DataApplication extends Application {

	private Set<Long> checkpoints;
	private Set<Long> processedCheckpoints;
	
	private Long ntpTimeFromCurrentCheckpoint;
	private Long ntpTimeFromStart;
	private Long systemTimeFromStart;
	private Long systemTimeFromEnd;
	
	private Integer currentCheckpointNumber = 0;
	
	private Long ntpFirstCheckpoint;
	private Long ntpSecondCheckpoint;
	
	private Long timeForCheckpoint1;
	private Long timeForCheckpoint2;

	private Long cameraDelay = 0l;

	private Long timeControlUptimeStart;
	private Long timeControlCountdown;
	
	private Long startAudit;
	private Long endAudit;

	
	public Long getTimeForCheckpoint1() {
		return timeForCheckpoint1;
	}

	public void setTimeForCheckpoint1(Long timeForCheckpoint1) {
		this.timeForCheckpoint1 = timeForCheckpoint1;
	}

	public Long getTimeForCheckpoint2() {
		return timeForCheckpoint2;
	}

	public void setTimeForCheckpoint2(Long timeForCheckpoint2) {
		this.timeForCheckpoint2 = timeForCheckpoint2;
	}

	public Set<Long> getCheckpoints() {
		return checkpoints;
	}

	public void setCheckpoints(Set<Long> checkpoints) {
		this.checkpoints = checkpoints;
	}

	public Long getCameraDelay() {
		return cameraDelay;
	}

	public void setCameraDelay(Long cameraDelay) {
		this.cameraDelay = cameraDelay;
	}

	public Long getStartAudit() {
		return startAudit;
	}

	public void setStartAudit(Long startAudit) {
		this.startAudit = startAudit;
	}

	public Long getEndAudit() {
		return endAudit;
	}

	public void setEndAudit(Long endAudit) {
		this.endAudit = endAudit;
	}

	public Long getTimeControlUptimeStart() {
		return timeControlUptimeStart;
	}

	public void setTimeControlUptimeStart(Long time) {
		this.timeControlUptimeStart = time;
	}

	public Long getTimeControlCountdown() {
		return timeControlCountdown;
	}

	public void setTimeControlCountdown(Long time) {
		this.timeControlCountdown = time;
	}

	public Long getNtpTimeFromStart() {
		return ntpTimeFromStart;
	}

	public void setNtpTimeFromStart(Long time) {
		this.ntpTimeFromStart = time;
	}

	public Long getSystemTimeFromStart() {
		return systemTimeFromStart;
	}

	public void setSystemTimeFromStart(Long time) {
		this.systemTimeFromStart = time;
	}

	public Long getSystemTimeFromEnd() {
		return systemTimeFromEnd;
	}

	public void setSystemTimeFromEnd(Long time) {
		this.systemTimeFromEnd = time;
	}

	public Long getNtpTimeFromCurrentCheckpoint() {
		return ntpTimeFromCurrentCheckpoint;
	}

	public void setNtpTimeFromCurrentCheckpoint(Long time) {
		this.ntpTimeFromCurrentCheckpoint = time;
	}

	public Integer getCurrentCheckpointNumber() {
		return currentCheckpointNumber;
	}

	public void setCurrentCheckpointNumber(Integer number) {
		this.currentCheckpointNumber = number;
	}

	public Long getNtpFirstCheckpoint() {
		return ntpFirstCheckpoint;
	}

	public void setNtpFirstCheckpoint(Long ntpFirstCheckpoint) {
		this.ntpFirstCheckpoint = ntpFirstCheckpoint;
	}

	public Long getNtpSecondCheckpoint() {
		return ntpSecondCheckpoint;
	}

	public void setNtpSecondCheckpoint(Long ntpSecondCheckpoint) {
		this.ntpSecondCheckpoint = ntpSecondCheckpoint;
	}

	public Set<Long> getProcessedCheckpoints() {
		return processedCheckpoints;
	}

	public void setProcessedCheckpoints(Set<Long> processedCheckpoints) {
		this.processedCheckpoints = processedCheckpoints;
	}

	
	
	
}