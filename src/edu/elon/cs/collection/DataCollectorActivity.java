package edu.elon.cs.collection;

/**
 *  Starts the GPS and Accelerometer threads for data collection.
 * 
 *  @author C. Brockmyre and J. Hollingsworth
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import edu.elon.cs.collection.CompassView;
import edu.elon.cs.collection.R;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DataCollectorActivity extends Activity {

	private TextView txtDirections;
	private Button theGoButton;

	private SensorManager sensorManager;
	private LocationManager locManager;

	private boolean gpsAcquired;
	
	private float[] magnetic_field_values;
	private float[] accelerometer_values;
	private float[] gyroscope_values;

	private CompassView compass;
	private float compassDirection;
	float[] rMatrix = new float[16];
	float[] iMatrix = new float[16];

	private SimpleDateFormat sdf;

	private FileOutputStream directionsFile = null;
	private FileOutputStream sensorsFile = null;
	private FileOutputStream gpsFile = null;

	private int counter;
	private ArrayList<String> directions = new ArrayList<String>(Arrays.asList("1.	Face North",
			"2.	Walk 30 steps forward -- phone out front (steady)",
			"3.	Turn left 180 degrees",
			"4.	Return to the cone -- phone looking generally forward but around a little",
			"5.	Turn right 90 degrees",
			"6.	Walk 20 steps forward",
			"7.	Turn right 90 degrees",
			"8.	Walk 20 steps forward",
			"9.	Turn right -- face cone",
			"10.	Return to the cone",
			"11.	Orient East",
			"12.	Walk 20 steps forward",
			"13.	Turn left 90 degrees",
			"14.	Walk 20 steps forward",
			"15.	Turn left -- face cone",
			"16.	Return to cone",
			"17.	Orient South",
			"18.	Walk 10 steps",
			"19.	Turn left 360 degrees",
			"20.	Walk 10 steps -- phone moving around a little",
			"21.	Turn right 180 degrees",
			"22.	Return to the cone in a serpentine pattern",
			"23.	Turn right 360 degrees -- phone looking up and down",
			"DONE"));

	/** Called when the activity is first created. **/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* UI setup */
		txtDirections = (TextView) findViewById(R.id.direct);
		txtDirections.setText("Ready");
		theGoButton = (Button) findViewById(R.id.gobutton);
		theGoButton.setOnClickListener(goButtonListener);
		theGoButton.setEnabled(false);
		compass = (CompassView) findViewById(R.id.compass);

		/* walk through counter */
		counter = 0;

		/* sensors and GPS */
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 

		/* gps ready to go? */
		gpsAcquired = false;

		/* open data files */
		sdf = new SimpleDateFormat("yyyy_MM_dd-7HH:mm:ss:SSS");
		String currentDateAndTime = sdf.format(new Date());
		File dir = Environment.getExternalStorageDirectory();
		String directions = currentDateAndTime + "-directions.csv";
		String sensors = currentDateAndTime + "-sensors.csv";
		String gps = currentDateAndTime + "-gps.csv";

		try {
			directionsFile = new FileOutputStream(new File(dir, directions));
			sensorsFile = new FileOutputStream(new File(dir, sensors));
			gpsFile = new FileOutputStream(new File(dir, gps));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		/* register for accelerometer and gyroscope */
		sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_FASTEST);
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, locListener);
	}

	@Override
	public void onPause() {
		super.onPause();

		/* unregister the listeners */
		sensorManager.unregisterListener(sensorListener);
		locManager.removeUpdates(locListener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();	

		/* close all of the files */
		try {
			directionsFile.close();
			sensorsFile.close();
			gpsFile.close();
		} catch (IOException e) { e.printStackTrace(); }
	}

	private void recordDirection(String displayedDirection){
		String str = sdf.format(new Date()) + "," + displayedDirection + "\n";
		try {
			directionsFile.write(str.getBytes());
		} catch (IOException e) { e.printStackTrace(); }
	}

	private SensorEventListener sensorListener = new SensorEventListener(){

		public void onSensorChanged(SensorEvent event) {
			
			if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
				return;
						
			switch(event.sensor.getType()) {
			case Sensor.TYPE_MAGNETIC_FIELD:
				magnetic_field_values = event.values.clone();
				break;
			case Sensor.TYPE_ACCELEROMETER:
				accelerometer_values = event.values.clone();
				break;
			case Sensor.TYPE_GYROSCOPE:
				gyroscope_values = event.values.clone();
				break;
			}
			
			if (magnetic_field_values != null && accelerometer_values != null) {
				if (SensorManager.getRotationMatrix(rMatrix, iMatrix, accelerometer_values, magnetic_field_values)) {
					float[] actual_orientation = new float[3];
					SensorManager.getOrientation(rMatrix, actual_orientation);
					
					float currentDirection = (float) -Math.toDegrees(actual_orientation[0]);
					compassDirection = (compassDirection*0.97f) + (currentDirection*0.03f);
					compass.setAzimuth(compassDirection);
					compass.invalidate();
				}
			}
			
			if (gpsAcquired){
				String str = sdf.format(new Date()) + "," + 
							accelerometer_values[0] + "," + 
							accelerometer_values[1] + "," + 
							accelerometer_values[2] + "," + 
							compassDirection + "," + 
							gyroscope_values[0] + "," + 
							gyroscope_values[1] + "," + 
							gyroscope_values[2] +  "\n";
				try {
					sensorsFile.write(str.getBytes());
				} catch (IOException e) { e.printStackTrace(); }
			}
		}		

		public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	};

	private OnClickListener goButtonListener = new OnClickListener() {

		public void onClick(View view){
			if (txtDirections.getText().equals("Ready")){
				txtDirections.setText(directions.get(counter));
				recordDirection(directions.get(counter));
				counter++;
			} else {
				txtDirections.setText(directions.get(counter));
				recordDirection(directions.get(counter));
				counter++;
				if (txtDirections.getText().equals("DONE")){
					try {
						directionsFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					theGoButton.setEnabled(false);
					finish();
				}
			}
		}
	};

	private LocationListener locListener = new LocationListener() {

		public void onLocationChanged(Location location) {

			gpsAcquired = true;
			theGoButton.setEnabled(true);

			String printOut = sdf.format(new Date()) + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "\n";
			try {
				gpsFile.write(printOut.getBytes());
			} catch (IOException e) { e.printStackTrace(); }
		}

		public void onProviderDisabled(String arg0) { }
		public void onProviderEnabled(String arg0) { }
		public void onStatusChanged(String provider, int status, Bundle extras) { }
	};
}