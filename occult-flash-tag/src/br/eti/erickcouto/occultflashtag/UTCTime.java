package br.eti.erickcouto.occultflashtag;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
 
public class UTCTime extends AsyncTask<Void, Void, Boolean> {
 
	private final SntpClient client = new SntpClient();
    private OnNtpTimeReceived listener;
    
    public UTCTime(OnNtpTimeReceived listener){
        this.listener = listener;
    }
	
    @Override
    protected Boolean doInBackground(Void... params) {
        client.requestTime("south-america.pool.ntp.org", 30000);
        return true;
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
    	super.onPostExecute(result);
    	Log.i("ntpTime", String.valueOf(client.getNtpTime()));
    	Log.i("ntpTimeReference", String.valueOf(client.getNtpTimeReference()));
    	Log.i("phoneTime", String.valueOf(System.currentTimeMillis()));
    	Log.i("elapse", String.valueOf(SystemClock.elapsedRealtime()));
    	
    	
   		listener.onNtpTimeReceived(client.getNtpTime(), client.getNtpTimeReference());
    }
    
}