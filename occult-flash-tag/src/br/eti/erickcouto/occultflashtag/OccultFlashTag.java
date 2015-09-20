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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class OccultFlashTag extends Activity implements OnNtpTimeReceived {

	public static final int FLASH_FPS = 10;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final String BREAK_LINE = "\n";
	
	private Handler timeControl;
	public CountDownTimer visualCountdown;
	private DataApplication appData;
	private SharedPreferences prefs;
	private LocationManager locationManager;
	private String locationProvider;
	private LocationListener locationListener;
	private Location bestLocation;
	
	public Set<Long> processedChecks = new HashSet<Long>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.occult_flash_tag);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean measured = prefs.getBoolean("measured", false);
        
        if(!measured){
        	hideBodySelector();
        	showFlashMeasurement();
        } else {
        	hideFlashMeasurement();
        	showBodySelector();
        	activateControls();
        }
        
		appData = (DataApplication) getApplication();

		locationProvider = LocationManager.GPS_PROVIDER;

		configureLocationGPS();
		locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
		
		RadioGroup rg = (RadioGroup) findViewById(R.id.rad_selector);
	    rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
	            public void onCheckedChanged(RadioGroup group, int checkedId) {
	                switch(checkedId){
	                    case R.id.rad_occult:
	                    	appData.setCurrentEvent("OC");
	                    break;
	                    case R.id.rad_eclipse:
	                        appData.setCurrentEvent("EC");
	                    break;
	                    case R.id.rad_transit:
	                        appData.setCurrentEvent("TR");
	                    break;
	                }
	            }
	    });
		
	}

	private void configureLocationGPS(){
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	if(isBetterLocation(location, bestLocation)){
			    	appData.setCurrentAltitude(location.getAltitude());
			    	appData.setCurrentLatitude(location.getLatitude());
			    	appData.setCurrentLongitude(location.getLongitude());
			    	
					TextView txtLatitude = ((TextView) findViewById(R.id.txt_latitude));
					txtLatitude.setText(String.valueOf(appData.getCurrentLatitude()));

					TextView txtLongitude = ((TextView) findViewById(R.id.txt_longitude));
					txtLongitude.setText(String.valueOf(appData.getCurrentLongitude()));
		    	}
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.occult_flash_tag_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_flash:
	            hideBodySelector();
	            showFlashMeasurement();
	            measure(findViewById(R.id.btn_flash_measurement));
	            return true;
	        case R.id.action_share:
	        	if(appData.getCheckpoint1ntp() == null || appData.getCheckpoint2ntp() == null){
	        		Context context = getApplicationContext();
	        		CharSequence text = getText(R.string.out_share_error_message);
	        		int duration = Toast.LENGTH_LONG;
	        		Toast toast = Toast.makeText(context, text, duration);
	        		toast.show();	
	        	} else {
	                Intent sendIntent = new Intent();
	                sendIntent.setAction(Intent.ACTION_SEND);
	                sendIntent.putExtra(Intent.EXTRA_TEXT, getFormattedResultsForShare());
	                sendIntent.setType("text/plain");
	                startActivity(sendIntent);
	        	}
	        	
	        	return true;
	        case R.id.action_settings:
	        	startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private String getFormattedResultsForShare(){
		StringBuffer sb = new StringBuffer(); 
		sb.append("Occult Flash Tag");
		sb.append(BREAK_LINE); 
		sb.append("==================================");
		sb.append(BREAK_LINE); 

		sb.append(getText(R.string.out_date) + ": ");
		sb.append(formatDateByMilliseconds(appData.getCheckpoint1ntp()));
		sb.append(BREAK_LINE); 
		sb.append(BREAK_LINE); 
		
		sb.append(appData.getCheckpointBody1());

		String event = appData.getCheckpointEvent();
		if(event != null && "OC".equals(event)){
			sb.append(" (" + getText(R.string.out_occult) + ") ");
		}
		else if(event != null && "EC".equals(event)){
			sb.append(" (" + getText(R.string.out_eclipse) + ") ");
		}
		else if(event != null && "TR".equals(event)){
			sb.append(" (" + getText(R.string.out_transit) + ") ");
		}

		sb.append(appData.getCheckpointBody2());
		sb.append(BREAK_LINE); 
		
		sb.append(getText(R.string.out_estimated_utc_1) + ": ");
		sb.append(formatTimeByMilliseconds(appData.getCheckpoint1ntp()));
		sb.append(BREAK_LINE); 

		sb.append(getText(R.string.out_estimated_utc_2) + ": ");
		sb.append(formatTimeByMilliseconds(appData.getCheckpoint2ntp()));
		sb.append(BREAK_LINE); 

		sb.append(getText(R.string.out_altitude) + ": ");
		sb.append(appData.getCheckpointAltitude());
		sb.append(BREAK_LINE); 

		sb.append(getText(R.string.out_latitude) + ": ");
		sb.append(appData.getCheckpointLatitude());
		sb.append(BREAK_LINE); 

		sb.append(getText(R.string.out_longitude) + ": ");
		sb.append(appData.getCheckpointLongitude());
		sb.append(BREAK_LINE); 

		return sb.toString();
		
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

		BtnStart.setClickable(false);
		BtnStart.setEnabled(false);
		BtnStop.setClickable(true);
		BtnStop.setEnabled(true);

		TextView txtUtc1Audited = ((TextView) findViewById(R.id.txt_utc1_audited));
		txtUtc1Audited.setText(getText(R.string.out_empty_timer));

		TextView txtUtc2Audited = ((TextView) findViewById(R.id.txt_utc2_audited));
		txtUtc2Audited.setText(getText(R.string.out_empty_timer));

		appData.setCheckpoint1ntp(null);
		appData.setCheckpoint2ntp(null);
		
		new UTCTime(this).execute();

	}

	public void showFlashMeasurement(){
		Button btnFlashMeasurement = ((Button) findViewById(R.id.btn_flash_measurement));
		btnFlashMeasurement.setVisibility(View.VISIBLE);
		btnFlashMeasurement.setClickable(true);
		btnFlashMeasurement.setEnabled(true);
		
		TextView tvStatusBar = ((TextView) findViewById(R.id.txt_status_bar));
		tvStatusBar.setVisibility(View.VISIBLE);
		tvStatusBar.setClickable(true);
		tvStatusBar.setEnabled(true);
	}

	public void hideFlashMeasurement(){
		Button btnFlashMeasurement = ((Button) findViewById(R.id.btn_flash_measurement));
		btnFlashMeasurement.setVisibility(View.GONE);
		btnFlashMeasurement.setClickable(false);
		btnFlashMeasurement.setEnabled(false);
		
		TextView tvStatusBar = ((TextView) findViewById(R.id.txt_status_bar));
		tvStatusBar.setVisibility(View.GONE);
		tvStatusBar.setClickable(false);
		tvStatusBar.setEnabled(false);
	}

	public void activateControls(){
    	Button btnEstimatedUtc1 = ((Button) findViewById(R.id.btn_estimated_utc1));
    	Button btnEstimatedUtc2 = ((Button) findViewById(R.id.btn_estimated_utc2));
    	btnEstimatedUtc1.setClickable(true);
    	btnEstimatedUtc1.setEnabled(true);
    	btnEstimatedUtc2.setClickable(true);
    	btnEstimatedUtc2.setEnabled(true);
	}
	
	public void showBodySelector(){
		LinearLayout layBody = ((LinearLayout) findViewById(R.id.lay_body));
		layBody.setVisibility(View.VISIBLE);
		layBody.setClickable(true);
		layBody.setEnabled(true);

		RadioGroup radSelector = ((RadioGroup) findViewById(R.id.rad_selector));
		radSelector.setVisibility(View.VISIBLE);
		radSelector.setClickable(true);
		radSelector.setEnabled(true);
	}

	public void hideBodySelector(){
		LinearLayout layBody = ((LinearLayout) findViewById(R.id.lay_body));
		layBody.setVisibility(View.GONE);
		layBody.setClickable(false);
		layBody.setEnabled(false);

		RadioGroup radSelector = ((RadioGroup) findViewById(R.id.rad_selector));
		radSelector.setVisibility(View.GONE);
		radSelector.setClickable(false);
		radSelector.setEnabled(false);
	}

	public void registerFlashMeasurement(){
        SharedPreferences.Editor ed = prefs.edit();
        ed.putBoolean("measured", true);
        ed.commit();
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
			
			appData.setCheckpointBody1(null);
			appData.setCheckpointBody2(null);
			appData.setCheckpointEvent(null);
			appData.setCheckpointAltitude(null);		
			appData.setCheckpointLatitude(null);
			appData.setCheckpointLongitude(null);
			appData.setCheckpoint1ntp(null);
			appData.setCheckpoint2ntp(null);
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
							
							//Update checkpoint 1 data
							TextView txtBody1 = ((TextView) findViewById(R.id.imp_body_1));
							appData.setCheckpointBody1(txtBody1.getText().toString());

							TextView txtBody2 = ((TextView) findViewById(R.id.imp_body_2));
							appData.setCheckpointBody2(txtBody2.getText().toString());
							
							appData.setCheckpointEvent(appData.getCurrentEvent());
							appData.setCheckpointAltitude(appData.getCurrentAltitude());
							appData.setCheckpointLatitude(appData.getCurrentLatitude());
							appData.setCheckpointLongitude(appData.getCurrentLongitude());
							appData.setCheckpoint1ntp(endNtp);
							
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
							
							//Update checkpoint 2 data
							if(appData.getCheckpointBody1() == null){
								TextView txtBody1 = ((TextView) findViewById(R.id.imp_body_1));
								appData.setCheckpointBody1(txtBody1.getText().toString());
							}
							
							if(appData.getCheckpointBody2() == null){
								TextView txtBody2 = ((TextView) findViewById(R.id.imp_body_2));
								appData.setCheckpointBody1(txtBody2.getText().toString());
							}
							
							appData.setCheckpointAltitude(appData.getCurrentAltitude());
							appData.setCheckpointLatitude(appData.getCurrentLatitude());
							appData.setCheckpointLongitude(appData.getCurrentLongitude());
							appData.setCheckpoint2ntp(endNtp);
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

	public String formatDateByMilliseconds(Long time) {
		time -= appData.getSystemTimeZoneDiff();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
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

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
}
