package com.application.zapplonmerchant.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.User;
import com.application.zapplonmerchant.data.UserWish;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.utils.UploadManager;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OrdersAdapter extends ArrayAdapter<UserWish> {

	// DELIVERY STATUS = 1 WHEN ORDER IS PLACED, 3 WHEN MARKED ON WAY

	private Activity mContext;
	private int width;
	private ArrayList<UserWish> feedItems;
	private ZApplication bapp;
	LayoutInflater mInflater;
	private String action;
	private float px;

	public static String NEW_ORDER_SNIPPET = "new_order";

	public static final int DELIVERY_STATUS_ENROUTE = 2;
	public static final int DELIVERY_STATUS_CARD_PAYMENT_DELIVERED = 4;
	public static final int DELIVERY_STATUS_DELIVERED = 6;
	public static final int DELIVERY_STATUS_TIMED_OUT = 7;
	public static final int DELIVERY_STATUS_REJECTED = 8;

	public static final String MOVE_TO_DELIVERY = "move_to_delivery";
	public static final String REPORT_ABUSE = "report_abuse";
	private int imageParams;
	Fragment fragment;
	private ViewHolder viewHolder;
	private OnTabActionPerformedListener mCallBack;

	public OrdersAdapter(Activity context, int resourceId, ArrayList<UserWish> objects, String action,
			Fragment fragment) {
		super(context.getApplicationContext(), resourceId, objects);
		this.mContext = context;
		this.feedItems = objects;
		this.action = action;
		px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources().getDisplayMetrics());
		width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		bapp = (ZApplication) mContext.getApplication();
		mInflater = LayoutInflater.from(context);
		imageParams = width / 10;
		this.fragment = fragment;
		try {
			mCallBack = (OnTabActionPerformedListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement OnTabActionPerformedListener");
		}
	}

	/**
	 * Activity must implement onTabActionPerformedListener to perform the below
	 * tab navigation action: 0 New Order Fragment -> Accepted Fragment, when
	 * the user accepts the order action: 1 Accepted Fragment -> Archive
	 * Fragment, when the user mark for delivery Cannot check if container
	 * activity is implementing this, as adapter is set in onPost.
	 */
	public interface OnTabActionPerformedListener {
		public void onTabActionPerformed(UserWish tab, int action);
	}

	protected class ViewHolder {
		ImageView userImage;
		TextView userName;
		TextView userContact;
		TextView restName;
		TextView date;
		TextView orderId;
		TextView accept_button;
		TextView decline_button;
	}

	@Override
	public int getCount() {
		if (feedItems == null) {
			return 0;
		} else {
			return feedItems.size();
		}
	}

	@Override
	public View getView(final int position, View v, ViewGroup parent) {

		final UserWish tabItem = feedItems.get(position);

		if (v == null) {
			viewHolder = new ViewHolder();
			v = mInflater.inflate(R.layout.order_list_item_snippet, null);
			viewHolder.userContact = (TextView) v.findViewById(R.id.user_contact);
			viewHolder.userImage = (ImageView) v.findViewById(R.id.user_image);
			viewHolder.userName = (TextView) v.findViewById(R.id.user_name);
			viewHolder.restName = (TextView) v.findViewById(R.id.rest_name);
			viewHolder.date = (TextView) v.findViewById(R.id.time);
			viewHolder.orderId = (TextView) v.findViewById(R.id.order_id);
			viewHolder.accept_button = (TextView) v.findViewById(R.id.accept_button);
			viewHolder.decline_button = (TextView) v.findViewById(R.id.decline_button);

			v.setPadding(width / 40, width / 40, width / 40, width / 40);
			v.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) v.getTag();
		}

		User userObject = tabItem.getUser();

		// user name
		if (userObject != null) {
			StringBuilder builder = new StringBuilder();
			builder.append("Booked by " + userObject.getUserName());
			if (tabItem.getCrowd() > 1)
				builder.append(" + " + (tabItem.getCrowd() - 1) + " more");
			viewHolder.userName.setText(builder.toString());
		}

		// date
		StringBuilder dateStr = new StringBuilder();
		dateStr.append("Reservation date: ");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(tabItem.getStartDate());
		int endYear = calendar.get(Calendar.YEAR);
		int endMonthOfYear = calendar.get(Calendar.MONTH);
		int endDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		SimpleDateFormat sdf = new SimpleDateFormat("EEE dd, MMM yy", Locale.getDefault());
		GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());
		cal.set(endYear, endMonthOfYear, endDayOfMonth, 23, 59);
		dateStr.append(sdf.format(cal.getTime()) + "\n");

		int startHour = calendar.get(Calendar.HOUR_OF_DAY);
		int startMinute = calendar.get(Calendar.MINUTE);

		calendar.setTimeInMillis(tabItem.getEndDate());
		int endHour = calendar.get(Calendar.HOUR_OF_DAY);
		int endMinute = calendar.get(Calendar.MINUTE);

		Time mTime = new Time();
		mTime.set(0, startMinute, startHour, 1, 1, 1);
		String startTime = mTime.format("%I:%M %P");

		String endTime = "";
		String formatted = "";
		if (tabItem.getEndDate() > 0) {
			mTime.set(0, endMinute, endHour, 1, 1, 1);
			endTime = mTime.format("%I:%M %P");
			formatted = startTime + " - " + endTime;
			// eventDetails.setEndTime(endHour + ":" + endMinute
			// + ":00");

		} else {
			formatted = startTime + " " + "Onwards";
		}
		dateStr.append(formatted);

		viewHolder.date.setText(dateStr.toString());

		viewHolder.orderId.setText("Order Id: " + tabItem.getUserWishId());

		if (tabItem.getStore() != null) {
			viewHolder.restName.setText("" + tabItem.getStore().getStoreName());
		}

		viewHolder.accept_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (NewBookingFragment.newOrderFragment != null)
					NewBookingFragment.newOrderFragment.updateClickedTabItem(tabItem);
				UploadManager.updateTableBookingStatus(tabItem.getUserWishId(), CommonLib.TABLE_BOOKING_ACTION_ACCEPT,
						"");
			}
		});

		viewHolder.decline_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (NewBookingFragment.newOrderFragment != null)
					NewBookingFragment.newOrderFragment.updateClickedTabItem(tabItem);
				UploadManager.updateTableBookingStatus(tabItem.getUserWishId(), CommonLib.TABLE_BOOKING_ACTION_REJECT,
						"");
			}
		});

		return v;
	}

	public void refreshList(ArrayList<UserWish> objects) {
		this.feedItems = objects;
		notifyDataSetChanged();
	}

}