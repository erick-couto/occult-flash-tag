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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import br.eti.erickcouto.occultflashtag.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {

	boolean start;

	public TimePickerFragment()	{
	}


	@SuppressLint("ValidFragment")
	public TimePickerFragment(boolean start) {
		this.start = start;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		return new TimePickerDialog(getActivity(),TimePickerDialog.THEME_DEVICE_DEFAULT_DARK, this, hour, minute, true);
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		DataApplication data = ((DataApplication) getActivity()
				.getApplication());
		if (start) {
			Long ini = (hourOfDay * 3600000L) + (minute * 60000L);
			data.setTimeForStart(ini);
			((TextView) getActivity().findViewById(R.id.txt_estimated_utc1))
					.setText(hourFormat(ini));

		} else {
			Long end = (hourOfDay * 3600000L) + (minute * 60000L);
			data.setTimeForEnd(end);
			((TextView) getActivity().findViewById(R.id.txt_estimated_utc2))
					.setText(hourFormat(end));
		}

		if (data.getTimeForStart() != null) {
			Button btn_start = ((Button) getActivity().findViewById(R.id.btn_start));
			btn_start.setEnabled(true);
			btn_start.setClickable(true);
		} else {

		}
	}

	private String hourFormat(Long millis) {

		long hour = millis / 3600000;
		long hourModule = millis % 3600000;
		long minutes = hourModule / 60000;

		return hour + ":" + minutes + ":00";
	}

}