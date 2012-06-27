package edu.elon.cs.collection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AccelerometerCollector extends Thread {    //implements SensorEventListener{
	
	private boolean running;
	private FileOutputStream outFile = null;
	private Handler handler;
	private SensorManager manager;
	float x;
	float y;
	float z;
	float orientation;


	public AccelerometerCollector(Context context, Handler handler) {
		this.running = false;
		this.handler = handler;

		//accelerometer senor manager setup
		manager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
		
/*
 * TODO
 * why does that line ^ not work?
 * Sensor.TYPE_ORIENTATION is deprecated and should be SensorManager.getOrientation
 * 
 * i need to put this somewhere but not sure where?
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("gps-message", "Available");
				msg.setData(b);
				handler.sendMessage(msg);
			
 */

		manager.registerListener(sensorListener, manager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_FASTEST);
		manager.registerListener(sensorListener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);


		// File setup
		File dir = Environment.getExternalStorageDirectory();
		String fname = System.currentTimeMillis() + "-accelerometer.csv";
		try {
			outFile = new FileOutputStream(new File(dir, fname));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}

	public void run() {

		while (running) {
			// do work here
		}

		//locManager.removeUpdates(locListener);
		
		
		try {
			outFile.close();
			manager.unregisterListener(sensorListener);
			//right?
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private SensorEventListener sensorListener = new SensorEventListener(){

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
				x = event.values[0];
				y = event.values[1];
				z = event.values[2];				
			}
			if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
				orientation = -event.values[0];
			}
			if (running){
				String str = System.currentTimeMillis() + "," + x + "," + y + "," + z + "," + orientation + "\n";
				try {
					outFile.write(str.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}		
	};

/*
 * if the class should implement sensorEventListener then the sensor listener above i think 
 * is useless.  Everything in the onSensorChanged method would go into the one below
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
	}
	
*/
}
