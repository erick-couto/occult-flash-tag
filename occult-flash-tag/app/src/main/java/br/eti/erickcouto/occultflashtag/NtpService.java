package br.eti.erickcouto.occultflashtag;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.util.List;

public class NtpService extends IntentService {

    private SharedPreferences prefs;
    private ENtpServer ntpServer = null;
    private String customNtpServer;
    private final SntpClient client = new SntpClient();

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

            DBAdapter db = new DBAdapter(this);
            List<Mark> values = db.getAllUnauditedsMarks(activeBootCounter);

            if(client.requestTime((ntpServer == null) ? customNtpServer : ntpServer.getServer(), 6000)){
                Long elapsed = SystemClock.elapsedRealtime();
                Long ntpTime = client.getNtpTime();

                for (Mark mark: values) {
                    Long difference = elapsed - mark.getElapsedTime();
                    db.storeAuditedTime(mark.getId(), ntpTime - difference);
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

            }

            db.close();

        }
    }


}