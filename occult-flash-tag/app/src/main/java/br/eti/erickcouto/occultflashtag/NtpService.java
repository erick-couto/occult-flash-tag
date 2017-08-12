package br.eti.erickcouto.occultflashtag;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

            if(client.requestTime((ntpServer == null) ? customNtpServer : ntpServer.getServer(), 6000)){
                long now = client.getNtpTime();
                long reference = client.getNtpTimeReference();

                DBAdapter db = new DBAdapter(this);

                Long addValue = now - reference;


                //Update all pendind sync marks
                List<Mark> values = db.getAllUnauditedsMarks(activeBootCounter);

                for (Mark mark: values) {
                    long actual = client.getNtpTime() + mark.getElapsedTime() - client.getNtpTimeReference();

                    db.storeAuditedTime(mark.getId(), actual);
                    db.markEventAsSynced(mark.getEventId());
                }

                //Update rebooted marks to canceled
                List<Mark> errors = db.getAllErrorMarks(activeBootCounter);

                for (Mark mark: errors) {
                    db.markEventAsError(mark.getEventId());
                }

                db.close();

                Intent verifyIntent=new Intent("br.eti.erickcouto.occultflashtag.statuschange");
                verifyIntent.putExtra("message", "statuschange");
                NtpService.this.sendBroadcast(verifyIntent);

            }
        }
    }


}