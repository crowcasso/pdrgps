package edu.elon.cs.collection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


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
import android.widget.TextView;

public class DataCollectorActivity extends Activity {
	
	private LocationManager locManager;
	private SensorManager manager;
	private File sensorFile;
	private File gpsFile;
	//private String sensorFile;
	private TextView xaxis;
	private TextView yaxis;
	private TextView zaxis;
	private float x;
	private float y;
	private float z;
	private float orientation;
	FileOutputStream fos = null;
	FileOutputStream fos2 = null;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10000.0f, locListener);
		
		manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		//sensorFile = "sensorFile"
		/*try {
			sensorFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		xaxis = (TextView) findViewById(R.id.xaxis);
		yaxis = (TextView) findViewById(R.id.yaxis);
		zaxis = (TextView) findViewById(R.id.zaxis);
		
		File test = Environment.getExternalStorageDirectory();
		try {
			fos = new FileOutputStream(new File(test, "sensorFile.csv"));
			fos2 = new FileOutputStream(new File(test, "gpsFile.csv"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.d("onCreate", getFilesDir().toString());

    }
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	try {
			fos.close();
			fos2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	@Override
	protected void onResume(){
		super.onResume();
		manager.registerListener(sensorListener, manager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_FASTEST);
		manager.registerListener(sensorListener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
	}
    
	@Override
	protected void onPause(){
		super.onPause();
		manager.unregisterListener(sensorListener);
	}
    
    private LocationListener locListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			// write the time and the gps location to a file
			String printOut = System.currentTimeMillis() + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy();
			//String str = System.currentTimeMillis() + "," + x + "," + y + "," + z + "," + orientation + "\n";
			try {
				//fos = openFileOutput("sensorFile", Context.MODE_PRIVATE);
				
				Log.d("Test", fos2.toString());
				fos.write(printOut.getBytes());
				//fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				zaxis.setText("didn't write");
			}
		}

		//@Override
		public void onProviderDisabled(String arg0) {
		}

		//@Override
		public void onProviderEnabled(String arg0) {
		}

		//@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}

    	
    };


	private SensorEventListener sensorListener = new SensorEventListener(){

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
				//write the time and accelerometer data to a file.
				//get the compass orientation in here too each time i think (aka dont need next if statement)
				xaxis.setText("X-axis: " + event.values[0]);
				yaxis.setText("Y-axis: " + event.values[1]);
				zaxis.setText("Z-axis: " + event.values[2]);
				x = event.values[0];
				y = event.values[1];
				z = event.values[2];				
			}
			if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
				orientation = -event.values[0];
			}
			String str = System.currentTimeMillis() + "," + x + "," + y + "," + z + "," + orientation + "\n";
			try {
				//fos = openFileOutput("sensorFile", Context.MODE_PRIVATE);
				
				Log.d("Test", fos.toString());
				fos.write(str.getBytes());
				//fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				xaxis.setText("didn't write");
			}
		}		
	};
    
}