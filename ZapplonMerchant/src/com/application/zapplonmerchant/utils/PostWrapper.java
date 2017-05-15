package com.application.zapplonmerchant.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.data.User;

import android.content.Context;
import android.content.SharedPreferences;

public class PostWrapper {

	private static SharedPreferences prefs;

	/** Constants */
	public static String PRESIGNUP = "pre_signup";
	public static String LOGOUT = "logout";
	public static String LOGIN = "register";
	public static String WISH_POST = "wish_post";
	public static String WISH_DELETE = "wish_delete";
	public static String HARDWARE_REGISTER = "hardware_register";
	public static String INSTITUTION_ID = "update_institution_id";
	public static String WISH_STATUS_UPDATE = "wish_status_update";
	public static String SEND_MESSAGE = "send_message";
	public static String LOCATION_UPDATE = "update_location";
	public static String REDEEM_COUPON = "redeem_coupon";
	public static String ADD_COUPON = "add_coupon";
	public static String ADD_STORE = "add_store";
	public static String UPDATE_AVAILABILITY = "update_availability";
	public static String UPDATE_COUPON = "update_coupon";
	public static String PASSWORD_UPDATE = "password_update";
	public static String UPDATE_OCCUPANCY = "update_OCCUPANCY";
	public static String UPDATE_TABLE_BOOKING = "update_table_booking";

	public static void Initialize(Context context) {
		// helper = new ResponseCacheManager(context);
		prefs = context.getSharedPreferences("application_settings", 0);
	}

	public static String convertStreamToString(java.io.InputStream is) {
		try {
			return new java.util.Scanner(is).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}

	public static Object[] postRequest(String Url, List<NameValuePair> nameValuePairs, String type,
			Context appContext) {

		Object[] resp = new Object[] { "failed", appContext.getResources().getString(R.string.could_not_connect),
				new User() };

		try {

			HttpResponse response = getPostResponse(Url, nameValuePairs, appContext);
			int responseCode = response.getStatusLine().getStatusCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream is = CommonLib.getStream(response);
				if (type.equals(PRESIGNUP)) {
					resp = ParserJson.parseSignupResponse(is);
				} else if (type.equals(REDEEM_COUPON)) {
					resp = ParserJson.parseRedeemCouponResponse(is);
				} else if (type.equals(ADD_COUPON)) {
					resp = ParserJson.parseCouponUploadResponse(is);
				} else if (type.equals(LOGIN)) {
					resp = ParserJson.parseLoginResponse(is);
				} else if (type.equals(LOGOUT)) {
					resp = ParserJson.parseLogoutResponse(is);
				} else if (type.equals(ADD_STORE)) {
					resp = ParserJson.parseAddStoreResponse(is);
				} else if (type.equals(UPDATE_AVAILABILITY)) {
					resp = ParserJson.parseUpdateAvailabilityResponse(is);
				} else if (type.equals(UPDATE_COUPON)) {
					resp = ParserJson.parseCouponUploadResponse(is);
				}  else if (type.equals(LOCATION_UPDATE)) {
					resp = ParserJson.parseLocationUpdateResponse(is);
				}  else if (type.equals(PASSWORD_UPDATE)) {
					resp = ParserJson.parsePasswordUpdateResponse(is);
				} else if (type.equals(UPDATE_OCCUPANCY)) {
					resp = ParserJson.parseUpdateOccupancyResponse(is);
				} else  {
					resp = ParserJson.parseGenericResponse(is);
				}
			}
			// else {
			// logErrorResponse(url, response);
			// }

		} catch (Exception E) {
			E.printStackTrace();
			return resp;
		}
		return resp;
	}

	public static HttpResponse getPostResponse(String Url, List<NameValuePair> nameValuePairs, Context appContext)
			throws Exception {

		HttpPost httpPost = new HttpPost(Url + CommonLib.getVersionString(appContext));
		httpPost.addHeader(new BasicHeader("client_id", CommonLib.CLIENT_ID));
		httpPost.addHeader(new BasicHeader("app_type", CommonLib.APP_TYPE));

		if (nameValuePairs != null)
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

		return HttpManager.execute(httpPost);
	}

}
