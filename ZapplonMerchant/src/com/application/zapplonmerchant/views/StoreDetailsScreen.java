package com.application.zapplonmerchant.views;

import java.util.ArrayList;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.Store;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.TypefaceSpan;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.UploadManagerCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class StoreDetailsScreen extends ActionBarActivity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private int width;
	private ZApplication zapp;
	private Activity mContext;
	private boolean destroyed = false;
	private ProgressDialog zProgressDialog;
	private final ArrayList<String> storeTypes = new ArrayList<String>();
	private LayoutInflater inflater;

	private double latitude = 0.0;
	private double longitude = 0.0;

	public static final int REQUEST_CODE_MAP = 101;
	public static final int RESULT_CODE_OK = 102;

	private boolean isEditable = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.store_details_screen);
		prefs = getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) getApplication();
		width = getWindowManager().getDefaultDisplay().getWidth();

		mContext = this;
		inflater = LayoutInflater.from(getApplicationContext());

		setUpActionBar();
		UploadManager.addCallback(this);
		storeTypes.add(mContext.getResources().getString(R.string.salon_spa_category));
		storeTypes.add(mContext.getResources().getString(R.string.food_drinks_category));

		if (getIntent() != null && getIntent().hasExtra("edit"))
			isEditable = getIntent().getBooleanExtra("edit", false);

		findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String name = ((TextView) findViewById(R.id.name)).getText().toString();
				if (name == null || name.length() == 0) {
					Toast.makeText(mContext, getResources().getString(R.string.enter_details), Toast.LENGTH_LONG)
							.show();
					return;
				}

				String type = ((TextView) findViewById(R.id.type)).getText().toString();
				if (type == null || type.length() == 0) {
					Toast.makeText(mContext, getResources().getString(R.string.enter_details), Toast.LENGTH_LONG)
							.show();
					return;
				}

				String address = ((TextView) findViewById(R.id.address)).getText().toString();
				if (address == null || address.length() == 0) {
					Toast.makeText(mContext, getResources().getString(R.string.enter_details), Toast.LENGTH_LONG)
							.show();
					return;
				}

				String contact = ((TextView) findViewById(R.id.contact)).getText().toString();
				if (contact == null || contact.length() == 0) {
					Toast.makeText(mContext, getResources().getString(R.string.enter_details), Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (type.equals(mContext.getResources().getString(R.string.salon_spa_category))) {
					type = "1";
				} else if (type.equals(mContext.getResources().getString(R.string.food_drinks_category))) {
					type = "2";
				}

				zProgressDialog = ProgressDialog.show(mContext, null,
						getResources().getString(R.string.request_upload_content_store));

				UploadManager.addStore(name, type, address, contact, latitude, longitude, isEditable);
			}
		});

		findViewById(R.id.location).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(StoreDetailsScreen.this, MapViewLayout.class);
				startActivityForResult(intent, REQUEST_CODE_MAP);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

		findViewById(R.id.type).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mContext != null) {
					Bundle args = new Bundle();
					args.putSerializable("list_items", storeTypes);
					showPopupDialog(StoreDetailsScreen.DIALOG_TYPE_PICKER, args);
				}
			}
		});

		if (isEditable) {
			int type = prefs.getInt("store_type", -1);
			if (type == 1) {
				((TextView) findViewById(R.id.type))
						.setText(mContext.getResources().getString(R.string.salon_spa_category));
			} else if (type == 2) {
				((TextView) findViewById(R.id.type))
						.setText(mContext.getResources().getString(R.string.food_drinks_category));
			}
			latitude = prefs.getFloat("store_lat", 0);
			longitude = prefs.getFloat("store_lon", 0);
			((TextView) findViewById(R.id.name)).setText(prefs.getString("store_name", ""));
			((TextView) findViewById(R.id.address)).setText(prefs.getString("store_address", ""));
			((TextView) findViewById(R.id.contact)).setText(prefs.getString("store_contact", ""));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_CODE_OK && data != null) {
			if (data.hasExtra("latitude"))
				latitude = data.getDoubleExtra("latitude", 0.0);
			if (data.hasExtra("longitude"))
				longitude = data.getDoubleExtra("longitude", 0.0);

			if (latitude != 0.0 && longitude != 0.0) {
				((TextView) findViewById(R.id.location))
						.setText(getResources().getString(R.string.location_identified));
			}
		}

	}

	private void setUpActionBar() {

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);

		String str = getResources().getString(R.string.add_store);
		SpannableString s = new SpannableString(str);
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
		if (!isAndroidL)
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_dark_feedback));

		actionBar.setTitle(s);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		if (zProgressDialog != null && zProgressDialog.isShowing()) {
			zProgressDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {

		if (requestType == CommonLib.STORE_ADD) {
			if (!destroyed) {
				if (zProgressDialog != null && zProgressDialog.isShowing())
					zProgressDialog.dismiss();

				if (data != null && status) {
					SharedPreferences.Editor editor = prefs.edit();

					Store store = null;
					try {
						store = (Store) data;
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (store != null) {
						editor.putInt("store_id", store.getStoreId());
						editor.putInt("store_type", store.getStoreType());
						editor.putFloat("store_lat", (float) store.getLatitude());
						editor.putFloat("store_lon", (float) store.getLongitude());
						editor.putString("store_name", store.getStoreName());
						editor.putString("store_address", store.getAddress());
						editor.putString("store_contact", store.getContactNumber());
						editor.putInt("availability", store.getAvailability());
					}

					editor.commit();

					navigateToHome();
				} else
					Toast.makeText(StoreDetailsScreen.this, getResources().getString(R.string.err_occurred),
							Toast.LENGTH_LONG).show();
			}
		}
	}

	private void navigateToHome() {
		if (prefs.getInt("merchant_id", 0) != 0) {
			if (prefs.getInt("store_id", 0) == 0) {
				// Store add activity
			} else {
				Intent intent = new Intent(this, Home.class);
				startActivity(intent);
				finish();
			}
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

	public static final int DIALOG_TYPE_PICKER = 101;

	// from here
	public void showPopupDialog(int type, Bundle args) {
		DialogFragment newFragment = MyAlertDialogFragment.newInstance(type, args);
		newFragment.show(getSupportFragmentManager(), "listPickerDialog");
	}

	public static class MyAlertDialogFragment extends DialogFragment {

		public static MyAlertDialogFragment newInstance(int type, Bundle args) {
			MyAlertDialogFragment newFragment = new MyAlertDialogFragment();
			args.putInt("type", type);
			newFragment.setArguments(args);
			return newFragment;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog mDialog = null;
			final int type = getArguments().getInt("type");
			if (type == DIALOG_TYPE_PICKER) {
				final ArrayList<String> nameList = (ArrayList<String>) getArguments().get("list_items");
				AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
						.setTitle(getActivity().getResources().getString(R.string.select_store_type)).setAdapter(
								((StoreDetailsScreen) getActivity()).new ListAdapter(getActivity(),
										R.layout.store_picker_snippet, nameList),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										((StoreDetailsScreen) getActivity()).returnSelecteListItem(nameList.get(which),
												which, type);
										dialog.dismiss();
									}
								});
				mDialog = mDialogBuilder.create();
			}
			return mDialog;
		}
	}

	/*
	 * call Back from ListDialog in this Activity
	 */
	private void returnSelecteListItem(String listName, int which, int dialogOrSelectionType) {
		if (findViewById(R.id.type) != null) {
			((TextView) findViewById(R.id.type)).setText(listName);
		}
	}

	private class ListAdapter extends ArrayAdapter<String> {

		ArrayList<String> items;

		public ListAdapter(Context context, int resourceId, ArrayList<String> objects) {
			super(context, resourceId, objects);
			this.items = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null || convertView.findViewById(R.id.restaurant_name) == null) {
				convertView = inflater.inflate(R.layout.store_picker_snippet, parent, false);
				convertView.findViewById(R.id.restaurant_picker_icon).setVisibility(View.GONE);
				convertView.findViewById(R.id.restaurant_locality).setVisibility(View.GONE);
			}
			convertView.findViewById(R.id.res_snippet_container).setBackgroundResource(0);
			TextView itemName = ((TextView) convertView.findViewById(R.id.restaurant_name));
			itemName.setText(items.get(position));
			itemName.setTextColor(getResources().getColor(R.color.holo_white));
			convertView.findViewById(R.id.restaurant_name).setPadding(width / 20, width / 40, width / 20, width / 40);
			return convertView;
		}
	}

}
