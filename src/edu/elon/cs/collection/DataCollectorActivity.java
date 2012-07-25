package edu.elon.cs.collection;

/**
 *  Starts the GPS and Accelerometer threads for data collection.
 * 
 *  @author C. Brockmyre and J. Hollingsworth
 */

import java.util.ArrayList;
import java.util.Arrays;

import edu.elon.cs.collection.CompassView;
import edu.elon.cs.collection.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

public class DataCollectorActivity extends Activity {

	//private TextView gpstv;
	//private TextView acceltv;
	private TextView direct;
	private Button go;

	private GPSCollector gpsCollector;
	private AccelerometerCollector accelCollector;
	
	private CompassView compass;
	private float compassValue;
	
	private int counter;
	private ArrayList<String> directions = new ArrayList<String>(Arrays.asList("1.	Face North",
			"2.	Walk 30 steps forward – phone out front (steady)",
			"3.	Turn left 180 degrees",
			"4.	Return to the cone – phone looking generally forward but around a little",
			"5.	Turn right 90 degrees",
			"6.	Walk 20 steps forward",
			"7.	Turn right 90 degrees",
			"8.	Walk 20 steps forward",
			"9.	Turn right – face cone",
			"10.	Return to the cone",
			"11.	Orient East",
			"12.	Walk 20 steps forward",
			"13.	Turn left 90 degrees",
			"14.	Walk 20 steps forward",
			"15.	Turn left – face cone",
			"16.	Return to cone",
			"17.	Orient South",
			"18.	Walk 10 steps",
			"19.	Turn left 360 degrees",
			"20.	Walk 10 steps – phone moving around a little",
			"21.	Turn right 180 degrees",
			"22.	Return to the cone in a serpentine pattern",
			"23.	Turn right 360 degrees – phone looking up and down",
			"DONE"));

	/** Called when the activity is first created. **/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* UI setup */
		direct = (TextView) findViewById(R.id.direct);
		direct.setText("Ready");
		go = (Button) findViewById(R.id.gobutton);
		go.setOnClickListener(goButtonListener);
		go.setEnabled(false);
		
		counter = 0;

		setupGPS();
		setupAccelerometer();
		
		compass = (CompassView) findViewById(R.id.compass);
		
		compass.setAzimuth(compassValue);
		compass.invalidate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "ON_DESTROY", Toast.LENGTH_LONG).show();
		exitAccelerometer();
		exitGPS();
	}

	private void setupGPS() {
		/* GPS Thread */
		Log.d("TEST", "calling GPSCollector");
		gpsCollector = new GPSCollector(getBaseContext(), new Handler() {
			public void handleMessage(Message m) {
				String msg = m.getData().getString("gps-message");
				if (msg.equals("Available")) {
					go.setEnabled(true);
				}
			}
		});  
	}
	
	private void setupAccelerometer() {
		/* Accelerometer Thread */
		accelCollector = new AccelerometerCollector(getBaseContext(), new Handler() {
			public void handleMessage(Message m) {
				String msg = m.getData().getString("compass-message");
				compassValue = Float.parseFloat(msg);
				compass.setAzimuth(compassValue);
				compass.invalidate();
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
	
	private void exitAccelerometer() {
		// exit the thread nicely
		boolean retry = true;
		accelCollector.setRunning(false);
		while (retry) {
			try {
				accelCollector.join();
				retry = false;
			} catch (InterruptedException ex) {}
		}
	}
	
	private OnClickListener goButtonListener = new OnClickListener() {
	
		public void onClick(View view){
			if (direct.getText().equals("Ready")){
				gpsCollector.setRunning(true);
				gpsCollector.start();
				accelCollector.setRunning(true);
				accelCollector.start();
				direct.setText(directions.get(counter));
				counter++;
			} else {
				direct.setText(directions.get(counter));
				counter++;
				if (direct.getText().equals("DONE")){
					exitGPS();
					exitAccelerometer();
					go.setEnabled(false);
					finish();
				}
			}

		}
	};
}