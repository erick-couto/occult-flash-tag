package br.eti.erickcouto.occultflashtag;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NtpService extends IntentService {

    private SharedPreferences prefs;
    private ENtpServer ntpServer = null;
    private String customNtpServer;
    private final SntpClient client = new SntpClient();

    private Set<Long> ntpValues = new HashSet<Long>();

    public NtpService() {
        super("NtpService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()){
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int activeBootCounter = Integer.valueOf(prefs.getInt("boot_count", 0));

            String prefsServer = prefs.getString("ntp_server", null);
            customNtpServer = prefs.getString("custom_ntp_server", null);

            if(prefsServer != null && !prefsServer.equals("")){
                ntpServer = ENtpServer.getServerByCode(prefsServer);
            }

            int accuracy = Integer.valueOf(prefs.getString("accuracy", null));

            DBAdapter db = new DBAdapter(this);
            List<Mark> values = db.getAllUnauditedsMarks(activeBootCounter);

            String server = (ntpServer == null) ? customNtpServer : ntpServer.getServer();

            for(int i = 0; i < accuracy; i++) {

                if(client.requestTime(server, 6000)) {
                    long elapsed = client.getNtpTimeReference();
                    long ntpTime = client.getNtpTime();
                    Log.i("OFT", "NTPTIME = " + ntpTime + " , REFERENCE = " + elapsed + " , AVERAGE = " + (ntpTime - elapsed) );

                    //NTPTIME - ELAPSED
                    long result = ntpTime - elapsed;
                    ntpValues.add(result);

                    Intent verifyIntent=new Intent("br.eti.erickcouto.occultflashtag.statuschange");
                    verifyIntent.putExtra("message", "updating");
                    NtpService.this.sendBroadcast(verifyIntent);


                    try {
                        Thread.sleep(4000l);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            long average = calculateAverage(ntpValues);

            Log.i("OFT", "FINAL AVERAGE: " + average);

            for (Mark mark: values) {
                db.storeAuditedTime(mark.getId(), average + mark.getElapsedTime());
                db.markEventAsSynced(mark.getEventId());
            }

            //Update rebooted marks to canceled
            List<Mark> errors = db.getAllErrorMarks(activeBootCounter);

            for (Mark mark: errors) {
                db.markEventAsError(mark.getEventId());
            }

            Intent verifyIntent=new Intent("br.eti.erickcouto.occultflashtag.statuschange");
            verifyIntent.putExtra("message", "statuschange");
            NtpService.this.sendBroadcast(verifyIntent);


            db.close();

        }
    }

    private long calculateAverage(Set <Long> marks) {
        Long sum = 0l;
        if(!marks.isEmpty()) {

            int loops = (marks.size() < 5) ? 0 : marks.size()/5;

            Log.i("OFT", "Loops: " + loops);

            for(int i = 0; i < loops; i++) {
                Log.i("OFT", "Removed : " + Collections.min(marks));
                Log.i("OFT", "Removed : " + Collections.max(marks));
                marks.remove(Collections.min(marks));
                marks.remove(Collections.max(marks));
            }

            for (Long mark : marks) {
                sum += mark;
            }

            return sum / marks.size();
        }
        return sum;
    }

}