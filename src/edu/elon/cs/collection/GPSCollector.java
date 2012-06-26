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

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class GPSCollector extends Thread {

	private LocationManager locManager;
	private boolean running;
	private FileOutputStream outFile = null;
	private Handler handler;

	public GPSCollector(Context context, Handler handler) {
		this.running = false;
		this.handler = handler;

		// GPS setup
		locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10000.0f, locListener);

		// File setup
		File dir = Environment.getExternalStorageDirectory();
		String fname = System.currentTimeMillis() + "-gps.csv";
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

		try {
			outFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private LocationListener locListener = new LocationListener() {

		public void onLocationChanged(Location location) {

			// only write the string if thread is running
			if (running) {
				String printOut = System.currentTimeMillis() + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "\n";
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
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.AVAILABLE) {
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("gps-message", "Available");
				msg.setData(b);
				handler.sendMessage(msg);
			}
		}
	};
}