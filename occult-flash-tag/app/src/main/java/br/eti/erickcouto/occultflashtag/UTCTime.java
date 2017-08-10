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

import android.os.AsyncTask;

public class UTCTime extends AsyncTask<Void, Void, Boolean> {

	private final SntpClient client = new SntpClient();
	private OnNtpTimeReceived listener;
	private ENtpServer ntpServer;
	private Event event;
	private long now;

	public UTCTime(OnNtpTimeReceived listener, String serverCode, Event event) {
		this.listener = listener;
		ntpServer = ENtpServer.getServerByCode(serverCode);
		this.event = event;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		int x = 0;

		while (x < 4) {
			if (client.requestTime(ntpServer.getServer(), 6000)) {
				now = client.getNtpTime();
				break;
			} else {
				x++;
			}
		}

		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		listener.onNtpTimeReceived(client.getNtpTime(),
				client.getNtpTimeReference());
	}

	
}