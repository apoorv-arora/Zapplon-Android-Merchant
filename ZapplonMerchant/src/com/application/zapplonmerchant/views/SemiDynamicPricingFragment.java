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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SemiDynamicPricingFragment extends Fragment implements UploadManagerCallback {

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
		return inflater.inflate(R.layout.fragment_semi_dynamic_pricing, null);
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

		getView.findViewById(R.id.full).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonSelected(CommonLib.DP_ACTION_FULL);

				new UpdateOccupancy().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object[] { CommonLib.DP_ACTION_FULL });
				UploadManager.updateAvailability(0);
			}
		});

		getView.findViewById(R.id.slow).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonSelected(CommonLib.DP_ACTION_SLOW);

				new UpdateOccupancy().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object[] { CommonLib.DP_ACTION_SLOW });
				UploadManager.updateAvailability(1);
			}
		});

		getView.findViewById(R.id.fast).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonSelected(CommonLib.DP_ACTION_FAST);

				new UpdateOccupancy().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object[] { CommonLib.DP_ACTION_FAST });
				UploadManager.updateAvailability(1);
			}
		});

		getView.findViewById(R.id.moderate).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonSelected(CommonLib.DP_ACTION_MODERATE);

				new UpdateOccupancy().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object[] { CommonLib.DP_ACTION_MODERATE });
				UploadManager.updateAvailability(1);
			}
		});
	}

	void setUpHomeViews() {

	}

	public void scrollSearchToTop() {
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

		private int action;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			action = (Integer) params[0];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("store_id", prefs.getInt("store_id", 0) + ""));
			nameValuePairs.add(new BasicNameValuePair("action", "" + action));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "dynamicPricing/update?", nameValuePairs,
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
				editor.putInt("action_mode", action).commit();
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
				url = CommonLib.SERVER + "dynamicPricing/currentDiscount?store_id=" + prefs.getInt("store_id", 0);
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
					refreshViews(((StoreCatalogueItem) result));
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

	private void refreshViews(StoreCatalogueItem storeItem) {
		if (storeItem == null)
			return;

		int discountAmount = storeItem.getDiscountAmount();
		((TextView) getView.findViewById(R.id.current_Discount)).setText("" + discountAmount);
		buttonSelected(storeItem.getActionMode());

	}

	private void buttonSelected(int action) {
		switch (action) {
		case CommonLib.DP_ACTION_FAST:
			getView.findViewById(R.id.full)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			getView.findViewById(R.id.slow)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			getView.findViewById(R.id.moderate)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			getView.findViewById(R.id.fast)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_button_feedback));
			break;
		case CommonLib.DP_ACTION_FULL:
			getView.findViewById(R.id.full)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_button_feedback));
			getView.findViewById(R.id.slow)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			getView.findViewById(R.id.moderate)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			getView.findViewById(R.id.fast)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			break;
		case CommonLib.DP_ACTION_MODERATE:
			getView.findViewById(R.id.full)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			getView.findViewById(R.id.slow)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			getView.findViewById(R.id.moderate)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_button_feedback));
			getView.findViewById(R.id.fast)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			break;
		case CommonLib.DP_ACTION_SLOW:
			getView.findViewById(R.id.full)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			getView.findViewById(R.id.slow)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_button_feedback));
			getView.findViewById(R.id.moderate)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			getView.findViewById(R.id.fast)
					.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_button_feedback));
			break;
		}
	}

}
