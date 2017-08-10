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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EventActivity extends Activity {

    private ProgressDialog progressDialog;
    private EventAdapter mAdapter;
    private RecyclerView recList;
    private PopulaAsyncTask populaAsyncTask = new PopulaAsyncTask();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading events");
        progressDialog.setCancelable(false);
        progressDialog.show();

        setContentView(R.layout.activity_event);

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        IntentFilter intentFilter = new IntentFilter("br.eti.erickcouto.occultflashtag.statuschange");
        registerReceiver(messageReceiver, intentFilter);

        populaAsyncTask.execute();

    }


    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getString("message") != null) {
                if (intent.getExtras().getString("message").equals("statuschange")) {

                    DBAdapter db = new DBAdapter(EventActivity.this);
                    List<Event> events = db.getAllEvents();
                    mAdapter = new EventAdapter(EventActivity.this, events);
                    recList.setAdapter(mAdapter);

                    TextView tx = (TextView) findViewById(R.id.txt_without_events);

                    if(events.size() == 0){
                        tx.setVisibility(View.VISIBLE);
                    } else {
                        tx.setVisibility(View.GONE);
                    }

                }
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_event_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_audit:
                Intent intent = new Intent(this, NtpService.class);
                startService(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onResume(){
        super.onResume();

        DBAdapter db = new DBAdapter(EventActivity.this);
        List<Event> events = db.getAllEvents();
        mAdapter = new EventAdapter(EventActivity.this, events);
        recList.setAdapter(mAdapter);

        TextView tx = (TextView) findViewById(R.id.txt_without_events);

        if(events.size() == 0){
            tx.setVisibility(View.VISIBLE);
        } else {
            tx.setVisibility(View.GONE);
        }

    }

    private class PopulaAsyncTask extends AsyncTask<Void, Integer, ArrayList<Event>> {

        protected void onPreExecute() {
            progressDialog.setProgress(0);
        }

        protected ArrayList<Event> doInBackground(Void...progress ) {
            DBAdapter db = new DBAdapter(EventActivity.this);
            return (ArrayList<Event>) db.getAllEvents();
        }

        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(ArrayList<Event> result) {
            mAdapter = new EventAdapter(EventActivity.this, result);
            recList.setAdapter(mAdapter);

            TextView tx = (TextView) findViewById(R.id.txt_without_events);

            if(result.size() == 0){
                tx.setVisibility(View.VISIBLE);
            } else {
                tx.setVisibility(View.GONE);
            }

            progressDialog.dismiss();
        }
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