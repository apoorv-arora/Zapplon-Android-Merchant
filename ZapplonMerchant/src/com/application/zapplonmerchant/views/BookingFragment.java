package com.application.zapplonmerchant.views;

import java.util.ArrayList;
import java.util.List;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.Store;
import com.application.zapplonmerchant.data.UserWish;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.RequestWrapper;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.UploadManagerCallback;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class BookingFragment extends Fragment implements UploadManagerCallback {

	private ZApplication zapp;
	private Activity activity;
	private View getView;
	private SharedPreferences prefs;
	private int width, height;
	private LayoutInflater vi;

	public static final int TYPE_SALON = 0;
	public static final int TYPE_SPA = 1;
	public static final int TYPE_RESTAURANT = 2;

	private AsyncTask mAsyncRunning;
	private Activity mContext;
	private BookingsAdapter mAdapter;
	private LayoutInflater inflater;
	private boolean destroyed = false;

	// Load more part
	private ListView mListView;
	private ArrayList<UserWish> wishes;
	private LinearLayout mListViewFooter;
	private int mWishesTotalCount;
	private boolean cancelled = false;
	private boolean loading = false;
	private int count = 10;

	private ProgressDialog zProgressDialog;
	int storeId;
	private boolean acceptsReservation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();
		getView = getView();
		mContext = activity;

		prefs = activity.getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) activity.getApplication();
		width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
		vi = LayoutInflater.from(activity.getApplicationContext());
		UploadManager.addCallback(this);
		init();
	}
	
	public void init() {
		storeId = prefs.getInt("selected_store_id", 0);
		acceptsReservation = prefs.getBoolean("accepts_reservation", false);
		if (acceptsReservation)
			setUpHomeViews();
		else {
			getView.findViewById(R.id.wishbox_progress_container).setVisibility(View.GONE);
			getView.findViewById(R.id.content).setVisibility(View.GONE);
			getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
			((TextView) getView.findViewById(R.id.empty_view_text))
					.setText("This store does not accepts Table Reservation");
		}
	}

	private void setUpHomeViews() {
		mListView = (ListView) getView.findViewById(R.id.deals_list);
		mListView.setDivider(null);
		mListView.setDividerHeight(0);
		if (storeId != 0) {
			refreshView();
			new GetStoresList(false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
		} else {
			new GetStoresList(true).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
		}
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

	public void scrollHomeToTop() {
		try {
			if (mListView != null)
				mListView.setSelection(0);
		} catch (Exception e) {
		}
	}

	void refreshView() {
		if (mAsyncRunning != null)
			mAsyncRunning.cancel(true);
		mAsyncRunning = new GetDeals().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class GetStoresList extends AsyncTask<Object, Void, Object> {

		private boolean initializeOnly = false;

		public GetStoresList(boolean initializeOnly) {
			this.initializeOnly = initializeOnly;
		}

		@Override
		protected void onPreExecute() {
			if (initializeOnly) {
				getView.findViewById(R.id.wishbox_progress_container).setVisibility(View.VISIBLE);

				getView.findViewById(R.id.content).setAlpha(1f);

				getView.findViewById(R.id.content).setVisibility(View.GONE);

				getView.findViewById(R.id.empty_view).setVisibility(View.GONE);
			}
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "store/all";
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.STORES_LIST, RequestWrapper.FAV);
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

			if (result != null) {
				if (result instanceof ArrayList<?> && activity instanceof Home) {
					((Home) activity).storesList = (ArrayList<Store>) result;
					if (initializeOnly) {
						((Home) activity).refreshViews(((Home) activity).storesList.get(0));
						refreshView();
					}
					// refresh activity views
				} else {
					String accessToken = prefs.getString("access_token", "");
					UploadManager.logout(accessToken);

					Editor editor = prefs.edit();
					editor.putInt("uid", 0);
					editor.putInt("merchant_id", 0);
					editor.putInt("availability", 0);
					editor.putInt("store_id", 0);
					editor.putString("thumbUrl", "");
					editor.putString("access_token", "");
					editor.remove("username");
					editor.remove("support_contact");
					editor.remove("profile_pic");
					editor.remove("HSLogin");
					editor.remove("INSTITUTION_NAME");
					editor.remove("STUDENT_ID");
					editor.putBoolean("facebook_post_permission", false);
					editor.putBoolean("post_to_facebook_flag", false);
					editor.putBoolean("facebook_connect_flag", false);
					editor.putBoolean("twitter_status", false);

					editor.commit();

					if (prefs.getInt("uid", 0) == 0) {
						Intent intent = new Intent(zapp, SplashScreen.class);
						startActivity(intent);
						activity.finish();
					}
				}
			} else {
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
							.show();

					getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					getView.findViewById(R.id.content).setVisibility(View.GONE);
				}
			}

		}
	}

	private class GetDeals extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			getView.findViewById(R.id.wishbox_progress_container).setVisibility(View.VISIBLE);

			getView.findViewById(R.id.content).setAlpha(1f);

			getView.findViewById(R.id.content).setVisibility(View.GONE);

			getView.findViewById(R.id.empty_view).setVisibility(View.GONE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "tablebooking/list?start=0&count=" + count + "&store_id=" + storeId;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.BOOKING_LIST, RequestWrapper.FAV);
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

			getView.findViewById(R.id.wishbox_progress_container).setVisibility(View.GONE);

			if (result != null) {
				getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
				if (result instanceof Object[]) {
					Object[] arr = (Object[]) result;
					mWishesTotalCount = (Integer) arr[0];
					setWishes((ArrayList<UserWish>) arr[1]);
				}
			} else {
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
							.show();

					getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					getView.findViewById(R.id.content).setVisibility(View.GONE);
				}
			}

		}
	}

	public class BookingsAdapter extends ArrayAdapter<UserWish> {

		private List<UserWish> wishes;
		private Activity mContext;
		private int width;

		public BookingsAdapter(Activity context, int resourceId, List<UserWish> wishes) {
			super(context.getApplicationContext(), resourceId, wishes);
			mContext = context;
			this.wishes = wishes;
			width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		}

		@Override
		public int getCount() {
			if (wishes == null) {
				return 0;
			} else {
				return wishes.size();
			}
		}

		protected class ViewHolder {
			TextView description;
			TextView time;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final UserWish wish = wishes.get(position);
			if (v == null || v.findViewById(R.id.store_item_root) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.booking_list_item_layout, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.description = (TextView) v.findViewById(R.id.description);
				viewHolder.time = (TextView) v.findViewById(R.id.start_time);
				v.setTag(viewHolder);
			}

			((LinearLayout.LayoutParams) v.findViewById(R.id.store_item_inner_root).getLayoutParams())
					.setMargins(width / 40, width / 40, width / 40, width / 40);

			viewHolder.description.setText(CommonLib.getTableBookingDetails(wish));

			return v;
		}

	}

	// set all the wishes here
	private void setWishes(ArrayList<UserWish> wishes) {
		this.wishes = wishes;
		if (wishes != null && wishes.size() > 0 && mWishesTotalCount > wishes.size()
				&& mListView.getFooterViewsCount() == 0) {
			mListViewFooter = new LinearLayout(activity.getApplicationContext());
			mListViewFooter.setBackgroundResource(R.color.white);
			mListViewFooter.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, width / 5));
			mListViewFooter.setGravity(Gravity.CENTER);
			mListViewFooter.setOrientation(LinearLayout.HORIZONTAL);
			ProgressBar pbar = new ProgressBar(activity.getApplicationContext(), null,
					android.R.attr.progressBarStyleSmallInverse);
			mListViewFooter.addView(pbar);
			pbar.setTag("progress");
			pbar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mListView.addFooterView(mListViewFooter);
		}
		mAdapter = new BookingsAdapter(mContext, R.layout.store_catalogue_item_snippet, this.wishes);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount - 1 < mWishesTotalCount
						&& !loading && mListViewFooter != null) {
					if (mListView.getFooterViewsCount() == 1) {
						loading = true;
						new LoadModeWishes().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, totalItemCount - 1);
					}
				} else if (totalItemCount - 1 == mWishesTotalCount && mListView.getFooterViewsCount() > 0) {
					mListView.removeFooterView(mListViewFooter);
				}
			}
		});
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.REDEEM_COUPON) {
			if (!destroyed) {
				if (zProgressDialog != null && zProgressDialog.isShowing()) {
					zProgressDialog.dismiss();
				}
				if (status) {
				}
			}
		} else if (requestType == CommonLib.PROMO_CREATION) {
			if (!destroyed) {
				if (zProgressDialog != null && zProgressDialog.isShowing()) {
					zProgressDialog.dismiss();
				}
				refreshView();
			}
		} else if (requestType == CommonLib.PROMO_UPDATE) {
			if (!destroyed) {
				if (zProgressDialog != null && zProgressDialog.isShowing()) {
					zProgressDialog.dismiss();
				}
				refreshView();
			}
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

	private class LoadModeWishes extends AsyncTask<Integer, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Integer... params) {
			int start = params[0];
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "tablebooking/list?start=" + start + "&count=" + count + "&store_id="
						+ storeId;
				;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.BOOKING_LIST, RequestWrapper.FAV);
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
			if (result != null && result instanceof Object[]) {
				Object[] arr = (Object[]) result;
				wishes.addAll((ArrayList<UserWish>) arr[1]);
				mAdapter.notifyDataSetChanged();
			}
			loading = false;
		}
	}
}
