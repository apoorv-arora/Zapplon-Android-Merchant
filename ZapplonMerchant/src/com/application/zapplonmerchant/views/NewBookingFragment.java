package com.application.zapplonmerchant.views;

import java.util.ArrayList;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.data.UserWish;
import com.application.zapplonmerchant.receivers.ZPollingReceiver;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.UploadManager;
import com.application.zapplonmerchant.utils.UploadManagerCallback;
import com.application.zapplonmerchant.views.OrdersAdapter.OnTabActionPerformedListener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class NewBookingFragment extends Fragment implements UploadManagerCallback {

	private View getView;
	private ListView orderList;
	private ArrayList<UserWish> orderTabList;
	private RelativeLayout background_layout;
	private boolean destroyed = false;
	public static NewBookingFragment newOrderFragment;
	private Activity mContext;

	private OrdersAdapter mOrdersAdapter;
	private OnTabActionPerformedListener mCallBack;

	private UserWish tabItem;

	public static NewBookingFragment newInstance(ArrayList<UserWish> newOrderList) {

		newOrderFragment = new NewBookingFragment();
		Bundle b = new Bundle();
		b.putSerializable("orderList", newOrderList);
		newOrderFragment.setArguments(b);
		return newOrderFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getView = inflater.inflate(R.layout.new_booking_fragment, null);
		return getView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = getActivity();
		mCallBack = (OnTabActionPerformedListener) mContext;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		UploadManager.addCallback(this);
		orderList = (ListView) getView.findViewById(R.id.new_order_list);

		int width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		orderList.setPadding(0, width / 20, 0, 0);
		background_layout = (RelativeLayout) getView.findViewById(R.id.background_layout);
		Bundle bundle = getArguments();

		if (bundle != null) {
			orderTabList = bundle.containsKey("orderList") ? (ArrayList<UserWish>) bundle.get("orderList") : null;
		}

		if (orderTabList != null) {
			mOrdersAdapter = new OrdersAdapter(getActivity(), R.layout.order_list_item_snippet, orderTabList,
					OrdersAdapter.NEW_ORDER_SNIPPET, this);
			orderList.setAdapter(mOrdersAdapter);
		}
		// setupActionbar();
		changeBackgroundColor();
		updateAlarmUp = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, ZPollingReceiver.class),
				PendingIntent.FLAG_NO_CREATE);
		if (updateAlarmUp == null) {
			alarm1 = new ZPollingReceiver();
			alarm1.cancelAlarm(mContext);
			alarm1.setAlarm(mContext, CommonLib.UPDATE_TIMER);
		}
	}

	PendingIntent updateAlarmUp;
	ZPollingReceiver alarm1;

	@Override
	public void onDestroy() {
		destroyed = true;
		if (zProgressDialog != null && zProgressDialog.isShowing())
			zProgressDialog.dismiss();
		UploadManager.removeCallback(this);
		if (updateAlarmUp != null) {
			updateAlarmUp.cancel();
		}
		if (alarm1 != null)
			alarm1.cancelAlarm(mContext);
		super.onDestroy();
	}

	private void changeBackgroundColor() {
		final int DELAY = 1000;
		ColorDrawable f = new ColorDrawable(getResources().getColor(R.color.zred_feedback));
		ColorDrawable f2 = new ColorDrawable(getResources().getColor(R.color.submit_green));
		AnimationDrawable a = new AnimationDrawable();
		a.addFrame(f, DELAY);
		a.addFrame(f2, DELAY);
		a.setOneShot(false);
		background_layout.setBackgroundDrawable(a);
		a.start();
	}

	private void setupActionbar() {
		ActionBar actionBar = ((Home) mContext).getSupportActionBar();

		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setTitle("New Order");

	}

	public void removeTab(UserWish tab) {
		if (isAdded()) {
			if (orderTabList.contains(tab)) {

				orderTabList.remove(tab);
				mOrdersAdapter.notifyDataSetChanged();

				if (orderTabList.size() == 0) {
					fragmentOnDestroy();
					((Home) mContext).removeFragment(NewBookingFragment.this, 0, R.anim.slide_out_bottom);
				}

			}
		}
	}
	
	private void fragmentOnDestroy() {
		destroyed = true;
		if (zProgressDialog != null && zProgressDialog.isShowing())
			zProgressDialog.dismiss();
		UploadManager.removeCallback(this);
		if (updateAlarmUp != null) {
			updateAlarmUp.cancel();
		}
		if (alarm1 != null)
			alarm1.cancelAlarm(mContext);
	}

	public void addTab(UserWish tab) {
		if (isAdded()) {
			if (orderTabList != null && !orderTabList.contains(tab)) {
				orderTabList.add(0, tab);
				mOrdersAdapter.notifyDataSetChanged();
			}
		}
	}

	public void updateList(final ArrayList<UserWish> newTabList) {
		if (mContext != null)
			mContext.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateOnUI(newTabList);
				}
			});
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.UPDATE_TABLE_BOOKING) {
			if (!destroyed) {
				if (zProgressDialog != null && zProgressDialog.isShowing())
					zProgressDialog.dismiss();
				if (status && tabItem != null) {
					removeTab(tabItem);
				}
			}
		}
	}

	private ProgressDialog zProgressDialog;

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
		if (requestType == CommonLib.UPDATE_TABLE_BOOKING) {
			if (!destroyed) {
				zProgressDialog = ProgressDialog.show(mContext, null, "Uploading your request. Please wait!!!");
			}
		}
	}

	public void updateClickedTabItem(UserWish tabItem) {
		this.tabItem = tabItem;
	}

	public void updateOnUI(ArrayList<UserWish> newTabList) {
		if (newTabList.size() == 0) {
			// remove fragment from view

			if (isAdded()) {
				((Home) mContext).removeFragment(NewBookingFragment.this, 0, R.anim.slide_out_bottom);
				// mCallBack.onTabActionPerformed(null,
				// HomeServices.ACTION_REMOVE_NEW_ORDER_FRAGMENT);
			}

			return;
		}

		boolean added = false;
		for (UserWish newTab : newTabList) {

			int userWishId = newTab.getUserWishId();

			boolean contains = false;

			for (UserWish currentTab : orderTabList) {
				if (currentTab.getUserWishId() == userWishId) {
					contains = true;
					break;
				}
			}

			// contains will work for ZTab .equals() overrided
			if (contains) {
				// do nothing
			} else {
				// add in list
				if (orderTabList != null) {
					orderTabList.add(newTab);
					added = true;
				}
			}
		}
		if (added && mOrdersAdapter != null) {
			mOrdersAdapter.notifyDataSetChanged();
		}
	}

}