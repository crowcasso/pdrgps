package edu.elon.cs.collection;

/**
 *  Collects GPS data into a file.
 * 
 *  @author J. Hollingsworth
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class GPSCollector extends Thread {

	private LocationManager locManager;
	private boolean running;
	private FileOutputStream outFile = null;
	private Handler handler;
	private boolean active;
	private SimpleDateFormat sdf;

	public GPSCollector(Context context, Handler handler) {
		this.running = false;
		this.handler = handler;

		this.active = false;
		
		// GPS setup
		locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, locListener);

		// File setup
		File dir = Environment.getExternalStorageDirectory();
		sdf = new SimpleDateFormat("yyyy_MM_dd-7HH:mm:ss:SSS");
		String currentDateAndTime = sdf.format(new Date());
		
		String fname = currentDateAndTime + "-gps.csv";
		Log.d("GPSCollector", "New: " + fname);
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

		locManager.removeUpdates(locListener);
		Log.d("GPSCollector", "closing  file");

		try {
			outFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private LocationListener locListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			
			if (!active) {
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("gps-message", "Available");
				msg.setData(b);
				handler.sendMessage(msg);
				active = true;
			}
			
			Log.d("onLocationChanged", "HERE NOW!");

			// only write the string if thread is running
			if (running) {
				Log.d("onLocationChanged", "writing to file");
				String printOut = sdf.format(new Date()) + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "\n";
				try {
					outFile.write(printOut.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		//@Override
		public void onProviderDisabled(String arg0) {}
		//@Override
		public void onProviderEnabled(String arg0) {}
		//@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
}