package com.application.zapplonmerchant.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.application.zapplonmerchant.ZApplication;
import com.application.zapplonmerchant.data.DealData;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Debug;
import android.widget.Toast;

public class UploadManager {

	public static Hashtable<Integer, AsyncTask> asyncs = new Hashtable<Integer, AsyncTask>();
	public static Context context;
	private static SharedPreferences prefs;
	private static ArrayList<UploadManagerCallback> callbacks = new ArrayList<UploadManagerCallback>();
	private static ZApplication zapp;

	public static void setContext(Context context) {
		UploadManager.context = context;
		prefs = context.getSharedPreferences("application_settings", 0);

		if (context instanceof ZApplication) {
			zapp = (ZApplication) context;
		}
	}

	public static void addCallback(UploadManagerCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}

		// this is here because its called from a lot of places.
		if ((double) Debug.getNativeHeapAllocatedSize() / Runtime.getRuntime().maxMemory() > .70) {
			if (zapp != null) {

				if (zapp.cache != null)
					zapp.cache.clear();
			}
		}
	}

	public static void removeCallback(UploadManagerCallback callback) {
		if (callbacks.contains(callback)) {
			callbacks.remove(callback);
		}
	}

	public static void updateStoreLocation(double lat, double lon) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.LOCATION_UPDATE, 0, "", null);
		}

		new UpdateStoreLocation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { lat, lon });

	}

	public static void updateRegistrationId(String regId) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.HARDWARE_REGISTER, 0, regId, null);
		}

		new UpdateRegistrationId().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { regId });

	}

	public static void updateLocation(double lat, double lon) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.LOCATION_UPDATE, 0, "", null);
		}

		new UpdateLocation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { lat, lon });

	}

	public static void updatePassword(String oldPassword, String newPasword) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.PASSWORD_UPDATE, 0, "", null);
		}

		new UpdatePassword().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { oldPassword, newPasword });

	}

	public static void login(String email, String password) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.LOGIN, 0, email, null);
		}

		new Login().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { email, password });

	}

	public static void logout(String accessToken) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.LOGOUT, 0, accessToken, null);
		}

		new Logout().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { accessToken });

	}

	public static void addCoupon(DealData dealData) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.PROMO_CREATION, 0, "" + dealData.getDealType(), null);
		}

		new AddCoupon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { dealData });
	}

	public static void updateStoreItem(DealData dealData) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.PROMO_UPDATE, 0, "" + dealData.getDealType(), null);
		}

		new UpdateCoupon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { dealData });
	}

	public static void addStore(String name, String type, String address, String contact, double latitude,
			double longitude, boolean isEditable) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.STORE_ADD, 0, "" + name, null);
		}

		new AddStore().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { name, type, address, contact, latitude, longitude, isEditable });

	}

	public static void updateAvailability(int action) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.UPDATE_AVAILABILITY, 0, "" + action, null);
		}

		new UpdateAvailability().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { action });

	}

	public static void updateTableBookingStatus(int bookingId, int action, String actionDescription) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.UPDATE_TABLE_BOOKING, 0, "" + action, null);
		}

		new UpdateTableBooking().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { bookingId, action, actionDescription });

	}

	public static void updateOccupancy(int occupancy) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.UPDATE_OCCUPANCY, 0, "" + occupancy, null);
		}

		new UpdateOccupancy().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[] { occupancy });

	}

	private static class UpdateStoreLocation extends AsyncTask<Object, Void, Object[]> {

		private double lat;
		private double lon;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			lat = (Double) params[0];
			lon = (Double) params[1];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("latitude", lat + ""));
			nameValuePairs.add(new BasicNameValuePair("longitude", lon + ""));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "store/updateLocation?", nameValuePairs,
						PostWrapper.LOCATION_UPDATE, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.LOCATION_UPDATE, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class UpdateLocation extends AsyncTask<Object, Void, Object[]> {

		private double lat;
		private double lon;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			lat = (Double) params[0];
			lon = (Double) params[1];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("latitude", lat + ""));
			nameValuePairs.add(new BasicNameValuePair("longitude", lon + ""));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "user/location?", nameValuePairs,
						PostWrapper.LOCATION_UPDATE, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
