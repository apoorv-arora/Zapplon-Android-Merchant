package com.application.zapplonmerchant.services;

import java.util.ArrayList;

import com.application.zapplonmerchant.data.UserWish;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.RequestWrapper;
import com.application.zapplonmerchant.views.Home;
import com.application.zapplonmerchant.views.NewBookingFragment;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

public class ZNewBookingService extends IntentService {

	public ZNewBookingService() {
		super("ZNewBookingService");
		CommonLib.ZLog("here", "inside service constructor");
	}

	@Override
	protected void onHandleIntent(Intent serviceIntent) {

		CommonLib.ZLog("here", "inside on handle intent");
		SharedPreferences prefs = getSharedPreferences(CommonLib.APP_SETTINGS, 0);
		if (!prefs.getString("access_token", "").equals("")) {

			String url = CommonLib.SERVER + "tablebooking/poll?" + "store_id=" + prefs.getInt("store_id", 0)
					+ CommonLib.getVersionString(getApplicationContext());
			CommonLib.ZLog("url ", url);

			Object response = RequestWrapper.RequestHttp(url, RequestWrapper.BOOKING_LIST, RequestWrapper.FAV);

			if (response != null && response instanceof Object[]) {

				Object[] arr = (Object[]) response;
				int mWishesTotalCount = (Integer) arr[0];
				ArrayList<UserWish> zTabs = (ArrayList<UserWish>) arr[1];

				if (zTabs != null && zTabs.size() > 0)
					launchNewOrderFragment(zTabs);
				else if (zTabs != null && zTabs.size() == 0) {
					if (NewBookingFragment.newOrderFragment != null && NewBookingFragment.newOrderFragment.isAdded()) {
						CommonLib.ZLog("xxPoller", "onNewIntent updateTabs");
						NewBookingFragment.newOrderFragment.updateList(zTabs);
					}
				}

			}
		}
		stopForeground(true);
	}

	private void launchNewOrderFragment(ArrayList<UserWish> tabList) {
		CommonLib.ZLog("xxurl", "launching Home from Polling");
		Intent intent = new Intent(getApplicationContext(), Home.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("fragment", "NewOrderFragment");
		intent.putExtra("orderList", tabList);
		startActivity(intent);
	}

}
