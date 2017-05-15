package com.application.zapplonmerchant.views;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.TypefaceSpan;
import com.application.zapplonmerchant.utils.location.ZLocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MapViewLayout extends ActionBarActivity implements ZLocationCallback {

	private ZApplication zapp;
	private SharedPreferences prefs;
	private int width;
	private LayoutInflater inflater;
	private AsyncTask mAsyncRunning;

	/** Map Object */
	private boolean mapRefreshed = false;
	private GoogleMap mMap;
	private MapView mMapView;
	private float defaultMapZoomLevel = 12.5f + 1.75f;
	private boolean mMapSearchAnimating = false;
	public boolean mapSearchVisible = false;
	private boolean mapOptionsVisible = true;
	private final float MIN_MAP_ZOOM = 13.0f;

	private double lat;
	private double lon;

	private boolean destroyed = false;
	private boolean isEditable = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_view_layout);

		width = getWindowManager().getDefaultDisplay().getWidth();

		if (getIntent() != null && getIntent().hasExtra("edit"))
			isEditable = getIntent().getBooleanExtra("edit", false);
		
		Drawable dr = getResources().getDrawable(R.drawable.mapmarker);
		Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
		int height = bitmap.getHeight();

		findViewById(R.id.markerImg).setPadding(0, 0, 0, height);

		inflater = LayoutInflater.from(this);

		try {
			MapsInitializer.initialize(this);
		} catch (Exception e) {
			// Crashlytics.logException(e);
		}

		mMapView = (MapView) findViewById(R.id.search_map);
		mMapView.onCreate(savedInstanceState);

		setupActionBar();

		prefs = getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) getApplication();
		
		if(isEditable) {
			lat = getIntent().getDoubleExtra("lat", 0.0);
			lon = getIntent().getDoubleExtra("lon", 0.0);
		} else{ 
			lat = zapp.lat;
			lon = zapp.lon;
		}
		refreshMap();

		zapp.zll.forced = true;
		zapp.zll.addCallback(this);
		zapp.startLocationCheck();

	}

	@Override
	public void onBackPressed() {
		if (mMap != null && mMap.getCameraPosition() != null) {
			LatLng center = mMap.getCameraPosition().target;
			if (center != null) {
				Intent data = new Intent();
				data.putExtra("latitude", center.latitude);
				data.putExtra("longitude", center.longitude);				
				setResult(StoreDetailsScreen.RESULT_CODE_OK, data);
			}
		} else {
			Toast.makeText(this, getResources().getString(R.string.choose_different_location), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
		actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBarCustomView.findViewById(R.id.tick_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(actionBarCustomView);

		SpannableString s = null;
		if(isEditable)
			s = new SpannableString(getString(R.string.edit_location));
		else
			s = new SpannableString(getString(R.string.add_location));
		
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

		((RelativeLayout.LayoutParams) actionBarCustomView.findViewById(R.id.back_icon).getLayoutParams())
				.setMargins(width / 40, 0, 0, 0);
		actionBarCustomView.findViewById(R.id.title).setPadding(width / 20, 0, width / 40, 0);
		title.setText(s);
	}

	public void proceed(View view) {
		if (mMap != null && mMap.getCameraPosition() != null) {
			LatLng center = mMap.getCameraPosition().target;
			if (center != null) {
				Intent data = new Intent();
				data.putExtra("latitude", center.latitude);
				data.putExtra("longitude", center.longitude);				
				setResult(StoreDetailsScreen.RESULT_CODE_OK, data);
				finish();
			}
		} else
			Toast.makeText(this, getResources().getString(R.string.choose_different_location), Toast.LENGTH_SHORT)
					.show();

	}

	private void refreshMap() {
		findViewById(R.id.wishbox_progress_container).setVisibility(View.GONE);
		findViewById(R.id.content).setVisibility(View.VISIBLE);

		// if (lat != 0.0 && lon != 0.0) {

		if (mMap == null)
			setUpMapIfNeeded();
		// }
	}

	public void actionBarSelected(View v) {
		switch (v.getId()) {
		case R.id.home_icon_container:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	private boolean displayed = false;

	@Override
	public void onResume() {

		super.onResume();
		displayed = true;
		if (mMapView != null) {
			mMapView.onResume();

			if (mMap == null && (lat != 0.0 || lon != 0.0))
				setUpMapIfNeeded();
		}

	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    if(mMapView != null)
	    	mMapView.onPause();
	}

	@Override
	public void onLowMemory() {
	    super.onLowMemory();
	    if(mMapView != null)
	    	mMapView.onLowMemory();
	}

	private void setUpMapIfNeeded() {
		if (mMap == null && mMapView != null)
			mMap = mMapView.getMap();
		if (mMap != null) {

			LatLng targetCoords = null;

			if (lat != 0.0 || lon != 0.0)
				targetCoords = new LatLng(lat, lon);
			else {
				// target the current city
			}
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mMap.getUiSettings().setAllGesturesEnabled(true);
			mMap.setMyLocationEnabled(true);
			mMap.setBuildingsEnabled(true);
			// mMap.getUiSettings().setZoomControlsEnabled(false);
			// mMap.getUiSettings().setTiltGesturesEnabled(false);
			// mMap.getUiSettings().setCompassEnabled(false);

			CameraPosition cameraPosition;
			if (targetCoords != null) {
				cameraPosition = new CameraPosition.Builder().target(targetCoords) // Sets
																					// the
																					// center
																					// of
																					// the
																					// map
																					// to
																					// Mountain
																					// View
						.zoom(defaultMapZoomLevel) // Sets the zoom
						.build(); // Creates a CameraPosition from the builder

				try {
					mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				} catch (Exception e) {
					MapsInitializer.initialize(MapViewLayout.this);
					mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				}
			}

		}

	}

	@Override
	public void onDestroy() {
		if (mMapView != null)
			mMapView.onDestroy();
		destroyed = true;
		zapp.zll.removeCallback(this);
		zapp.cache.clear();
		super.onDestroy();
	}

	@Override
	public void onCoordinatesIdentified(Location loc) {
		if (loc != null) {
			LatLng targetCoords = null;
			if (lat == 0.0 && lon == 0.0) {
				lat = loc.getLatitude();
				lon = loc.getLongitude();
				targetCoords = new LatLng(lat, lon);
				CameraPosition cameraPosition = new CameraPosition.Builder().target(targetCoords) // Sets
						// the
						// center
						// of
						// the
						// map
						// to
						// Mountain
						// View
						.zoom(defaultMapZoomLevel) // Sets the zoom
						.build(); // Creates a CameraPosition from the builder

				try {
					mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				} catch (Exception e) {
					MapsInitializer.initialize(MapViewLayout.this);
					mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				}

			}
		}
	}

	@Override
	public void onLocationIdentified() {
		CommonLib.ZLog("test", "inside");
	}

	@Override
	public void onLocationNotIdentified() {
		CommonLib.ZLog("test", "inside");
	}

	@Override
	public void onDifferentCityIdentified() {
		CommonLib.ZLog("test", "inside");
	}

	@Override
	public void locationNotEnabled() {
		CommonLib.ZLog("test", "inside");
	}

	@Override
	public void onLocationTimedOut() {
		CommonLib.ZLog("test", "inside");
	}

	@Override
	public void onNetworkError() {
		CommonLib.ZLog("test", "inside");
	}

}