//			if (arg[0].equals("failure"))
//				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.LOCATION_UPDATE, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class UpdateRegistrationId extends AsyncTask<Object, Void, Object[]> {

		private String regId;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			regId = (String) params[0];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("pushId", regId));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "user/registrationId?", nameValuePairs,
						PostWrapper.HARDWARE_REGISTER, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
//			if (arg[0].equals("failure"))
//				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.HARDWARE_REGISTER, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class UpdatePassword extends AsyncTask<Object, Void, Object[]> {

		private String oldPassword;
		private String newPassword;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			oldPassword = (String) params[0];
			newPassword = (String) params[1];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("old_password", oldPassword));
			nameValuePairs.add(new BasicNameValuePair("new_password", newPassword));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "profile/updatePassword?", nameValuePairs,
						PostWrapper.PASSWORD_UPDATE, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.PASSWORD_UPDATE, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class Logout extends AsyncTask<Object, Void, Object[]> {

		private String email;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			email = (String) params[0];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "auth/logout?", nameValuePairs, PostWrapper.LOGOUT,
						context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.LOGOUT, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class AddCoupon extends AsyncTask<Object, Void, Object[]> {

		DealData dealData;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			dealData = (DealData) params[0];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("store_id", prefs.getInt("store_id", 0) + ""));

			nameValuePairs.add(new BasicNameValuePair("dealType", dealData.getDealType() + ""));
			nameValuePairs.add(new BasicNameValuePair("dealSubType", dealData.getDealSubType() + ""));
			nameValuePairs.add(new BasicNameValuePair("discount_amount", dealData.getDiscountAmount() + ""));
			nameValuePairs
					.add(new BasicNameValuePair("discount_second_amount", dealData.getDiscountSecondAmount() + ""));
			nameValuePairs.add(new BasicNameValuePair("min_order", dealData.getMinOrder() + ""));
			nameValuePairs.add(new BasicNameValuePair("max_order", dealData.getMaxOrder() + ""));
			nameValuePairs.add(new BasicNameValuePair("count", dealData.getCount() + ""));
			nameValuePairs.add(new BasicNameValuePair("product_name", dealData.getProductName()));

			nameValuePairs.add(new BasicNameValuePair("deal_opening_hour", dealData.getDealOpeningHour() + ""));
			nameValuePairs.add(new BasicNameValuePair("deal_closing_hour", dealData.getDealClosingHour() + ""));
			nameValuePairs.add(new BasicNameValuePair("deal_opening_min", dealData.getDealOpeningMin() + ""));
			nameValuePairs.add(new BasicNameValuePair("deal_closing_min", dealData.getDealClosingMin() + ""));
			nameValuePairs.add(new BasicNameValuePair("end_time", dealData.getEndTime() + ""));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "deal/add?", nameValuePairs, PostWrapper.ADD_COUPON,
						context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.PROMO_CREATION, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class Login extends AsyncTask<Object, Void, Object[]> {

		private String email;
		private String password;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			email = (String) params[0];
			password = (String) params[1];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("email", email));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("registration_id", password));
			nameValuePairs.add(new BasicNameValuePair("latitude", prefs.getString("lat", "0")));
			nameValuePairs.add(new BasicNameValuePair("longitude", prefs.getString("lon", "0")));
			nameValuePairs.add(new BasicNameValuePair("device_id", CommonLib.getIMEI(context)));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "auth/login?", nameValuePairs, PostWrapper.LOGIN,
						context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.LOGIN, prefs.getInt("uid", 0), 0, arg[1], 0, arg[0].equals("success"),
						"");
			}
		}
	}

	private static class AddStore extends AsyncTask<Object, Void, Object[]> {

		private String type;
		private String contactNumber;
		private String address;
		private String storeName;
		private double latitude;
		private double longitude;
		private boolean editable;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			storeName = (String) params[0];
			type = (String) params[1];
			address = (String) params[2];
			contactNumber = (String) params[3];
			latitude = (Double) params[4];
			longitude = (Double) params[5];
			editable = (Boolean) params[6];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("storeType", type));
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("contactNumber", contactNumber));
			nameValuePairs.add(new BasicNameValuePair("latitude", latitude + ""));
			nameValuePairs.add(new BasicNameValuePair("longitude", longitude + ""));
			nameValuePairs.add(new BasicNameValuePair("address", address));
			nameValuePairs.add(new BasicNameValuePair("storeName", storeName));

			String url;
			if (editable)
				url = CommonLib.SERVER + "store/edit?";
			else
				url = CommonLib.SERVER + "store/add?";
			try {
				result = PostWrapper.postRequest(url, nameValuePairs, PostWrapper.ADD_STORE, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.STORE_ADD, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class UpdateAvailability extends AsyncTask<Object, Void, Object[]> {

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
				result = PostWrapper.postRequest(CommonLib.SERVER + "store/availability?", nameValuePairs,
						PostWrapper.UPDATE_AVAILABILITY, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.UPDATE_AVAILABILITY, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class UpdateTableBooking extends AsyncTask<Object, Void, Object[]> {

		private int bookingId;
		private int action;
		private String actionDescription;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			bookingId = (Integer) params[0];
			action = (Integer) params[1];
			actionDescription = (String) params[2];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("booking_id", bookingId + ""));
			nameValuePairs.add(new BasicNameValuePair("action", "" + action));
			nameValuePairs.add(new BasicNameValuePair("action_description", "" + actionDescription));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "tablebooking/update?", nameValuePairs,
						PostWrapper.UPDATE_TABLE_BOOKING, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.UPDATE_TABLE_BOOKING, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class UpdateOccupancy extends AsyncTask<Object, Void, Object[]> {

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
						PostWrapper.UPDATE_OCCUPANCY, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.UPDATE_OCCUPANCY, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

	private static class UpdateCoupon extends AsyncTask<Object, Void, Object[]> {

		DealData dealData;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			dealData = (DealData) params[0];

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("store_id", prefs.getInt("store_id", 0) + ""));

			nameValuePairs.add(new BasicNameValuePair("dealType", dealData.getDealType() + ""));
			nameValuePairs.add(new BasicNameValuePair("dealSubType", dealData.getDealSubType() + ""));
			nameValuePairs.add(new BasicNameValuePair("discount_amount", dealData.getDiscountAmount() + ""));
			nameValuePairs
					.add(new BasicNameValuePair("discount_second_amount", dealData.getDiscountSecondAmount() + ""));
			nameValuePairs.add(new BasicNameValuePair("min_order", dealData.getMinOrder() + ""));
			nameValuePairs.add(new BasicNameValuePair("max_order", dealData.getMaxOrder() + ""));
			nameValuePairs.add(new BasicNameValuePair("count", dealData.getCount() + ""));
			nameValuePairs.add(new BasicNameValuePair("product_name", dealData.getProductName()));

			nameValuePairs.add(new BasicNameValuePair("deal_opening_hour", dealData.getDealOpeningHour() + ""));
			nameValuePairs.add(new BasicNameValuePair("deal_closing_hour", dealData.getDealClosingHour() + ""));
			nameValuePairs.add(new BasicNameValuePair("deal_opening_min", dealData.getDealOpeningMin() + ""));
			nameValuePairs.add(new BasicNameValuePair("deal_closing_min", dealData.getDealClosingMin() + ""));
			nameValuePairs.add(new BasicNameValuePair("end_time", dealData.getEndTime() + ""));
			nameValuePairs.add(new BasicNameValuePair("action", dealData.getAction() + ""));
			nameValuePairs.add(new BasicNameValuePair("storeItemId", dealData.getStoreItemId() + ""));

			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "deal/update?", nameValuePairs,
						PostWrapper.UPDATE_COUPON, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.PROMO_UPDATE, prefs.getInt("uid", 0), 0, arg[1], 0,
						arg[0].equals("success"), "");
			}
		}
	}

}