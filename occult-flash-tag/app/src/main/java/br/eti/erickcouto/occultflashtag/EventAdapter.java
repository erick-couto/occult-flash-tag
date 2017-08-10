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

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    Context context;
    private List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    @Override
    public void onBindViewHolder(EventViewHolder eventViewHolder, int i) {
        final Event ci = eventList.get(i);

        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");
        String date = sdf.format(ci.getStartDate());
        eventViewHolder.vDate.setText(date);

        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        String timeTag = sdf2.format(ci.getStartDate());
        eventViewHolder.vTimeTag.setText(timeTag);

        String type = (ci.getType().equals("OC") ? "OCCULT" : ci.getType().equals("EC") ? "ECLIPSE" : ci.getType().equals("TR") ? "TRANSIT" : "");
        eventViewHolder.vEvent.setText(ci.getBody1() + " " + type + " " + ci.getBody2());

        if(ci.getSynced().equals(Boolean.FALSE)){
            eventViewHolder.vSynced.setBackground(null);
        }

        if(ci.getStatus() != null){
            eventViewHolder.vStatus.setText(ci.getStatus().toUpperCase());
        } else {
            eventViewHolder.vStatus.setVisibility(View.INVISIBLE);
        }

        eventViewHolder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventAdapter.this.context, DetailsActivity.class);
                intent.putExtra("ID_EVENT", ci.getEventId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout_events, viewGroup, false);
        return new EventViewHolder(itemView);
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        protected TextView vDate;
        protected TextView vTimeTag;
        protected TextView vEvent;
        protected TextView vStatus;
        protected ImageView vSynced;
        protected LinearLayout details;

        public EventViewHolder(View v) {
            super(v);
            vDate =  (TextView) v.findViewById(R.id.date);
            vTimeTag = (TextView) v.findViewById(R.id.time_tag);
            vEvent = (TextView) v.findViewById(R.id.event);
            vStatus = (TextView) v.findViewById(R.id.status_tag);
            vSynced = (ImageView) v.findViewById(R.id.synced_tag);
            details = (LinearLayout) v.findViewById(R.id.rel_data);
        }

    }
}