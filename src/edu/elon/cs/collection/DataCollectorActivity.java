package edu.elon.cs.collection;

/**
 *  Starts the GPS and Accelerometer threads for data collection.
 * 
 *  @author C. Brockmyre and J. Hollingsworth
 */

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;

public class DataCollectorActivity extends Activity {

	private TextView gpstv;
	private TextView acceltv;
	private Button go;

	private GPSCollector gpsCollector;

	/** Called when the activity is first created. **/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* UI setup */
		gpstv = (TextView) findViewById(R.id.gpstv);
		go = (Button) findViewById(R.id.gobutton);
		go.setOnClickListener(goButtonListener);
		go.setEnabled(false);

		setupGPS();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		exitGPS();
	}

	private void setupGPS() {
		/* GPS Thread */
		gpsCollector = new GPSCollector(getBaseContext(), new Handler() {
			public void handleMessage(Message m) {
				String msg = m.getData().getString("gps-message");
				gpstv.setText(msg);
				if (msg.equals("Available")) {
					go.setEnabled(true);
				}
			}
		});  
	}

	private void exitGPS() {
		// exit the thread nicely
		boolean retry = true;
		gpsCollector.setRunning(false);
		while (retry) {
			try {
				gpsCollector.join();
				retry = false;
			} catch (InterruptedException ex) {}
		}
	}

	private OnClickListener goButtonListener = new OnClickListener() {
		public void onClick(View view) {
			if (go.getText().equals("End Data Collection")) {
				exitGPS();
				go.setEnabled(false);
				go.setText("Start Data Collection");
				setupGPS();
			} else {
				go.setText("End Data Collection");
				gpsCollector.setRunning(true);
				gpsCollector.start();
			}
		}    	
	};
}