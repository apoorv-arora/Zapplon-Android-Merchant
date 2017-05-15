package com.application.zapplonmerchant.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.DealData;
import com.application.zapplonmerchant.data.StoreCatalogueItem;
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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class CreatePromoScreen extends ActionBarActivity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private int width;
	private ZApplication zapp;
	private Activity mContext;
	private boolean destroyed = false;
	private ProgressDialog zProgressDialog;
	private final ArrayList<String> dealTypes = new ArrayList<String>();
	public static final int DIALOG_TYPE_PICKER = 101;
	private LayoutInflater inflater;

	private boolean isEditable = false;
	private StoreCatalogueItem wishEditableItem;

	Date openingTime, closingTime;
	Date endTime;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_promo_layout);
		prefs = getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) getApplication();
		width = getWindowManager().getDefaultDisplay().getWidth();

		mContext = this;
		inflater = LayoutInflater.from(getApplicationContext());
		setUpActionBar();
		UploadManager.addCallback(this);

		if (getIntent() != null && getIntent().hasExtra("edit"))
			isEditable = getIntent().getBooleanExtra("edit", false);

		findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String type = ((TextView) findViewById(R.id.type)).getText().toString();
				if (type == null || type.length() == 0) {
					Toast.makeText(mContext, getResources().getString(R.string.invalid_deal_type), Toast.LENGTH_LONG)
							.show();
					return;
				}

				String coupons_count = ((TextView) findViewById(R.id.coupons_count)).getText().toString();
				if (coupons_count == null || coupons_count.length() == 0) {
					Toast.makeText(mContext, getResources().getString(R.string.invalid_coupons_count),
							Toast.LENGTH_LONG).show();
					return;
				}

				String productName = ((TextView) findViewById(R.id.product_value)).getText().toString();

				String max_order = ((TextView) findViewById(R.id.max_order)).getText().toString();

				int dealType = 0;
				if (type.equals(mContext.getResources().getString(R.string.deal_1))) {
					dealType = 1;
				} else if (type.equals(mContext.getResources().getString(R.string.deal_2))) {
					dealType = 2;
				} else if (type.equals(mContext.getResources().getString(R.string.deal_3))) {
					dealType = 3;
				}

				String discount_value, discount_value_other = "";
				if (dealType == 2) {
					discount_value = ((TextView) findViewById(R.id.discount_value)).getText().toString();
					discount_value_other = ((TextView) findViewById(R.id.discount_second_value)).getText().toString();
					if ((discount_value == null || discount_value.length() == 0)
							&& (discount_value_other == null || discount_value_other.length() == 0)) {
						Toast.makeText(mContext, getResources().getString(R.string.invalid_discount_percentage),
								Toast.LENGTH_LONG).show();
						return;
					}
				} else {
					discount_value = ((TextView) findViewById(R.id.discount_value)).getText().toString();
					if (discount_value == null || discount_value.length() == 0) {
						Toast.makeText(mContext, getResources().getString(R.string.invalid_discount_percentage),
								Toast.LENGTH_LONG).show();
						return;
					}
				}

				String min_order_value = ((TextView) findViewById(R.id.min_order_value)).getText().toString();
				if (dealType == 2 && (min_order_value == null || min_order_value.length() == 0)) {
					Toast.makeText(mContext, getResources().getString(R.string.invalid_minimum_order),
							Toast.LENGTH_LONG).show();
					return;
				}

				if (dealType == 3 && (productName == null || productName.length() == 0)) {
					Toast.makeText(mContext, getResources().getString(R.string.invalid_product_name), Toast.LENGTH_LONG)
							.show();
					return;
				}

				String end_date_text = ((TextView) findViewById(R.id.end_date_text)).getText().toString();
				if (end_date_text == null || end_date_text.length() == 0) {
					Toast.makeText(mContext, getResources().getString(R.string.invalid_date), Toast.LENGTH_LONG).show();
					return;
				}

				String time_picker = ((TextView) findViewById(R.id.time_picker)).getText().toString();
				if (time_picker == null || time_picker.length() == 0) {
					Toast.makeText(mContext, getResources().getString(R.string.invalid_time), Toast.LENGTH_LONG).show();
					return;
				}

				int dealSubType = 0;
				if (((RadioButton) findViewById(R.id.radio_flat)).isChecked())
					dealSubType = 1;
				else if (((RadioButton) findViewById(R.id.radio_amount)).isChecked())
					dealSubType = 2;

				int discountValue = Integer.parseInt(discount_value);

				int discountSecondValue = 0, minOrderValue = 0, maxOrderValue = 0;

				try {
					discountSecondValue = Integer.parseInt(discount_value_other);
					minOrderValue = Integer.parseInt(min_order_value);
					if (max_order != null && max_order.length() > 0 && dealSubType == 2) {
						maxOrderValue = Integer.parseInt(max_order);
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				if (dealSubType == 1 && ((discountValue > 100 || discountValue < 0)
						|| (discountSecondValue > 100 || discountSecondValue < 0))) {

				}

				int couponCount = Integer.parseInt(coupons_count);

				long startTime = openingTime.getTime();
				long endingTime = closingTime.getTime();

				int dealOpeningHour, dealClosingHour, dealOpeningMin, dealClosingMin;
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(startTime);
				dealOpeningHour = calendar.get(Calendar.HOUR_OF_DAY);
				dealOpeningMin = calendar.get(Calendar.MINUTE);

				calendar.setTimeInMillis(endingTime);
				dealClosingHour = calendar.get(Calendar.HOUR_OF_DAY);
				dealClosingMin = calendar.get(Calendar.MINUTE);
				
				zProgressDialog = ProgressDialog.show(mContext, null,
						getResources().getString(R.string.request_upload_content));

				if (isEditable) {
					DealData dealData = new DealData();
					dealData.setDealType(dealType);
					dealData.setDealSubType(dealSubType);
					dealData.setDiscountAmount(discountValue);
					dealData.setMinOrder(minOrderValue);
					dealData.setMaxOrder(maxOrderValue);
					dealData.setCount(couponCount);
					dealData.setProductName(productName);
					dealData.setDiscountSecondAmount(discountSecondValue);
					dealData.setDealOpeningHour(dealOpeningHour);
					dealData.setDealClosingHour(dealClosingHour);
					dealData.setDealOpeningMin(dealOpeningMin);
					dealData.setDealClosingMin(dealClosingMin);
					dealData.setEndTime(endTime.getTime());
					dealData.setStoreItemId(wishEditableItem.getStoreItemId());
					dealData.setAction(CommonLib.STORE_ITEM_ACTION_EDIT);
					UploadManager.updateStoreItem(dealData);
				} else {
					DealData dealData = new DealData();
					dealData.setDealType(dealType);
					dealData.setDealSubType(dealSubType);
					dealData.setDiscountAmount(discountValue);
					dealData.setMinOrder(minOrderValue);
					dealData.setMaxOrder(maxOrderValue);
					dealData.setCount(couponCount);
					dealData.setProductName(productName);
					dealData.setEndTime(endTime.getTime());
					dealData.setDiscountSecondAmount(discountSecondValue);
					dealData.setDealOpeningHour(dealOpeningHour);
					dealData.setDealClosingHour(dealClosingHour);
					dealData.setDealOpeningMin(dealOpeningMin);
					dealData.setDealClosingMin(dealClosingMin);
					UploadManager.addCoupon(dealData);
				}
				CommonLib.hideKeyBoard(CreatePromoScreen.this, findViewById(R.id.type));
			}
		});

		// dealTypes.add(mContext.getResources().getString(R.string.deal_1));
		dealTypes.add(mContext.getResources().getString(R.string.deal_2));
		dealTypes.add(mContext.getResources().getString(R.string.deal_3));

		findViewById(R.id.type).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mContext != null) {
					Bundle args = new Bundle();
					args.putSerializable("list_items", dealTypes);
					showPopupDialog(StoreDetailsScreen.DIALOG_TYPE_PICKER, args);
				}
			}
		});

		if (isEditable) {// existing deal

			wishEditableItem = (StoreCatalogueItem) getIntent().getSerializableExtra("wish");

			// set date
			openingTime = new Date(System.currentTimeMillis());
			openingTime.setHours(wishEditableItem.getStartingHour());
			openingTime.setMinutes(wishEditableItem.getStartingMin());
			endTime = new Date(wishEditableItem.getEndTime());
			closingTime = new Date(System.currentTimeMillis());
			closingTime.setHours(wishEditableItem.getEndingHour());
			closingTime.setMinutes(wishEditableItem.getEndingMin());

			// set date text
			SimpleDateFormat sdf = new SimpleDateFormat("EEE dd, MMM yy", Locale.getDefault());
			GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());

			cal.set(endTime.getYear(), endTime.getMonth(), endTime.getDate());
			sdf.setCalendar(cal);
			String formatted = sdf.format(cal.getTime());
			((TextView) findViewById(R.id.date_picker)).setText(formatted);

			// set time text
			Time mTime = new Time();
			mTime.set(0, openingTime.getMinutes(), openingTime.getHours(), 1, 1, 1);
			String startTime = mTime.format("%I:%M %P");
			String endTime = "";
			String formattedTime = "";
			mTime.set(0, closingTime.getMinutes(), closingTime.getHours(), 1, 1, 1);
			endTime = mTime.format("%I:%M %P");
			formattedTime = startTime + " - " + endTime;
			((TextView) findViewById(R.id.time_picker)).setText(formattedTime);

			// deal type
			((TextView) findViewById(R.id.type)).setText(CommonLib.getDealType(this, wishEditableItem.getDealType()));

			// deal sub type
			switch (wishEditableItem.getDealSubType()) {
			case CommonLib.DEAL_SUB_TYPE_FLAT: {
				((RadioButton) findViewById(R.id.radio_flat)).setChecked(true);
				((RadioButton) findViewById(R.id.radio_amount)).setChecked(false);

				// flat on click
				((TextView) findViewById(R.id.discount))
						.setText(getResources().getString(R.string.discount_percentage));
				((TextView) findViewById(R.id.discount_value))
						.setHint(getResources().getString(R.string.discount_percentage_hint));
				findViewById(R.id.max_order_header).setVisibility(View.VISIBLE);
				findViewById(R.id.max_order).setVisibility(View.VISIBLE);

				break;
			}
			case CommonLib.DEAL_SUB_TYPE_AMOUNT: {
				((RadioButton) findViewById(R.id.radio_flat)).setChecked(false);
				((RadioButton) findViewById(R.id.radio_amount)).setChecked(true);

				// amount on click
				((TextView) findViewById(R.id.discount)).setText(getResources().getString(R.string.discount_amount));
				((TextView) findViewById(R.id.discount_value))
						.setHint(getResources().getString(R.string.discount_amount_hint));
				findViewById(R.id.max_order_header).setVisibility(View.GONE);
				findViewById(R.id.max_order).setVisibility(View.GONE);

				break;
			}
			}

			if (wishEditableItem.getDealType() == CommonLib.DEAL_TYPE_3) {
				((TextView) findViewById(R.id.product_value)).setText(wishEditableItem.getProductName());
				findViewById(R.id.product_value).setVisibility(View.VISIBLE);
				findViewById(R.id.product).setVisibility(View.VISIBLE);

				findViewById(R.id.radio_amount).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.discount)).setText(getResources().getString(R.string.details));
				((TextView) findViewById(R.id.discount_value)).setHint(getResources().getString(R.string.details));

				findViewById(R.id.discount_second).setVisibility(View.GONE);
				findViewById(R.id.discount_second_value).setVisibility(View.GONE);

				findViewById(R.id.min_order_header).setVisibility(View.GONE);
				findViewById(R.id.min_order_value).setVisibility(View.GONE);
			} else if (wishEditableItem.getDealType() == CommonLib.DEAL_TYPE_2) {
				findViewById(R.id.product_value).setVisibility(View.GONE);
				findViewById(R.id.product).setVisibility(View.GONE);

				findViewById(R.id.product).setVisibility(View.GONE);
				findViewById(R.id.product_value).setVisibility(View.GONE);
				findViewById(R.id.radio_amount).setVisibility(View.GONE);

				// radio flat options
				((TextView) findViewById(R.id.discount))
						.setText(getResources().getString(R.string.discount_percentage));
				((TextView) findViewById(R.id.discount_value))
						.setHint(getResources().getString(R.string.discount_percentage_hint));

				findViewById(R.id.discount_second).setVisibility(View.VISIBLE);
				findViewById(R.id.discount_second_value).setVisibility(View.VISIBLE);

				((TextView) findViewById(R.id.discount)).setText(getResources().getString(R.string.discount_food));

				((TextView) findViewById(R.id.discount_value))
						.setHint(getResources().getString(R.string.discount_food));

				((TextView) findViewById(R.id.discount_second))
						.setText(getResources().getString(R.string.discount_drinks));

				((TextView) findViewById(R.id.discount_second_value))
						.setHint(getResources().getString(R.string.discount_drinks));

				findViewById(R.id.min_order_header).setVisibility(View.VISIBLE);
				findViewById(R.id.min_order_value).setVisibility(View.VISIBLE);
			}

			((TextView) findViewById(R.id.discount_value)).setText("" + wishEditableItem.getDiscountAmount());
			((TextView) findViewById(R.id.discount_second_value))
					.setText("" + wishEditableItem.getDiscountSecondAmount());
			((TextView) findViewById(R.id.min_order_value)).setText("" + wishEditableItem.getMinOrder());
			((TextView) findViewById(R.id.max_order)).setText("" + wishEditableItem.getMaxOrder());
			((TextView) findViewById(R.id.coupons_count)).setText("" + wishEditableItem.getCount());

			// current time
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getDefault());
			calendar.setTimeInMillis(wishEditableItem.getEndTime());
			String endDate = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
			((TextView) findViewById(R.id.end_date_text)).setText(endDate);

			// time
			((TextView) findViewById(R.id.submit)).setText(getResources().getString(R.string.update));
		} else {// new deal
			openingTime = new Date(System.currentTimeMillis());
			closingTime = new Date(System.currentTimeMillis());
			closingTime.setHours(openingTime.getHours() + 2);
		}

		findViewById(R.id.date_picker).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDatePicker();
			}
		});

		findViewById(R.id.time_picker).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showTimePicker();
			}
		});

	}

	private void setUpActionBar() {

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);

		String str = getResources().getString(R.string.restaurants);
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
	public void onBackPressed() {
		CommonLib.hideKeyBoard(CreatePromoScreen.this, findViewById(R.id.type));
		super.onBackPressed();
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

		if (requestType == CommonLib.PROMO_CREATION || requestType == CommonLib.PROMO_UPDATE) {
			if (!destroyed) {
				if (zProgressDialog != null && zProgressDialog.isShowing())
					zProgressDialog.dismiss();
				if (status) {
					Toast.makeText(mContext, getResources().getString(R.string.request_upload_success),
							Toast.LENGTH_LONG).show();
					onBackPressed();
				} else {
					Toast.makeText(mContext, getResources().getString(R.string.cannot_add_deal), Toast.LENGTH_LONG)
							.show();
				}
			}
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

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
						.setTitle(getActivity().getResources().getString(R.string.select_deal_type)).setAdapter(
								((CreatePromoScreen) getActivity()).new ListAdapter(getActivity(),
										R.layout.store_picker_snippet, nameList),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										((CreatePromoScreen) getActivity()).returnSelecteListItem(nameList.get(which),
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
			if (listName.equals(getResources().getString(R.string.deal_3))) {
				findViewById(R.id.product).setVisibility(View.VISIBLE);
				findViewById(R.id.product_value).setVisibility(View.VISIBLE);
				findViewById(R.id.radio_amount).setVisibility(View.VISIBLE);

				((TextView) findViewById(R.id.discount)).setText(getResources().getString(R.string.details));

				((TextView) findViewById(R.id.discount_value)).setHint(getResources().getString(R.string.details));

				findViewById(R.id.discount_second).setVisibility(View.GONE);
				findViewById(R.id.discount_second_value).setVisibility(View.GONE);

				findViewById(R.id.min_order_header).setVisibility(View.GONE);
				findViewById(R.id.min_order_value).setVisibility(View.GONE);

			} else if (listName.equals(getResources().getString(R.string.deal_2))) {
				findViewById(R.id.product).setVisibility(View.GONE);
				findViewById(R.id.product_value).setVisibility(View.GONE);
				findViewById(R.id.radio_amount).setVisibility(View.GONE);
				((RadioButton) findViewById(R.id.radio_flat)).setChecked(true);

				// radio flat options
				((TextView) findViewById(R.id.discount))
						.setText(getResources().getString(R.string.discount_percentage));
				((TextView) findViewById(R.id.discount_value))
						.setHint(getResources().getString(R.string.discount_percentage_hint));

				findViewById(R.id.discount_second).setVisibility(View.VISIBLE);
				findViewById(R.id.discount_second_value).setVisibility(View.VISIBLE);

				((TextView) findViewById(R.id.discount)).setText(getResources().getString(R.string.discount_food));

				((TextView) findViewById(R.id.discount_value))
						.setHint(getResources().getString(R.string.discount_food));

				((TextView) findViewById(R.id.discount_second))
						.setText(getResources().getString(R.string.discount_drinks));

				((TextView) findViewById(R.id.discount_second_value))
						.setHint(getResources().getString(R.string.discount_drinks));

				findViewById(R.id.min_order_header).setVisibility(View.VISIBLE);
				findViewById(R.id.min_order_value).setVisibility(View.VISIBLE);

			}
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

	public void onRadioButtonClicked(View view) {
		int viewId = view.getId();
		switch (viewId) {
		case R.id.radio_flat:
			findViewById(R.id.max_order_header).setVisibility(View.VISIBLE);
			findViewById(R.id.max_order).setVisibility(View.VISIBLE);
			String type = ((TextView) findViewById(R.id.type)).getText().toString();
			int dealType = 0;
			if (type.equals(mContext.getResources().getString(R.string.deal_1))) {
				dealType = 1;
			} else if (type.equals(mContext.getResources().getString(R.string.deal_2))) {
				dealType = 2;
			} else if (type.equals(mContext.getResources().getString(R.string.deal_3))) {
				dealType = 3;
			}
			if (dealType == 2) {
				((TextView) findViewById(R.id.discount)).setText(getResources().getString(R.string.details));
				((TextView) findViewById(R.id.discount_value)).setHint(getResources().getString(R.string.details));
			} else {
				((TextView) findViewById(R.id.discount))
						.setText(getResources().getString(R.string.discount_percentage));
				((TextView) findViewById(R.id.discount_value))
						.setHint(getResources().getString(R.string.discount_percentage_hint));
			}
			break;
		case R.id.radio_amount:
			((TextView) findViewById(R.id.discount)).setText(getResources().getString(R.string.discount_amount));
			((TextView) findViewById(R.id.discount_value))
					.setHint(getResources().getString(R.string.discount_amount_hint));
			findViewById(R.id.max_order_header).setVisibility(View.GONE);
			findViewById(R.id.max_order).setVisibility(View.GONE);
			break;
		}

	}

	private void showDatePicker() {
		LayoutInflater inflater = (LayoutInflater) mContext.getLayoutInflater();
		View customView = inflater.inflate(R.layout.two_date_pickers, null);

		// Define your date pickers
		// final DatePicker dpStartDate = (DatePicker)
		// customView.findViewById(R.id.dpStartDate);
		final DatePicker dpEndDate = (DatePicker) customView.findViewById(R.id.dpEndDate);

		// dpStartDate.updateDate(openingTime.getYear() + 1900,
		// openingTime.getMonth(), openingTime.getDate());
		dpEndDate.updateDate(closingTime.getYear() + 1900, closingTime.getMonth(), closingTime.getDate());

		// Build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(customView); // Set the view of the dialog to your
										// custom layout
		builder.setTitle(mContext.getResources().getString(R.string.Select_dates));
		builder.setPositiveButton(mContext.getResources().getString(R.string.small_ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						// int startYear = dpStartDate.getYear();
						// int startMonthOfYear = dpStartDate.getMonth();
						// int startDayOfMonth = dpStartDate.getDayOfMonth();

						int endYear = dpEndDate.getYear();
						int endMonthOfYear = dpEndDate.getMonth();
						int endDayOfMonth = dpEndDate.getDayOfMonth();

						SimpleDateFormat sdf = new SimpleDateFormat("EEE dd, MMM yy", Locale.getDefault());
						GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());

						// cal.set(startYear, startMonthOfYear,
						// startDayOfMonth);
						// sdf.setCalendar(cal);
						// String formatted = sdf.format(cal.getTime());
						// startingTime = cal.getTime();

						cal.set(endYear, endMonthOfYear, endDayOfMonth, 23, 59);
						String formatted = sdf.format(cal.getTime());

						// if (!(startYear == endYear && startDayOfMonth ==
						// endDayOfMonth
						// && startMonthOfYear == endMonthOfYear)) {
						// formatted += " - " + sdf.format(cal.getTime());
						// }
						endTime = cal.getTime();

						((TextView) findViewById(R.id.date_picker)).setText(formatted);

						// startDate = (startYear + "-" + (startMonthOfYear + 1)
						// + "-" + startDayOfMonth + " 00:00:00");
						// endDate = (endYear + "-" + (endMonthOfYear + 1) + "-"
						// + endDayOfMonth + " 00:00:00");

						dialog.dismiss();
					}
				});

		builder.setNegativeButton(mContext.getResources().getString(R.string.dialog_cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.create().show();
	}

	private void showTimePicker() {
		LayoutInflater inflater = (LayoutInflater) mContext.getLayoutInflater();
		final View customView = inflater.inflate(R.layout.two_time_pickers, null);

		// Define your time pickers
		final TimePicker dpStartTime = (TimePicker) customView.findViewById(R.id.dpStartTime);
		final TimePicker dpEndTime = (TimePicker) customView.findViewById(R.id.dpEndTime);

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTimeZone(TimeZone.getDefault());
		calendarStart.setTimeInMillis(openingTime.getTime());

		Calendar calendarEnd = Calendar.getInstance();
		calendarEnd.setTimeZone(TimeZone.getDefault());
		calendarEnd.setTimeInMillis(closingTime.getTime());

		dpStartTime.setCurrentHour(calendarStart.get(Calendar.HOUR_OF_DAY));
		dpStartTime.setCurrentMinute(calendarStart.get(Calendar.MINUTE));

		dpEndTime.setCurrentHour(calendarEnd.get(Calendar.HOUR_OF_DAY));
		dpEndTime.setCurrentMinute(calendarEnd.get(Calendar.MINUTE));

		final CheckBox mBox = (CheckBox) customView.findViewById(R.id.toggle);
		((TextView) customView.findViewById(R.id.text))
				.setText(mContext.getResources().getString(R.string.set_end_date));
		mBox.setClickable(false);
		customView.findViewById(R.id.end_time_container).setVisibility(View.VISIBLE);
		mBox.setPadding(0, 0, 0, 0);
		customView.findViewById(R.id.end_time_check_box).setPadding(0, 0, 0, width / 40);
		customView.findViewById(R.id.end_time_check_box).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean checked = ((CheckBox) customView.findViewById(R.id.toggle)).isChecked();
				((CheckBox) customView.findViewById(R.id.toggle)).setChecked(!checked);
				if (!checked) {
					customView.findViewById(R.id.end_time_container).setVisibility(View.VISIBLE);
				} else {
					customView.findViewById(R.id.end_time_container).setVisibility(View.GONE);
				}
			}
		});

		// Build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(customView); // Set the view of the dialog to your
		// custom layout
		builder.setTitle(mContext.getResources().getString(R.string.select_time_range));
		builder.setPositiveButton(mContext.getResources().getString(R.string.small_ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						int startHour = dpStartTime.getCurrentHour();
						int startMinute = dpStartTime.getCurrentMinute();

						int endHour = dpEndTime.getCurrentHour();
						int endMinute = dpEndTime.getCurrentMinute();

						Time mTime = new Time();
						mTime.set(0, startMinute, startHour, 1, 1, 1);
						String startTime = mTime.format("%I:%M %P");

						openingTime.setHours(startHour);
						openingTime.setMinutes(startMinute);
						// eventDetails.setStartTime(startHour + ":" +
						// startMinute + ":00");

						String endTime = "";
						String formatted = "";
						if (mBox.isChecked()) {
							mTime.set(0, endMinute, endHour, 1, 1, 1);
							endTime = mTime.format("%I:%M %P");
							formatted = startTime + " - " + endTime;
							// eventDetails.setEndTime(endHour + ":" + endMinute
							// + ":00");

						} else {
							formatted = startTime + " " + mContext.getResources().getString(R.string.onwards);
							// eventDetails.setEndTime("23:59:59");
						}
						closingTime.setHours(endHour);
						closingTime.setMinutes(endMinute);

						((TextView) findViewById(R.id.time_picker)).setText(formatted);
						// ((TextView)
						// headerView.findViewById(R.id.event_summary_time))
						// .setText(mContext.getResources().getString(R.string.time)
						// + ": " +
						// formatted);
						dialog.dismiss();
					}
				});

		builder.setNegativeButton(mContext.getResources().getString(R.string.dialog_cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		// Create and show the dialog
		builder.create().show();
	}

}
