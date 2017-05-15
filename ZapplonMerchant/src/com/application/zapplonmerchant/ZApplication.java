package com.application.zapplonmerchant;

import java.util.List;

import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.LruCache;
import com.application.zapplonmerchant.utils.RequestWrapper;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.location.ZLocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.AsyncTask;

public class ZApplication extends Application {

	public ZLocationListener zll = new ZLocationListener(this);
	public LocationManager locationManager = null;
	public String location = "";
	public double lat = 0;
	public double lon = 0;
	public boolean isNetworkProviderEnabled = false;
	public boolean isGpsProviderEnabled = false;
	public boolean firstLaunch = false;
	public int state = CommonLib.LOCATION_DETECTION_RUNNING;

	private CheckLocationTimeoutAsync checkLocationTimeoutThread;

	public LruCache<String, Bitmap> cache;

	public void onCreate() {
		super.onCreate();

		cache = new LruCache<String, Bitmap>(30);
		SharedPreferences prefs = getSharedPreferences("application_settings", 0);
		try {
			lat = Double.parseDouble(prefs.getString("lat1", "0"));
			lon = Double.parseDouble(prefs.getString("lon1", "0"));
		} catch (ClassCastException e) {
		} catch (Exception e) {
		}
		location = prefs.getString("location", "");

		// Managers initialize
		RequestWrapper.Initialize(getApplicationContext());
		UploadManager.setContext(getApplicationContext());
	}

	public void setLocationString(String lstr) {
		location = lstr;
		SharedPreferences prefs = getSharedPreferences("application_settings", 0);
		Editor editor = prefs.edit();
		editor.putString("location", location);
		editor.commit();
	}

	public String getLocationString() {
		return location;
	}

	public void interruptLocationTimeout() {
		// checkLocationTimeoutThread.interrupt();
		if (checkLocationTimeoutThread != null)
			checkLocationTimeoutThread.interrupt = false;
	}

	public void startLocationCheck() {

		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

		if (result == ConnectionResult.SUCCESS) {
			zll.getFusedLocation(this);
		} else {
			getAndroidLocation();
		}
	}

	public void getAndroidLocation() {

		CommonLib.ZLog("zll", "getAndroidLocation");

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(true);

		if (providers != null) {
			for (String providerName : providers) {
				if (providerName.equals(LocationManager.GPS_PROVIDER))
					isGpsProviderEnabled = true;
				if (providerName.equals(LocationManager.NETWORK_PROVIDER))
					isNetworkProviderEnabled = true;
			}
		}

		if (isNetworkProviderEnabled || isGpsProviderEnabled) {

			if (isGpsProviderEnabled)
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, zll);
			if (isNetworkProviderEnabled)
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 500.0f, zll);

			if (checkLocationTimeoutThread != null) {
				checkLocationTimeoutThread.interrupt = false;
			}

			checkLocationTimeoutThread = new CheckLocationTimeoutAsync();
			checkLocationTimeoutThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		} else {
			zll.locationNotEnabled();
		}
	}

	private class CheckLocationTimeoutAsync extends AsyncTask<Void, Void, Void> {
		boolean interrupt = true;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			if (interrupt) {
				zll.interruptProcess();
			}
		}
	}

	public boolean isLocationAvailable() {
		return (isNetworkProviderEnabled || isGpsProviderEnabled);
	}

}
