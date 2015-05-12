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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import br.eti.erickcouto.occultflashtag.R;
import android.app.Activity;
import android.app.DialogFragment;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OccultFlashTag extends Activity implements OnNtpTimeReceived {

	public static final int FLASH_FPS = 10;

	private Handler timeControl;
	public CountDownTimer visualCountdown;
	private DataApplication appData;

	public Set<Long> processedChecks = new HashSet<Long>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.occult_flash_tag);

		appData = (DataApplication) getApplication();

	}

	public void showUTC1Dialog(View v) {
		DialogFragment newFragment = new TimePickerFragment(true);
		newFragment.show(this.getFragmentManager(), "UTC1");
	}

	public void showUTC2Dialog(View v) {
		DialogFragment newFragment = new TimePickerFragment(false);
		newFragment.show(this.getFragmentManager(), "UTC2");
	}

	public void start(View v) {

		appData.setCheckpoints(generateCheckpoints(new Date()));

		Button BtnStart = ((Button) findViewById(R.id.btn_start));
		Button BtnStop = ((Button) findViewById(R.id.btn_stop));
		Button BtnFlashMeasurement = ((Button) findViewById(R.id.btn_flash_measurement));

		BtnStart.setClickable(false);
		BtnStart.setEnabled(false);
		BtnStop.setClickable(true);
		BtnStop.setEnabled(true);
		BtnFlashMeasurement.setClickable(false);
		BtnFlashMeasurement.setEnabled(false);

		TextView txtUtc1Audited = ((TextView) findViewById(R.id.txt_utc1_audited));
		txtUtc1Audited.setText(getText(R.string.out_empty_timer));

		TextView txtUtc2Audited = ((TextView) findViewById(R.id.txt_utc2_audited));
		txtUtc2Audited.setText(getText(R.string.out_empty_timer));

		new UTCTime(this).execute();

	}

	public void measure(View v) {
		Button BtnFlashMeasurement = ((Button) findViewById(R.id.btn_flash_measurement));
		BtnFlashMeasurement.setClickable(false);
		BtnFlashMeasurement.setEnabled(false);
		new MeasurementThread().execute(this);
	}

	public void stop(View v) {

		if (visualCountdown != null)
			visualCountdown.cancel();
		clean(true);

	}

	private void clean(boolean full) {

		Button btnStart = ((Button) findViewById(R.id.btn_start));
		Button btnStop = ((Button) findViewById(R.id.btn_stop));
		Button btnFlashMeasurement = ((Button) findViewById(R.id.btn_flash_measurement));

		if (appData.getTimeForCheckpoint1() != null
				&& appData.getTimeForCheckpoint2() != null) {
			btnStart.setClickable(true);
			btnStart.setEnabled(true);
		}

		btnStop.setClickable(false);
		btnStop.setEnabled(false);

		btnFlashMeasurement.setClickable(true);
		btnFlashMeasurement.setEnabled(true);

		appData.setCurrentCheckpointNumber(0);
		appData.setNtpFirstCheckpoint(null);
		appData.setNtpSecondCheckpoint(null);
		appData.setNtpTimeFromCurrentCheckpoint(null);
		appData.setNtpTimeFromStart(null);
		appData.setSystemTimeFromStart(null);
		appData.setSystemTimeFromEnd(null);
		appData.setTimeControlUptimeStart(null);
		appData.setTimeControlCountdown(null);

		if (full) {
			TextView txtCountdown = ((TextView) findViewById(R.id.txt_countdown));
			txtCountdown.setText(getString(R.string.out_empty_timer));
			TextView txtUtc1Audited = ((TextView) findViewById(R.id.txt_utc1_audited));
			txtUtc1Audited.setText(getString(R.string.out_empty_timer));
			TextView txtUtc2Audited = ((TextView) findViewById(R.id.txt_utc2_audited));
			txtUtc2Audited.setText(getString(R.string.out_empty_timer));
		}

	}

	@Override
	public void onNtpTimeReceived(Long ntpTime, Long ntpReference) {
		ntpTime += SystemClock.elapsedRealtime() - ntpReference;

		final Long startTimeMillis = SystemClock.uptimeMillis();
		appData.setSystemTimeFromStart(startTimeMillis);
		appData.setNtpTimeFromStart(ntpTime);

		Long currentCheckpoint = getCurrentCheckpoint(ntpTime);

		appData.setNtpTimeFromCurrentCheckpoint(currentCheckpoint);
		appData.setCurrentCheckpointNumber(appData.getCurrentCheckpointNumber() + 1);

		ntpTime += (SystemClock.uptimeMillis() - startTimeMillis);

		Long millisToCheckpoint = currentCheckpoint - ntpTime
				- appData.getCameraDelay();

		appData.setTimeControlCountdown(millisToCheckpoint);
		appData.setTimeControlUptimeStart(SystemClock.uptimeMillis());

		addToTimeControl(appData.getTimeControlUptimeStart()
				+ millisToCheckpoint); 

		visualCountdown = createVisualCountdown(currentCheckpoint, ntpTime);
	}

	public CountDownTimer createVisualCountdown(Long checkpointTime,
			Long currentTime) {
		visualCountdown = new CountDownTimer((checkpointTime - currentTime),
				1000 / FLASH_FPS) {
			TextView txtCountdown = ((TextView) findViewById(R.id.txt_countdown));

			public void onTick(long millisUntilFinished) {
				long hourTmp = millisUntilFinished / 3600000;
				String hour = (String) ((hourTmp < 10) ? "0" + hourTmp : String
						.valueOf(hourTmp));
				long hourModule = millisUntilFinished % 3600000;
				long minuteTmp = hourModule / 60000;
				String minute = (String) ((minuteTmp < 10) ? "0" + minuteTmp
						: String.valueOf(minuteTmp));
				long minuteModule = hourModule % 60000;
				long secondTmp = minuteModule / 1000;
				String second = (String) ((secondTmp < 10) ? "0" + secondTmp
						: String.valueOf(secondTmp));
				long secondModule = minuteModule % 1000;
				String millisecond = (String) ((secondModule < 10) ? "00"
						+ secondModule : (secondModule < 100) ? "0"
						+ secondModule : String.valueOf(secondModule));
				txtCountdown.setText(hour + ":" + minute + ":" + second + "."
						+ millisecond);
			}

			public void onFinish() {
			}

		}.start();

		return visualCountdown;
	}

	public void addToTimeControl(Long time) {
		timeControl = new Handler();
		timeControl.postAtTime(mRunnable, time);
	}

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			thread.run();
		}

	};

	private Thread thread = new Thread() {
		@Override
		@SuppressWarnings("deprecation")
		public void run() {

			Long nextStart;

			try {
				synchronized (this) {

					Camera camera = Camera.open();
					if (camera != null) {
						Parameters p = camera.getParameters();
						p.setFlashMode(Parameters.FLASH_MODE_TORCH);
						camera.setParameters(p);
						camera.startPreview();
						appData.setSystemTimeFromEnd(SystemClock.uptimeMillis());
						nextStart = SystemClock.uptimeMillis();
						wait(1000 / 25);
						camera.stopPreview();
						camera.release();

						Set<Long> processed = appData.getProcessedCheckpoints();
						if (processed == null)
							processed = new HashSet<Long>();

						processed
								.add(appData.getNtpTimeFromCurrentCheckpoint());
						appData.setProcessedCheckpoints(processed);

						Long endNtp = appData.getSystemTimeFromEnd()
								+ (appData.getNtpTimeFromStart() - appData
										.getSystemTimeFromStart());

						if (appData.getCurrentCheckpointNumber().equals(1)) {
							TextView txtUtc1Audited = ((TextView) findViewById(R.id.txt_utc1_audited));
							txtUtc1Audited
									.setText(formatTimeByMilliseconds(endNtp));
							appData.setNtpFirstCheckpoint(endNtp);
						} else if (appData.getCurrentCheckpointNumber().equals(
								2)) {
							TextView txtUtc2Audited = ((TextView) findViewById(R.id.txt_utc2_audited));
							txtUtc2Audited
									.setText(formatTimeByMilliseconds(endNtp));
							appData.setNtpSecondCheckpoint(endNtp);
							TextView txtCountdown = ((TextView) findViewById(R.id.txt_countdown));
							txtCountdown
									.setText(getString(R.string.out_finished_upper));
							clean(false);
						}

						if (appData.getCurrentCheckpointNumber().equals(1)) {
							Long nextAdd = SystemClock.uptimeMillis()
									- nextStart;

							final Long startTimeMillis = SystemClock
									.uptimeMillis();
							appData.setSystemTimeFromStart(startTimeMillis);

							appData.setNtpTimeFromStart(endNtp + nextAdd);
							Long currentCheckpoint = getCurrentCheckpoint(endNtp);
							appData.setNtpTimeFromCurrentCheckpoint(currentCheckpoint);
							appData.setCurrentCheckpointNumber(appData
									.getCurrentCheckpointNumber() + 1);

							Long millisToCheckpoint = currentCheckpoint
									- (endNtp + nextAdd)
									- appData.getCameraDelay();

							appData.setTimeControlCountdown(millisToCheckpoint);
							appData.setTimeControlUptimeStart(SystemClock
									.uptimeMillis());

							addToTimeControl(appData
									.getTimeControlUptimeStart()
									+ millisToCheckpoint);
							visualCountdown = createVisualCountdown(
									currentCheckpoint, (endNtp + nextAdd));

						}

					} else {
						Toast.makeText(OccultFlashTag.this,
								"flash not available", Toast.LENGTH_LONG)
								.show();
					}
				}
			} catch (InterruptedException ex) {
				Log.e("camera", ex.getMessage());
				Toast.makeText(OccultFlashTag.this, ex.getMessage(),
						Toast.LENGTH_LONG).show();
			}

		}
	};

	public String formatTimeByMilliseconds(Long time) {
		time -= appData.getSystemTimeZoneDiff();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		return sdf.format(new Date(time));
	}

	private Set<Long> generateCheckpoints(Date currentDate) {

		Set<Long> checkpoints = new HashSet<Long>();

		final GregorianCalendar gc = new GregorianCalendar();
		TimeZone tz = gc.getTimeZone();
		Long timeZoneDiff = (long) tz.getOffset(currentDate.getTime());
		appData.setSystemTimeZoneDiff(timeZoneDiff);

		gc.setTime(currentDate);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);

		final Long today = gc.getTimeInMillis();
		gc.add(Calendar.DATE, -1);
		final Long yesterday = gc.getTimeInMillis();
		gc.add(Calendar.DATE, 2);
		final Long tomorrow = gc.getTimeInMillis();

		checkpoints.add(yesterday + appData.getTimeForCheckpoint1()
				+ timeZoneDiff);
		checkpoints.add(yesterday + appData.getTimeForCheckpoint2()
				+ timeZoneDiff);
		checkpoints.add(today + appData.getTimeForCheckpoint1() + timeZoneDiff);
		checkpoints.add(today + appData.getTimeForCheckpoint2() + timeZoneDiff);
		checkpoints.add(tomorrow + appData.getTimeForCheckpoint1()
				+ timeZoneDiff);
		checkpoints.add(tomorrow + appData.getTimeForCheckpoint2()
				+ timeZoneDiff);

		return checkpoints;

	}

	public Long getCurrentCheckpoint(Long time) {
		Set<Long> checkpoints = appData.getCheckpoints();
		final Set<Long> checkpointsProcessed = appData
				.getProcessedCheckpoints();

		if (checkpointsProcessed != null && checkpointsProcessed.size() > 0)
			checkpoints.removeAll(checkpointsProcessed);

		Long activeCheckpoint = null;
		for (Long checkpoint : checkpoints) {
			if (activeCheckpoint == null
					&& checkpoint > time
					|| (activeCheckpoint != null && checkpoint > time && checkpoint < activeCheckpoint)) {
				activeCheckpoint = checkpoint;
			}
		}

		return activeCheckpoint;
	}

	public DataApplication getAppData() {
		return appData;
	}

	public void setAppData(DataApplication appData) {
		this.appData = appData;
	}

}
