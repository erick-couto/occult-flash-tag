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
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import br.eti.erickcouto.occultflashtag.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class OccultFlashTag extends Activity {

	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final String BREAK_LINE = "\n";

	private CarregaDadosIniciaisAsyncTask carregaDadosAsyncTask = new CarregaDadosIniciaisAsyncTask();
	private ProgressDialog progressDialog;

	private Handler timeControl;
	public CountDownTimer visualCountdown;
	private DataApplication appData;
	private SharedPreferences prefs;
	private LocationManager locationManager;
	private String locationProvider;
	private LocationListener locationListener;
	private Location bestLocation;
	private String ntpServer;
	private String customNtpServer;
	private int marks;
	private int activeBootCounter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.occult_flash_tag);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

       	showBodySelector();
       	activateControls();

        String ntp = prefs.getString("ntp_server", null);
        if (ntp == null){
            SharedPreferences.Editor ed = prefs.edit();
            ed.putString("ntp_server", getText(R.string.out_prefs_ntp_server_default).toString());
            ed.commit();
        }
        ntpServer = prefs.getString("ntp_server", null);

		String repetitions = prefs.getString("repetitions", null);
		if (repetitions == null){
			SharedPreferences.Editor ed = prefs.edit();
			ed.putString("repetitions", getText(R.string.out_prefs_repetitions_default).toString());
			ed.commit();
		}
		marks = Integer.valueOf(prefs.getString("repetitions", null));

		String customNtp = prefs.getString("custom_ntp_server", null);
		if (customNtp == null){
			SharedPreferences.Editor ed = prefs.edit();
			ed.putString("custom_ntp_server", getText(R.string.out_prefs_custom_ntp_server_default).toString());
			ed.commit();
		}
		customNtpServer = prefs.getString("custom_ntp_server", null);

		Integer bootCount = prefs.getInt("boot_count", 0);
		if (bootCount == 0) {
			SharedPreferences.Editor ed = prefs.edit();
			ed.putInt("boot_count", bootCount);
			ed.commit();
		}
		activeBootCounter = Integer.valueOf(prefs.getInt("boot_count", 0));



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

		String versionName = "";

		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		TextView staturBar = (TextView) findViewById(R.id.txt_status_bar);
		staturBar.setText("Version " + versionName);

		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Mounting access data");
		progressDialog.setCancelable(false);
		progressDialog.show();

		carregaDadosAsyncTask.execute();

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
	        case R.id.action_list:
			    Intent intent = new Intent(this, EventActivity.class);
                startActivity(intent);
	        	return true;
	        case R.id.action_settings:
	        	startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
	            return true;
	        case R.id.action_about:
	        	showAbout();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
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

		SortedSet<Long> checkpoints = generateCheckpoints(new Date(), appData.getTimeForStart(), appData.getTimeForEnd(), marks);

		Event event = new Event(checkpoints);

		TextView txtBody1 = ((TextView) findViewById(R.id.imp_body_1));
		event.setBody1(txtBody1.getText().toString() != null && !txtBody1.getText().toString().trim().equals("") ? txtBody1.getText().toString() : "BODY 1");
		TextView txtBody2 = ((TextView) findViewById(R.id.imp_body_2));
		event.setBody2(txtBody2.getText().toString() != null && !txtBody2.getText().toString().trim().equals("") ? txtBody2.getText().toString() : "BODY 2");
		event.setType(appData.getCurrentEvent() != null ? appData.getCurrentEvent() : "OC");
		event.setStartDate(new Date());

		appData.setEvent(event);

		Button BtnStart = ((Button) findViewById(R.id.btn_start));
		Button BtnStop = ((Button) findViewById(R.id.btn_stop));

		BtnStart.setClickable(false);
		BtnStart.setEnabled(false);
		BtnStop.setClickable(true);
		BtnStop.setEnabled(true);

		addToTimeControl();

	}

	public void activateControls(){
    	ImageButton btnEstimatedUtc1 = ((ImageButton) findViewById(R.id.btn_estimated_utc1));
    	ImageButton btnEstimatedUtc2 = ((ImageButton) findViewById(R.id.btn_estimated_utc2));
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

	public void stop(View v) {
		if (visualCountdown != null)
			visualCountdown.cancel();
		clean(true);
	}

	private void clean(boolean full) {

		Button btnStart = ((Button) findViewById(R.id.btn_start));
		Button btnStop = ((Button) findViewById(R.id.btn_stop));

		if (appData.getTimeForStart() != null
				&& appData.getTimeForEnd() != null) {
			btnStart.setClickable(true);
			btnStart.setEnabled(true);
		}

		btnStop.setClickable(false);
		btnStop.setEnabled(false);

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

	public CountDownTimer createVisualCountdown(Long checkpointTime,
			Long currentTime) {
		visualCountdown = new CountDownTimer((checkpointTime - currentTime),
				(1000 / 25) ) {
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

	public void addToTimeControl() {
		timeControl = new Handler();
		long now = System.currentTimeMillis();
		long base = SystemClock.uptimeMillis();

		SortedSet<Long> checks = appData.getEvent().getCheckpoints();
		Iterator it = checks.iterator();
		Long firstCounter = null;

		while (it.hasNext()){
			Long current = (Long)it.next();
			timeControl.postAtTime(mRunnable, (current - now) + base);
			if(firstCounter == null) firstCounter = current;
		}

		visualCountdown = createVisualCountdown(firstCounter, now);
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
			try {
				synchronized (this) {

					Event event = appData.getEvent();

					Camera camera = Camera.open();
					if (camera != null) {
						Parameters p = camera.getParameters();
						p.setFlashMode(Parameters.FLASH_MODE_TORCH);
						camera.setParameters(p);
						event.addRegisteredTime(SystemClock.elapsedRealtime()); //Changed
						camera.startPreview();

						wait(1000);
						camera.stopPreview();
						camera.release();

						Long next = event.nextCheckpointTime();

						event.setAltitude(appData.getCurrentAltitude());
						event.setLatitude(appData.getCurrentLatitude());
						event.setLongitude(appData.getCurrentLongitude());

						if(next == null){
							TextView txtCountdown = ((TextView) findViewById(R.id.txt_countdown));
							txtCountdown.setText(getString(R.string.out_finished_upper));
							clean(false);

							DBAdapter db = new DBAdapter(OccultFlashTag.this);
							db.addEvent(event, activeBootCounter);

							Intent intent = new Intent(OccultFlashTag.this, NtpService.class);
							startService(intent);

						} else {
							visualCountdown.cancel();
							visualCountdown = createVisualCountdown(next, System.currentTimeMillis());
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

	
	private SortedSet<Long> generateCheckpoints(Date currentDate, Long startTime, Long endTime, int marks) {

		SortedSet<Long> checkpoints = new TreeSet<>();

		final GregorianCalendar gc = new GregorianCalendar();
		TimeZone tz = gc.getTimeZone();
		Long timeZoneDiff = (long) tz.getOffset(currentDate.getTime());

		gc.setTime(currentDate);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);

		Long base = gc.getTimeInMillis();

		if(base + startTime + timeZoneDiff < currentDate.getTime()){ //Significa que o start é amanhã
			gc.add(Calendar.DATE, 1);
			base = gc.getTimeInMillis();
		}

		long timeAddedInMillis = 0;
		long maiorValor = 0;

		for(int i=1; i<= marks; i++){
			maiorValor = base + startTime + timeZoneDiff + timeAddedInMillis;
			checkpoints.add(maiorValor);
			timeAddedInMillis += 2000;
		}

		if(endTime != null){
			timeAddedInMillis = 0;
			if(base + endTime + timeZoneDiff <= maiorValor){
				gc.add(Calendar.DATE, 1);
				base = gc.getTimeInMillis();
			}

			for(int i=1; i<= marks; i++){
				maiorValor = base + endTime + timeZoneDiff + timeAddedInMillis;
				checkpoints.add(maiorValor);
				timeAddedInMillis += 2000;
			}
		}

		return checkpoints;

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

    protected void showAbout() {
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);
        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);
 
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);

		String versionName = "";

		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		builder.setTitle("Occult Flash Tag " + versionName);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

	private class CarregaDadosIniciaisAsyncTask extends AsyncTask<Void, Integer, Void> {

		protected void onPreExecute() {
			progressDialog.setProgress(0);
		}

		protected Void doInBackground(Void... progress) {

			DBAdapter db = new DBAdapter(OccultFlashTag.this);

			SharedPreferences.Editor ed = prefs.edit();
			Float version = prefs.getFloat("version", 0);

			if (version == null) version = 0.00f;

			Long versionCode = 0l;

			try {
				versionCode = Long.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}


			if (version < versionCode) {

				try {
					db.open();
					publishProgress(75);
					db.close();
					ed.putFloat("version", versionCode);
					publishProgress(100);
					ed.commit();

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			progressDialog.setProgress(progress[0]);
		}

		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
		}
	}


}
