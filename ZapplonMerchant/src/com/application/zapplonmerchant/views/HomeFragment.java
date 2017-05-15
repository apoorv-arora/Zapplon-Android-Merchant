package com.application.zapplonmerchant.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.DealData;
import com.application.zapplonmerchant.data.Store;
import com.application.zapplonmerchant.data.StoreCatalogueItem;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.RequestWrapper;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.UploadManagerCallback;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
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

public class HomeFragment extends Fragment implements UploadManagerCallback {

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
	private WishesAdapter mAdapter;
	private LayoutInflater inflater;
	private boolean destroyed = false;

	// Load more part
	private ListView mListView;
	private ArrayList<StoreCatalogueItem> wishes;
	private LinearLayout mListViewFooter;
	private int mWishesTotalCount;
	private boolean cancelled = false;
	private boolean loading = false;
	private int count = 10;

	private ProgressDialog zProgressDialog;
	int storeId;

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
		storeId = prefs.getInt("selected_store_id", 0);
		UploadManager.addCallback(this);
		setUpHomeViews();
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
			if(initializeOnly) {
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
					if(initializeOnly) {
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
				url = CommonLib.SERVER + "store/list?start=0&count=" + count + "&store_id=" + storeId;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.DEALS_LIST, RequestWrapper.FAV);
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
					setWishes((ArrayList<StoreCatalogueItem>) arr[1]);
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

	public class WishesAdapter extends ArrayAdapter<StoreCatalogueItem> {

		private List<StoreCatalogueItem> wishes;
		private Activity mContext;
		private int width;

		public WishesAdapter(Activity context, int resourceId, List<StoreCatalogueItem> wishes) {
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
			TextView edit, delete;
			TextView description;
			TextView count;
			TextView type;
			TextView active;
			TextView time;
			TextView endDate;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final StoreCatalogueItem wish = wishes.get(position);
			if (v == null || v.findViewById(R.id.store_item_root) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.store_catalogue_item_snippet, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.edit = (TextView) v.findViewById(R.id.edit);
				viewHolder.delete = (TextView) v.findViewById(R.id.delete);
				viewHolder.description = (TextView) v.findViewById(R.id.description);
				viewHolder.type = (TextView) v.findViewById(R.id.type);
				viewHolder.count = (TextView) v.findViewById(R.id.count);
				viewHolder.active = (TextView) v.findViewById(R.id.active);
				viewHolder.time = (TextView) v.findViewById(R.id.start_time);
				viewHolder.endDate = (TextView) v.findViewById(R.id.end_time);
				v.setTag(viewHolder);
			}

			((LinearLayout.LayoutParams) v.findViewById(R.id.store_item_inner_root).getLayoutParams())
					.setMargins(width / 40, width / 40, width / 40, width / 40);

			viewHolder.description.setText(CommonLib.getStoreItemDescription(wish));
			viewHolder.type.setText(CommonLib.getDealType(mContext, wish.getDealType()));
			if (wish.getDealType() != CommonLib.DEAL_TYPE_1 && wish.getDealType() != CommonLib.DEAL_TYPE_4) {
				viewHolder.count.setText("Remaining: " + wish.getCount());
				viewHolder.count.setVisibility(View.VISIBLE);
			} else
				viewHolder.count.setVisibility(View.GONE);

			Time mTime = new Time();
			Date openingTime = new Date(System.currentTimeMillis());
			openingTime.setHours(wish.getStartingHour());
			openingTime.setMinutes(wish.getStartingMin());
			Date closingTime = new Date(System.currentTimeMillis());
			closingTime.setHours(wish.getEndingHour());
			closingTime.setMinutes(wish.getEndingMin());
			mTime.set(0, openingTime.getMinutes(), openingTime.getHours(), 1, 1, 1);
			String startTime = mTime.format("%I:%M %P");
			String endTime = "";
			String formattedTime = "";
			mTime.set(0, closingTime.getMinutes(), closingTime.getHours(), 1, 1, 1);
			endTime = mTime.format("%I:%M %P");
			formattedTime = startTime + " - " + endTime;
			viewHolder.time.setText(formattedTime);

			Date endDate = new Date(wish.getEndTime());
			SimpleDateFormat sdf = new SimpleDateFormat("EEE dd, MMM yy", Locale.getDefault());
			GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());

			cal.set(endDate.getYear(), endDate.getMonth(), endDate.getDate());
			sdf.setCalendar(cal);
			String formatted = sdf.format(cal.getTime());
			viewHolder.endDate.setText("Till " + formatted);

			viewHolder.active.setText(CommonLib.getStoreItemStatusString(wish.getStatus()));
			viewHolder.active.setTextColor(CommonLib.getStoreItemStatusColor(mContext, wish.getStatus()));

			viewHolder.edit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), CreatePromoScreen.class);
					intent.putExtra("edit", true);
					intent.putExtra("wish", wish);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			});

			viewHolder.delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final AlertDialog logoutDialog;
					logoutDialog = new AlertDialog.Builder(getActivity())
							.setMessage(getResources().getString(R.string.are_you_sure_delete_deal))
							.setPositiveButton(getResources().getString(R.string.delete),
									new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							zProgressDialog = ProgressDialog.show(mContext, null,
									getResources().getString(R.string.deleting_deal_progress));

							DealData dealData = new DealData();
							dealData.setDealType(wish.getDealType());
							dealData.setDealSubType(wish.getDealSubType());
							dealData.setDiscountAmount(wish.getDiscountAmount());
							dealData.setMinOrder(wish.getMinOrder());
							dealData.setMaxOrder(wish.getMaxOrder());
							dealData.setCount(wish.getCount());
							dealData.setProductName(wish.getProductName());
							dealData.setDiscountSecondAmount(wish.getDiscountAmount());
							dealData.setDealOpeningHour(wish.getStartingHour());
							dealData.setDealClosingHour(wish.getEndingHour());
							dealData.setDealOpeningMin(wish.getStartingMin());
							dealData.setDealClosingMin(wish.getEndingMin());
							dealData.setStoreItemId(wish.getStoreItemId());
							dealData.setAction(CommonLib.STORE_ITEM_ACTION_DELETE);
							UploadManager.updateStoreItem(dealData);
						}
					}).setNegativeButton(getResources().getString(R.string.dialog_cancel),
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create();
					logoutDialog.show();
				}
			});

			if (wish.getDealType() == CommonLib.DEAL_TYPE_1 || wish.getDealType() == CommonLib.DEAL_TYPE_4) {
				viewHolder.edit.setVisibility(View.GONE);
				viewHolder.delete.setVisibility(View.GONE);
			}
			return v;
		}

	}

	// set all the wishes here
	private void setWishes(ArrayList<StoreCatalogueItem> wishes) {
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
		mAdapter = new WishesAdapter(mContext, R.layout.store_catalogue_item_snippet, this.wishes);
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
				url = CommonLib.SERVER + "store/list?start=" + start + "&count=" + count;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.DEALS_LIST, RequestWrapper.FAV);
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
				wishes.addAll((ArrayList<StoreCatalogueItem>) arr[1]);
				mAdapter.notifyDataSetChanged();
			}
			loading = false;
		}
	}
}
