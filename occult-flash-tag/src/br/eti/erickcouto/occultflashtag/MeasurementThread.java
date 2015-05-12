package br.eti.erickcouto.occultflashtag;

import br.eti.erickcouto.occultflashtag.R;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;

public class MeasurementThread extends AsyncTask<Activity, String, Long>  {

	private OccultFlashTag main;
	private boolean erro = false;
	
	@SuppressWarnings("deprecation")
	@Override
	protected Long doInBackground(Activity... params) {
		main = (OccultFlashTag) params[0];

		Long d = 0l;

		for(int x=0; x < 10; x++){
			Long ini = 0l;
			Long end = 0l;
			
			try {
                synchronized(this){
                	ini = System.currentTimeMillis();
                	Camera camera = Camera.open();
                	if(camera != null){
	        	    	Parameters p = camera.getParameters();
	        	        p.setFlashMode(Parameters.FLASH_MODE_TORCH);
	        	        camera.setParameters(p);
	        	        camera.startPreview();
	                	end = System.currentTimeMillis();
	                    wait(1000/25);
	                    camera.stopPreview();
	                    camera.release();
	                    wait(500);
                	} else {
                		erro = true;
                		break;
                	}
            }
			}
            catch(InterruptedException ex){                    
            	erro = true;
            }
			
			if(!erro){
				d = main.getAppData().getCameraDelay();
				if (d == null){
					d = end - ini;
				} else {
					d = (d + (end - ini)) / 2;
				}
				main.getAppData().setCameraDelay(d);
			}
		    
            publishProgress(main.getString(R.string.out_measuring_flash) + " " + (x+1) + "/10 - " + d + main.getString(R.string.out_milliseconds_symbol));
		    
		}

		return d;
		
    }

    protected void onProgressUpdate(String... progress) {
		TextView txtStatusBar = ((TextView) main.findViewById(R.id.txt_status_bar));
		txtStatusBar.setText(progress[0]);
    }

    protected void onPostExecute(Long result) {
    	Button btnEstimatedUtc1 = ((Button) main.findViewById(R.id.btn_estimated_utc1));
    	Button btnEstimatedUtc2 = ((Button) main.findViewById(R.id.btn_estimated_utc2));
		
    	TextView txtStatusBar = ((TextView) main.findViewById(R.id.txt_status_bar));
    	if(erro) txtStatusBar.setText(main.getString(R.string.out_flash_not_available));
		else txtStatusBar.setText(main.getString(R.string.out_successful_flash_measurement) + " - " + result + main.getString(R.string.out_milliseconds_symbol));
    	
    	btnEstimatedUtc1.setClickable(true);
    	btnEstimatedUtc1.setEnabled(true);
    	btnEstimatedUtc2.setClickable(true);
    	btnEstimatedUtc2.setEnabled(true);
		
    	Button btnFlashMeasurement = ((Button) main.findViewById(R.id.btn_flash_measurement));
    	btnFlashMeasurement.setClickable(true);
    	btnFlashMeasurement.setEnabled(true);

    }

	
}
