package br.eti.erickcouto.occultflashtag;

/*
 * Copyright (C) 2017 Erick Couto
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DetailsActivity extends Activity {

    private ProgressDialog progressDialog;
    private Long eventId = null;
    private List<Mark> marks = new ArrayList<Mark>();
    private Switch switchAB;
    private PopulaAsyncTask populaAsyncTask = new PopulaAsyncTask();
    private static final String BREAK_LINE = "\n";
    private StringBuffer shareText = new StringBuffer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        eventId = intent.getLongExtra("ID_EVENT", 0);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Reading details");
        progressDialog.setCancelable(false);
        progressDialog.show();

        setContentView(R.layout.activity_details);

        populaAsyncTask.execute();

        shareText.append("Occult Flash Tag").append(BREAK_LINE);
        shareText.append("================").append(BREAK_LINE).append(BREAK_LINE);

    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_detail_menu, menu);

        final DBAdapter da = new DBAdapter(this);
        Event e = da.getEvent(eventId.toString());

        if(e.getSynced()){

            if(e.getStatus() == null || (e.getStatus() != null && !e.getStatus().equals("ER")) ) {
                switchAB = (Switch) menu.findItem(R.id.switchId).getActionView().findViewById(R.id.switchAB);
                switchAB.setChecked(e.getStatus() == null ? false : e.getStatus().equals("PR") ? true : false);

                switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            da.markEventAsProduction(eventId);
                        } else {
                            da.markEventAsDefault(eventId);
                        }

                        Event e = da.getEvent(eventId.toString());
                        updateScreen(e);
                    }
                });
            }

        } else {
            switchAB = (Switch)menu.findItem(R.id.switchId).getActionView().findViewById(R.id.switchAB);
            switchAB.setVisibility(View.GONE);
        }


        return super.onCreateOptionsMenu(menu);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.event_delete:
                AlertDialog diaBox = AskOption();
                diaBox.show();
                return true;
            case R.id.event_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Occult Flash Tag - Event");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText.toString());
                startActivity(Intent.createChooser(sharingIntent, "Occult Flash Tag"));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Delete")
                .setMessage("Do you want to continue? \nThis operation cannot be undone and will lost all event marks!")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        DBAdapter db = new DBAdapter(DetailsActivity.this);
                        db.deleteEvent(eventId);
                        dialog.dismiss();
                        finish();

                    }

                })

                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        return myQuittingDialogBox;

    }


    private class PopulaAsyncTask extends AsyncTask<Void, Integer, Event> {

        protected void onPreExecute() {
            progressDialog.setProgress(0);
        }

        protected Event doInBackground(Void...progress ) {
            DBAdapter db = new DBAdapter(DetailsActivity.this);
            marks = db.getAllMarksByEvent(eventId);
            return (Event) db.getEvent(eventId.toString());
        }

        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        protected void onPostExecute(Event result) {
            updateScreen(result);
            progressDialog.dismiss();
        }
    }

    private void updateScreen(Event result){
        TextView tx = (TextView) findViewById(R.id.details_body1);

        String type = (result.getType().equals("OC") ? "OCCULT" : result.getType().equals("EC") ? "ECLIPSE" : result.getType().equals("TR") ? "TRANSIT" : "");
        tx.setText(result.getBody1() + " " + type + " " + result.getBody2());
        shareText.append(result.getBody1() + " " + type + " " + result.getBody2()).append(BREAK_LINE);

        TextView tx2 = (TextView) findViewById(R.id.details_datetime);
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");
        String date = sdf.format(result.getStartDate());
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        String timeTag = sdf2.format(result.getStartDate());

        tx2.setText(date + "  |  " + timeTag);
        shareText.append(date + "  |  " + timeTag).append(BREAK_LINE).append(BREAK_LINE);

        TextView tx3 = (TextView) findViewById(R.id.details_status);

        if (result.getStatus() != null) {
            tx3.setVisibility(View.VISIBLE);
            String status = (result.getStatus().equals("PR") ? "PRODUCTION USED" : result.getStatus().equals("ER") ? "CANCELED BY REBOOT" : "");
            tx3.setText(status);
            shareText.append(status).append(BREAK_LINE).append(BREAK_LINE);
        } else {
            tx3.setVisibility(View.INVISIBLE);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        TableLayout tl = (TableLayout) findViewById(R.id.tableLayout);
        tl.removeAllViews();

        TableRow rowTop = new TableRow(DetailsActivity.this);
        TableRow.LayoutParams lp2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        rowTop.setLayoutParams(lp2);

        TextView tv1Top = new TextView(DetailsActivity.this);
        tv1Top.setTextColor(Color.RED);
        tv1Top.setText("Mark");
        tv1Top.setTextSize(12);
        tv1Top.setPadding(0,0,50,0);
        tv1Top.setTypeface(Typeface.DEFAULT_BOLD);

        rowTop.addView(tv1Top);

        TextView tv2Top = new TextView(DetailsActivity.this);
        tv2Top.setTextColor(Color.RED);
        tv2Top.setText("Audited UTC Time");
        tv2Top.setTextSize(12);
        tv2Top.setTypeface(Typeface.DEFAULT_BOLD);

        rowTop.addView(tv2Top);

        tl.addView(rowTop);

        shareText.append("Mark / Audited UTC Time").append(BREAK_LINE).append("-----------------------").append(BREAK_LINE);

        int x = 0;
        for (Mark mark : marks) {
            x++;

            TableRow row = new TableRow(DetailsActivity.this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);

            TextView tv1 = new TextView(DetailsActivity.this);
            tv1.setTextColor(Color.RED);
            tv1.setText(String.valueOf(x));
            tv1.setTextSize(16);
            tv1.setTypeface(Typeface.MONOSPACE);

            row.addView(tv1);

            shareText.append(x).append("->");

            TextView tv2 = new TextView(DetailsActivity.this);
            tv2.setTextColor(Color.RED);
            if(mark.getAuditedTime() == null || mark.getAuditedTime() == 0l){
                tv2.setText("---");
                shareText.append("---").append(BREAK_LINE);
            } else {
                tv2.setText(formatter.format(new Date(mark.getAuditedTime())));
                shareText.append(formatter.format(new Date(mark.getAuditedTime()))).append(BREAK_LINE);
            }
            tv2.setTextSize(16);
            tv2.setTypeface(Typeface.MONOSPACE);

            row.addView(tv2);

            tl.addView(row);
        }

        shareText.append(BREAK_LINE);

        TextView tvLatitude = (TextView) findViewById(R.id.table_latitude_value);
        tvLatitude.setText(result.getLatitude().toString());
        shareText.append("Latitude: ").append(result.getLatitude().toString()).append(BREAK_LINE);

        TextView tvLongitude = (TextView) findViewById(R.id.table_longitude_value);
        tvLongitude.setText(result.getLongitude().toString());
        shareText.append("Longitude: ").append(result.getLongitude().toString()).append(BREAK_LINE);

        TextView tvAltitude = (TextView) findViewById(R.id.table_altitude_value);
        tvAltitude.setText(result.getAltitude().toString());
        shareText.append("Altitude: ").append(result.getAltitude().toString()).append(BREAK_LINE);

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}