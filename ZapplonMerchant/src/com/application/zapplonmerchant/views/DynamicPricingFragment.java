package com.application.zapplonmerchant.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.StoreCatalogueItem;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.PostWrapper;
import com.application.zapplonmerchant.utils.RequestWrapper;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.UploadManagerCallback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class DynamicPricingFragment extends Fragment implements UploadManagerCallback {

	private ZApplication zapp;
	private Activity activity;
	private View getView;
	private SharedPreferences prefs;
	private int width, height;
	private LayoutInflater vi;
	ProgressDialog zProgressDialog;
	private boolean destroyed = false;
	public static final int REQUEST_CODE_MAP = 101;
	public static final int RESULT_CODE_OK = 102;

	private AsyncTask mAsyncTaskRunning, mGetAsyncRunning;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_dynamic_pricing, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();
		getView = getView();
		destroyed = false;
		prefs = activity.getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) activity.getApplication();
		width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
		vi = LayoutInflater.from(activity.getApplicationContext());

		UploadManager.addCallback(this);
		setUpHomeViews();
		refreshDiscount();
	}

	void setUpHomeViews() {

		// updated via get discount api call
		// ((TextView) getView.findViewById(R.id.current_Discount)).setText("");

		((TextView) getView.findViewById(R.id.current_crowd)).setText(prefs.getInt("current_occupancy", 0) + "");

		((SeekBar) getView.findViewById(R.id.seekbar_crowd)).setMax(prefs.getInt("max_occupancy", 0));
		((SeekBar) getView.findViewById(R.id.seekbar_crowd)).setProgress(prefs.getInt("current_occupancy", 0));

		((EditText) getView.findViewById(R.id.current_crowd)).addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// after 1 second, fire an async which will update this
				// value
				final int occupancy = Integer.parseInt(s.toString());
				if( occupancy > prefs.getInt("max_occupancy", 0)) {
					((TextView) getView.findViewById(R.id.current_crowd)).setText((occupancy - 1) + "");
					return;
				} else if(occupancy < 0) {
					((TextView) getView.findViewById(R.id.current_crowd)).setText(0 + "");
					return;
				}
				
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						if (destroyed)
							return;

						if (mAsyncTaskRunning != null)
							mAsyncTaskRunning.cancel(true);

						mAsyncTaskRunning = new UpdateOccupancy().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,
								new Object[] { occupancy });
					}
				}, 1000);
			}
		});
		((SeekBar) getView.findViewById(R.id.seekbar_crowd)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				if (fromUser) {
					((TextView) getView.findViewById(R.id.current_crowd)).setText(progress + "");
				}
			}
		});

		getView.findViewById(R.id.increase).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int progress = 0;
				try {
					progress = Integer
							.parseInt(((TextView) getView.findViewById(R.id.current_crowd)).getText().toString());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				progress++;
				((TextView) getView.findViewById(R.id.current_crowd)).setText(progress + "");
			}
		});
		
		getView.findViewById(R.id.decrease).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int progress = 0;
				try {
					progress = Integer
							.parseInt(((TextView) getView.findViewById(R.id.current_crowd)).getText().toString());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				progress--;
				((TextView) getView.findViewById(R.id.current_crowd)).setText(progress + "");
			}
		});

	}

	public void scrollSearchToTop() {
		try {
			if (getView.findViewById(R.id.home_fragment_scroll_root) != null) {
				((ScrollView) getView.findViewById(R.id.home_fragment_scroll_root)).smoothScrollTo(0, 0);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		if (zProgressDialog != null && zProgressDialog.isShowing()) {
			zProgressDialog.dismiss();
		}
		UploadManager.removeCallback(this);
		super.onDestroy();
	}

	private void refreshDiscount() {
		if (mGetAsyncRunning != null)
			mGetAsyncRunning.cancel(true);

		mGetAsyncRunning = new GetCurrentDiscount().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class UpdateOccupancy extends AsyncTask<Object, Void, Object[]> {

		private int occupancy;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			occupancy = (Integer) params[0];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("store_id", prefs.getInt("store_id", 0) + ""));
			nameValuePairs.add(new BasicNameValuePair("occupancy", "" + occupancy));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "store/occupancy?", nameValuePairs,
						PostWrapper.UPDATE_OCCUPANCY, activity);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(activity, (String) arg[1], Toast.LENGTH_SHORT).show();

			// if successful, make the get call

			if (arg[0].equals("success")) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("current_occupancy", occupancy).commit();
				refreshDiscount();
			}
		}
	}

	private class GetCurrentDiscount extends AsyncTask<Object, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "store/currentDiscount?store_id=" + prefs.getInt("store_id", 0);
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.CURRENT_DISCOUNT, RequestWrapper.FAV);
				CommonLib.ZLog("url", url);
				return info;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (destroyed)
				return;

			if (zProgressDialog != null && zProgressDialog.isShowing())
				zProgressDialog.dismiss();

			if (result != null) {
				if (result instanceof StoreCatalogueItem) {
					int discountAmount = ((StoreCatalogueItem) result).getDiscountAmount();
					((TextView) getView.findViewById(R.id.current_Discount)).setText("" + discountAmount);
				}
			} else {
				if (CommonLib.isNetworkAvailable(activity)) {
					Toast.makeText(activity, getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(activity, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
							.show();

				}
			}

		}
	}

}
