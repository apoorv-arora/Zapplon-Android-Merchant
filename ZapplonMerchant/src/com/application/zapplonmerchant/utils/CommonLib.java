package com.application.zapplonmerchant.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.data.StoreCatalogueItem;
import com.application.zapplonmerchant.data.UserWish;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class CommonLib {

	public final static boolean ZapplonLog = false;
	private static SharedPreferences prefs;

	public static final String SERVER_PREFIX = "http://";
	public static final String SERVER_BODY = "52.74.241.192:8080/ZapplonServer/rest/merchant/";
	public static String SERVER_WITHOUT_VERSION = "http://52.74.241.192:8080/ZapplonServer/rest/merchant/";

	public static String API_VERSION = "";
	public static String SERVER = SERVER_WITHOUT_VERSION + API_VERSION;

	public static final int ANIMATION_LOGIN = 200;
	public static final int ANIMATION_DURATION_SIGN_IN = 300;

	public static final String SERVER_VERSION_STRING = "VERSION_ANDROID_MERCHANT";
	public static final String SERVER_SUPPORT_STRING = "VERSION_ANDROID_MERCHANT_CONTACT";

	/** Preferences */
	public final static String APP_SETTINGS = "application_settings";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";

	public static String LightItalic = "fonts/Glober_Light_Italic.otf";
	// public static String Regular = "fonts/Glober_Light.otf";
	// public static String Bold = "fonts/Glober_SemiBold.otf";
	public static String Regular = "fonts/ProximaNova-Reg.otf";
	public static String Bold = "fonts/ProximaNova-Semibold.otf";
	public static String Icons = "fonts/baats-app.ttf";
	public static String BOLD_FONT_FILENAME = "BOLD_FONT_FILENAME";

	public static final int STORE_ITEM_ACTION_ADD = 101;
	public static final int STORE_ITEM_ACTION_EDIT = 102;
	public static final int STORE_ITEM_ACTION_DELETE = 103;

	public static final int STORE_ITEM_STATUS_ACTIVE = 1;
	public static final int STORE_ITEM_STATUS_ARCHIVE = 2;
	public static final int STORE_ITEM_STATUS_DE_PRIORITIZED = 3;

	public static final int DP_ACTION_SLOW = 1;
	public static final int DP_ACTION_MODERATE = 2;
	public static final int DP_ACTION_FAST = 3;
	public static final int DP_ACTION_FULL = 4;

	public static final int TABLE_BOOKING_ACTION_ACCEPT = 205;
	public static final int TABLE_BOOKING_ACTION_REJECT = 206;

	/* UPDATE TIMER */
	public static final long UPDATE_TIMER = 1000 * 60 * 2;

	/** GCM Sender ID */
	public static final String GCM_SENDER_ID = "481732547877";

	/** Application version */
	public static final int VERSION = 10;
	public static final String VERSION_STRING = "1.91";

	/** Authorization params */
	public static final String SOURCE = "&source=android_market&version=" + android.os.Build.VERSION.RELEASE
			+ "&app_version=" + VERSION;
	public static final String CLIENT_ID = "bt_android_client";
	public static final String APP_TYPE = "bt_android";

	/**
	 * Thread pool executors
	 */
	private static final int mImageAsyncsMaxSize = 4;
	public static final BlockingQueue<Runnable> sPoolWorkQueueImage = new LinkedBlockingQueue<Runnable>(128);
	private static ThreadFactory sThreadFactoryImage = new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r);
		}
	};
	public static final Executor THREAD_POOL_EXECUTOR_IMAGE = new ThreadPoolExecutor(mImageAsyncsMaxSize,
			mImageAsyncsMaxSize, 1, TimeUnit.SECONDS, sPoolWorkQueueImage, sThreadFactoryImage);

	/** Upload status tracker */
	public static final int REQUEST_PRE_SIGNUP = 201;
	public static final int LOGIN = 202;
	public static final int LOGOUT = 203;
	public static final int WISH_ADD = 204;
	public static final int WISH_REMOVE = 205;
	public static final int HARDWARE_REGISTER = 206;
	public static final int UPDATE_INSTITUTION = 207;
	public static final int WISH_UPDATE_STATUS = 208;
	public static final int SEND_MESSAGE = 209;
	public static final int LOCATION_UPDATE = 210;
	public static final int REDEEM_COUPON = 211;
	public static final int PROMO_CREATION = 212;
	public static final int STORE_ADD = 213;
	public static final int UPDATE_AVAILABILITY = 214;
	public static final int PROMO_UPDATE = 215;
	public static final int PASSWORD_UPDATE = 216;
	public static final int UPDATE_OCCUPANCY = 217;
	public static final int UPDATE_TABLE_BOOKING = 218;

	/** Constant to track location identification progress */
	public static final int LOCATION_NOT_ENABLED = 0;
	/** Constant to track location identification progress */
	public static final int LOCATION_NOT_DETECTED = 1;
	/** Constant to track location identification progress */
	public static final int LOCATION_DETECTED = 2;
	/** Constant to track location identification progress */
	public static final int GETZONE_CALLED = 3;
	/** Constant to track location identification progress */
	public static final int CITY_IDENTIFIED = 4;
	/** Constant to track location identification progress */
	public static final int CITY_NOT_IDENTIFIED = 5;
	public static final int LOCATION_DETECTION_RUNNING = 6;
	public static final int DIFFERENT_CITY_IDENTIFIED = 7;

	/**
	 * Push notifiation types
	 */
	public static final String MERCHANT_GCM_TABLE_BOOKING = "merchant_gcm_table_booking";

	public static final String LOCAL_BROADCAST_NOTIFICATIONS = "new_push_notification";

	// Return this string for every call
	public static String getVersionString(Context context) {
		String language = Locale.getDefault().getLanguage();
		String languageLog = Locale.getDefault().getLanguage();
		String country = Locale.getDefault().getCountry();
		String uuidString = "";

		if (prefs == null && context != null)
			prefs = context.getSharedPreferences(APP_SETTINGS, 0);

		if (prefs != null)
			uuidString = "&uuid=" + prefs.getString("app_id", "");

		if (language.equalsIgnoreCase("pt") && country.equalsIgnoreCase("PT"))
			language = "pt";

		else if (language.equalsIgnoreCase("pt") && country.equalsIgnoreCase("BR"))
			language = "por";

		else if (language.equalsIgnoreCase("in"))
			language = "id";

		else if (language.equalsIgnoreCase("es") && country.equalsIgnoreCase("CL"))
			language = "es_cl";

		else if (language.equalsIgnoreCase("cs"))
			language = "cs";

		else if (language.equalsIgnoreCase("sk"))
			language = "sk";

		else if (language.equalsIgnoreCase("pl"))
			language = "pl";

		else if (language.equalsIgnoreCase("it"))
			language = "it";

		return SOURCE + uuidString + "&lang=" + language + "&android_language=" + languageLog + "&android_country="
				+ country;
	}

	public static boolean checkLgManufacturer() {

		try {
			String manufacturer = "";
			manufacturer = android.os.Build.MANUFACTURER;

			if (android.os.Build.VERSION.SDK_INT == 17 && manufacturer.toLowerCase().startsWith("lg"))
				return true;

		} catch (Exception e) {
		}

		return false;
	}

	// Calculate the sample size of bitmaps
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		int inSampleSize = 1;
		double ratioH = (double) options.outHeight / reqHeight;
		double ratioW = (double) options.outWidth / reqWidth;

		int h = (int) Math.round(ratioH);
		int w = (int) Math.round(ratioW);

		if (h > 1 || w > 1) {
			if (h > w) {
				inSampleSize = h >= 2 ? h : 2;

			} else {
				inSampleSize = w >= 2 ? w : 2;
			}
		}
		return inSampleSize;
	}

	public static final Hashtable<String, Typeface> typefaces = new Hashtable<String, Typeface>();

	public static Typeface getTypeface(Context c, String name) {
		synchronized (typefaces) {
			if (!typefaces.containsKey(name)) {
				try {
					InputStream inputStream = c.getAssets().open(name);
					File file = createFileFromInputStream(inputStream, name);
					if (file == null) {
						return Typeface.DEFAULT;
					}
					Typeface t = Typeface.createFromFile(file);
					typefaces.put(name, t);
				} catch (Exception e) {
					e.printStackTrace();
					return Typeface.DEFAULT;
				}
			}
			return typefaces.get(name);
		}
	}

	private static File createFileFromInputStream(InputStream inputStream, String name) {

		try {
			File f = File.createTempFile("font", null);
			OutputStream outputStream = new FileOutputStream(f);
			byte buffer[] = new byte[1024];
			int length = 0;

			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}

			outputStream.close();
			inputStream.close();
			return f;
		} catch (Exception e) {
			// Logging exception
			e.printStackTrace();
		}

		return null;
	}

	// Baatna Logging end points
	public static void ZLog(String Tag, String Message) {
		if (ZapplonLog && Message != null)
			Log.i(Tag, Message);
	}

	public static void ZLog(String Tag, float Message) {
		if (ZapplonLog)
			Log.i(Tag, Message + "");
	}

	public static void ZLog(String Tag, boolean Message) {
		if (ZapplonLog)
			Log.i(Tag, Message + "");
	}

	public static void ZLog(String Tag, int Message) {
		if (ZapplonLog)
			Log.i(Tag, Message + "");
	}

	public static InputStream getStream(HttpResponse response) throws IllegalStateException, IOException {
		InputStream instream = response.getEntity().getContent();
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
			instream = new GZIPInputStream(instream);
		}
		return instream;
	}

	// Checks if network is available
	public static boolean isNetworkAvailable(Context c) {
		ConnectivityManager connectivityManager = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return distance in km
	 */

	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		return dist;
	}

	// Returns the Network State
	public static String getNetworkState(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		String returnValue = "";
		if (null != activeNetwork) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
				returnValue = "wifi";
			else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
				returnValue = "mobile" + "_" + getNetworkType(context);
			else
				returnValue = "Unknown";
		} else
			returnValue = "Not connected";
		return returnValue;
	}

	// Returns the Data Network type
	public static String getNetworkType(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		switch (telephonyManager.getNetworkType()) {

		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "1xRTT";

		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";

		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE ";

		case TelephonyManager.NETWORK_TYPE_EHRPD:
			return "EHRPD ";

		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "EVDO_0 ";

		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "EVDO_A ";

		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return "EVDO_B ";

		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GPRS ";

		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "HSDPA ";

		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA ";

		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return "HSPAP ";

		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "HSUPA ";

		case TelephonyManager.NETWORK_TYPE_IDEN:
			return "IDEN ";

		case TelephonyManager.NETWORK_TYPE_LTE:
			return "LTE ";

		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS ";

		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return "UNKNOWN ";

		default:
			return "UNKNOWN ";
		}
	}

	// check done before storing the bitmap in the memory
	public static boolean shouldScaleDownBitmap(Context context, Bitmap bitmap) {
		if (context != null && bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);
			int width = metrics.widthPixels;
			int height = metrics.heightPixels;
			return ((width != 0 && width / bitmap.getWidth() < 1) || (height != 0 && height / bitmap.getHeight() < 1));
		}
		return false;
	}

	public static boolean isAndroidL() {
		return android.os.Build.VERSION.SDK_INT >= 21;
	}

	public static String getDateFromUTC(long timestamp) {
		Date date = new Date(timestamp);
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		cal.setTime(date);
		return (cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DATE) + " " + cal.get(Calendar.HOUR) + ":"
				+ cal.get(Calendar.MINUTE) + (cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM"));
	}

	/**
	 * Returns the bitmap associated
	 */
	public static Bitmap getBitmap(Context mContext, int resId, int width, int height) throws OutOfMemoryError {
		if (mContext == null)
			return null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeResource(mContext.getResources(), resId, options);
		options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;

		if (!CommonLib.isAndroidL())
			options.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId, options);

		return bitmap;
	}

	/**
	 * Blur a bitmap with the radius associated
	 */
	public static Bitmap fastBlur(Bitmap bitmap, int radius) {
		try {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();

			int[] pix = new int[w * h];
			CommonLib.ZLog("pix", w + " " + h + " " + pix.length);
			bitmap.getPixels(pix, 0, w, 0, 0, w, h);

			Bitmap blurBitmap = bitmap.copy(bitmap.getConfig(), true);

			int wm = w - 1;
			int hm = h - 1;
			int wh = w * h;
			int div = radius + radius + 1;

			int r[] = new int[wh];
			int g[] = new int[wh];
			int b[] = new int[wh];
			int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
			int vmin[] = new int[Math.max(w, h)];

			int divsum = (div + 1) >> 1;
			divsum *= divsum;
			int dv[] = new int[256 * divsum];
			for (i = 0; i < 256 * divsum; i++) {
				dv[i] = (i / divsum);
			}

			yw = yi = 0;

			int[][] stack = new int[div][3];
			int stackpointer;
			int stackstart;
			int[] sir;
			int rbs;
			int r1 = radius + 1;
			int routsum, goutsum, boutsum;
			int rinsum, ginsum, binsum;

			for (y = 0; y < h; y++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				for (i = -radius; i <= radius; i++) {
					p = pix[yi + Math.min(wm, Math.max(i, 0))];
					sir = stack[i + radius];
					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);
					rbs = r1 - Math.abs(i);
					rsum += sir[0] * rbs;
					gsum += sir[1] * rbs;
					bsum += sir[2] * rbs;
					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}
				}
				stackpointer = radius;

				for (x = 0; x < w; x++) {

					r[yi] = dv[rsum];
					g[yi] = dv[gsum];
					b[yi] = dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (y == 0) {
						vmin[x] = Math.min(x + radius + 1, wm);
					}
					p = pix[yw + vmin[x]];

					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[(stackpointer) % div];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi++;
				}
				yw += w;
			}
			for (x = 0; x < w; x++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				yp = -radius * w;
				for (i = -radius; i <= radius; i++) {
					yi = Math.max(0, yp) + x;

					sir = stack[i + radius];

					sir[0] = r[yi];
					sir[1] = g[yi];
					sir[2] = b[yi];

					rbs = r1 - Math.abs(i);

					rsum += r[yi] * rbs;
					gsum += g[yi] * rbs;
					bsum += b[yi] * rbs;

					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}

					if (i < hm) {
						yp += w;
					}
				}
				yi = x;
				stackpointer = radius;
				for (y = 0; y < h; y++) {
					// Preserve alpha channel: ( 0xff000000 & pix[yi] )
					pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (x == 0) {
						vmin[y] = Math.min(y + r1, hm) * w;
					}
					p = x + vmin[y];

					sir[0] = r[p];
					sir[1] = g[p];
					sir[2] = b[p];

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[stackpointer];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi += w;
				}
			}

			CommonLib.ZLog("pix", w + " " + h + " " + pix.length);
			blurBitmap.setPixels(pix, 0, w, 0, 0, w, h);
			return blurBitmap;

		} catch (OutOfMemoryError e) {
			return bitmap;
		} catch (Exception e) {
			return bitmap;
		}
	}

	public static Bitmap getBitmapFromDisk(String url, Context ctx) {

		Bitmap defautBitmap = null;
		try {
			String filename = constructFileName(url);
			File filePath = new File(ctx.getCacheDir(), filename);

			if (filePath.exists() && filePath.isFile() && !filePath.isDirectory()) {
				FileInputStream fi;
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inPreferredConfig = Config.RGB_565;
				fi = new FileInputStream(filePath);
				defautBitmap = BitmapFactory.decodeStream(fi, null, opts);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (Exception e) {

		} catch (OutOfMemoryError e) {

		}

		return defautBitmap;
	}

	public static String constructFileName(String url) {
		return url.replaceAll("/", "_");
	}

	public static void writeBitmapToDisk(String url, Bitmap bmp, Context ctx, CompressFormat format) {
		FileOutputStream fos;
		String fileName = constructFileName(url);
		try {
			if (bmp != null) {
				fos = new FileOutputStream(new File(ctx.getCacheDir(), fileName));
				bmp.compress(format, 75, fos);
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Bitmap getRoundedCornerBitmap(final Bitmap bitmap, final float roundPx) {

		if (bitmap != null) {
			try {
				final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
				Canvas canvas = new Canvas(output);

				final Paint paint = new Paint();
				final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
				final RectF rectF = new RectF(rect);

				paint.setAntiAlias(true);
				canvas.drawARGB(0, 0, 0, 0);
				canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

				paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
				canvas.drawBitmap(bitmap, rect, rect, paint);

				return output;

			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	public static final int STORE_TYPE_SALON_SPA = 1;
	public static final int STORE_TYPE_FOOD_DRINKS = 2;

	public static String getStoreType(Activity mContext, int storeType) {
		switch (storeType) {
		case STORE_TYPE_SALON_SPA:
			return mContext.getResources().getString(R.string.salon_spa_category);
		case STORE_TYPE_FOOD_DRINKS:
			return mContext.getResources().getString(R.string.food_drinks_category);
		default:
			return "";
		}
	}

	public static final int DEAL_TYPE_1 = 1;
	public static final int DEAL_TYPE_2 = 2;
	public static final int DEAL_TYPE_3 = 3;
	public static final int DEAL_TYPE_4 = 4; // Product deal tied up when the
												// merchant is tied up.

	public static String getDealType(Activity mContext, int storeType) {
		switch (storeType) {
		case DEAL_TYPE_1:
			return mContext.getResources().getString(R.string.deal_1);
		case DEAL_TYPE_2:
			return mContext.getResources().getString(R.string.deal_2);
		case DEAL_TYPE_3:
			return mContext.getResources().getString(R.string.deal_3);
		case DEAL_TYPE_4:
			return mContext.getResources().getString(R.string.deal_4);
		default:
			return "";
		}
	}

	public static final int DEAL_SUB_TYPE_FLAT = 1;
	public static final int DEAL_SUB_TYPE_AMOUNT = 2;

	/**
	 * Remove the keyboard explicitly.
	 */
	public static void hideKeyBoard(Activity mActivity, View mGetView) {
		try {
			((InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(mGetView.getRootView().getWindowToken(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getStoreItemDescription(StoreCatalogueItem storeItem) {

		if (storeItem == null)
			return "";

		StringBuilder finalStr = new StringBuilder();

		// Get X %/Rs off
		if (storeItem.getDealType() == CommonLib.DEAL_TYPE_4) {
			// get the current day and return the string
			Calendar calendar = Calendar.getInstance();
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			switch (dayOfWeek) {
			case Calendar.MONDAY:
				if (storeItem.getProductNameMon() != null && storeItem.getDiscountAmountMon() > 0) {
					finalStr.append(
							"Get " + storeItem.getDiscountAmountMon() + " % off on " + storeItem.getProductNameMon());
				}
				break;
			case Calendar.TUESDAY:
				if (storeItem.getProductNameTue() != null && storeItem.getDiscountAmountTue() > 0) {
					finalStr.append(
							"Get " + storeItem.getDiscountAmountTue() + " % off on " + storeItem.getProductNameTue());
				}
				break;
			case Calendar.WEDNESDAY:
				if (storeItem.getProductNameWed() != null && storeItem.getDiscountAmountWed() > 0) {
					finalStr.append(
							"Get " + storeItem.getDiscountAmountWed() + " % off on " + storeItem.getProductNameWed());
				}
				break;
			case Calendar.THURSDAY:
				if (storeItem.getProductNameThu() != null && storeItem.getDiscountAmountThu() > 0) {
					finalStr.append(
							"Get " + storeItem.getDiscountAmountThu() + " % off on " + storeItem.getProductNameThu());
				}
				break;
			case Calendar.FRIDAY:
				if (storeItem.getProductNameFri() != null && storeItem.getDiscountAmountFri() > 0) {
					finalStr.append(
							"Get " + storeItem.getDiscountAmountFri() + " % off on " + storeItem.getProductNameFri());
				}
				break;
			case Calendar.SATURDAY:
				if (storeItem.getProductNameSat() != null && storeItem.getDiscountAmountSat() > 0) {
					finalStr.append(
							"Get " + storeItem.getDiscountAmountSat() + " % off on " + storeItem.getProductNameSat());
				}
				break;
			case Calendar.SUNDAY:
				if (storeItem.getProductNameSun() != null && storeItem.getDiscountAmountSun() > 0) {
					finalStr.append(
							"Get " + storeItem.getDiscountAmountSun() + " % off on " + storeItem.getProductNameSun());
				}
				break;

			}
			if (finalStr.length() > 1) {
				return finalStr.toString();
			} else {
				finalStr.append("No Deal available today");

				if (storeItem.getProductNameMon() != null && storeItem.getDiscountAmountMon() > 0) {
					finalStr.append("\nMonday: Get " + storeItem.getDiscountAmountMon() + " % off on "
							+ storeItem.getProductNameMon());
				}
				if (storeItem.getProductNameTue() != null && storeItem.getDiscountAmountTue() > 0) {
					finalStr.append("\nTuesday: Get " + storeItem.getDiscountAmountTue() + " % off on "
							+ storeItem.getProductNameTue());
				}
				if (storeItem.getProductNameWed() != null && storeItem.getDiscountAmountWed() > 0) {
					finalStr.append("\nWednesday: Get " + storeItem.getDiscountAmountWed() + " % off on "
							+ storeItem.getProductNameWed());
				}
				if (storeItem.getProductNameThu() != null && storeItem.getDiscountAmountThu() > 0) {
					finalStr.append("\nThursday: Get " + storeItem.getDiscountAmountThu() + " % off on "
							+ storeItem.getProductNameThu());
				}
				if (storeItem.getProductNameFri() != null && storeItem.getDiscountAmountFri() > 0) {
					finalStr.append("\nFriday: Get " + storeItem.getDiscountAmountFri() + " % off on "
							+ storeItem.getProductNameFri());
				}
				if (storeItem.getProductNameSat() != null && storeItem.getDiscountAmountSat() > 0) {
					finalStr.append("\nSaturday: Get " + storeItem.getDiscountAmountSat() + " % off on "
							+ storeItem.getProductNameSat());
				}
				if (storeItem.getProductNameSun() != null && storeItem.getDiscountAmountSun() > 0) {
					finalStr.append("\nSunday: Get " + storeItem.getDiscountAmountSun() + " % off on "
							+ storeItem.getProductNameSun());
				}
				return finalStr.toString();
			}
		}

		if (storeItem.getDealSubType() == CommonLib.DEAL_SUB_TYPE_FLAT)
			finalStr.append("Get " + storeItem.getDiscountAmount() + " % off");
		else if (storeItem.getDealSubType() == CommonLib.DEAL_SUB_TYPE_AMOUNT)
			finalStr.append("Get Rs. " + storeItem.getDiscountAmount() + " off");

		if (storeItem.getDealType() != CommonLib.DEAL_TYPE_3 && storeItem.getDiscountAmount() >= 1) {
			if (storeItem.getStoreType() == CommonLib.STORE_TYPE_SALON_SPA) {
				finalStr.append(" on " + "Services");
			} else if (storeItem.getStoreType() == CommonLib.STORE_TYPE_FOOD_DRINKS) {
				finalStr.append(" on " + "Food");
			}
		}

		// Get X %/Rs off
		if (storeItem.getDiscountAmount() < 1 && storeItem.getDiscountSecondAmount() >= 1) {
			if (storeItem.getDealSubType() == CommonLib.DEAL_SUB_TYPE_FLAT)
				finalStr.append("Get " + storeItem.getDiscountSecondAmount() + " % off");
			else if (storeItem.getDealSubType() == CommonLib.DEAL_SUB_TYPE_AMOUNT)
				finalStr.append("Get Rs. " + storeItem.getDiscountSecondAmount() + " off");
		} else if (storeItem.getDealType() != CommonLib.DEAL_TYPE_3 && storeItem.getDiscountSecondAmount() >= 1) {
			if (storeItem.getDealSubType() == CommonLib.DEAL_SUB_TYPE_FLAT)
				finalStr.append(" and Get " + storeItem.getDiscountSecondAmount() + " % off");
			else if (storeItem.getDealSubType() == CommonLib.DEAL_SUB_TYPE_AMOUNT)
				finalStr.append(" and Get Rs. " + storeItem.getDiscountSecondAmount() + " off");
		}

		if (storeItem.getDealType() != CommonLib.DEAL_TYPE_3 && storeItem.getDiscountSecondAmount() >= 1) {
			if (storeItem.getStoreType() == CommonLib.STORE_TYPE_SALON_SPA) {
				finalStr.append(" on " + "Services");
			} else if (storeItem.getStoreType() == CommonLib.STORE_TYPE_FOOD_DRINKS) {
				finalStr.append(" on " + "Drinks");
			}
		}

		if (storeItem.getDealType() == CommonLib.DEAL_TYPE_3) {
			finalStr.append(" on " + storeItem.getProductName());
		}

		if (storeItem.getMinOrder() != 0)
			finalStr.append(" with a minimum order of Rs. " + storeItem.getMinOrder());

		if (storeItem.getDealSubType() == CommonLib.DEAL_SUB_TYPE_AMOUNT && storeItem.getMaxOrder() != 0)
			finalStr.append(" upto a maximum discount of Rs. " + storeItem.getMaxOrder());

		return finalStr.toString();
	}

	public static String getTimeDifferenceString(Date fromDate) {

		Date currentDate = new Date(System.currentTimeMillis());

		if (currentDate.after(fromDate)) {
			currentDate = new Date(System.currentTimeMillis());
		} else {
			currentDate = fromDate;
			fromDate = new Date(System.currentTimeMillis());
		}

		StringBuilder builder = new StringBuilder();
		int year = currentDate.getYear() - fromDate.getYear();
		int month = currentDate.getMonth() - fromDate.getMonth();
		int date = currentDate.getDate() - fromDate.getDate();
		int hour = currentDate.getHours() - fromDate.getHours();
		int minute = currentDate.getMinutes() - fromDate.getMinutes();

		if (year > 1)
			builder.append(year + " years, ");

		if (month > 1)
			builder.append(month + " months, ");

		if (date > 1)
			builder.append(date + " days, ");
		else if (date == 1)
			builder.append(date + " day, ");

		if (hour > 1)
			builder.append(hour + " hours, ");
		else if (hour == 1)
			builder.append(hour + " hour, ");

		if (minute > 1)
			builder.append(minute + " minutes, ");
		else if (minute == 1)
			builder.append(minute + " minute, ");

		if (builder != null && builder.toString() != null && builder.toString().length() > 1)
			return builder.toString().substring(0, builder.toString().length() - 2);
		else
			return "";
	}

	public static String getStoreItemStatusString(int status) {
		switch (status) {
		case CommonLib.STORE_ITEM_STATUS_ACTIVE:
			return "Active";
		case CommonLib.STORE_ITEM_STATUS_ARCHIVE:
			return "Archived";
		case CommonLib.STORE_ITEM_STATUS_DE_PRIORITIZED:
			return "Inactive";
		default:
			return "";
		}
	}

	public static String getTableBookingDetails(UserWish userWish) {

		if (userWish == null)
			return "";

		StringBuilder finalStr = new StringBuilder();
		finalStr.append("Reservation date: ");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(userWish.getStartDate());
		int endYear = calendar.get(Calendar.YEAR);
		int endMonthOfYear = calendar.get(Calendar.MONTH);
		int endDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		SimpleDateFormat sdf = new SimpleDateFormat("EEE dd, MMM yy", Locale.getDefault());
		GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());

		cal.set(endYear, endMonthOfYear, endDayOfMonth, 23, 59);

		finalStr.append(sdf.format(cal.getTime()) + "\n");

		finalStr.append("Reservation time: ");

		int startHour = calendar.get(Calendar.HOUR_OF_DAY);
		int startMinute = calendar.get(Calendar.MINUTE);

		calendar.setTimeInMillis(userWish.getEndDate());
		int endHour = calendar.get(Calendar.HOUR_OF_DAY);
		int endMinute = calendar.get(Calendar.MINUTE);

		Time mTime = new Time();
		mTime.set(0, startMinute, startHour, 1, 1, 1);
		String startTime = mTime.format("%I:%M %P");

		String endTime = "";
		String formatted = "";
		if (userWish.getEndDate() > 0) {
			mTime.set(0, endMinute, endHour, 1, 1, 1);
			endTime = mTime.format("%I:%M %P");
			formatted = startTime + " - " + endTime;
			// eventDetails.setEndTime(endHour + ":" + endMinute
			// + ":00");

		} else {
			formatted = startTime + " " + "Onwards";
		}
		finalStr.append(formatted);
		if (userWish.getUser() != null && userWish.getUser().getUserName() != null)
			finalStr.append(
					"\nBooked by " + userWish.getUser().getUserName() + " + " + (userWish.getCrowd() - 1) + " more");
		return finalStr.toString();
	}

	// IMEISV
	public static String getIMEI(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imeisv = telephonyManager.getDeviceId();
		if (imeisv == null)
			imeisv = "Unknown";
		return imeisv;
	}

	public static int getStoreItemStatusColor(Context mContext, int status) {
		switch (status) {
		case CommonLib.STORE_ITEM_STATUS_ACTIVE:
			return mContext.getResources().getColor(R.color.submit_green);
		case CommonLib.STORE_ITEM_STATUS_ARCHIVE:
			return mContext.getResources().getColor(R.color.submit_green);
		case CommonLib.STORE_ITEM_STATUS_DE_PRIORITIZED:
			return mContext.getResources().getColor(R.color.zomato_red_feedback);
		default:
			return mContext.getResources().getColor(R.color.submit_green);
		}
	}

	public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isApplicationInForeGround(Context context) {

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			String classes = "com.application.zapplonmerchant";
			// add your activities here.
			if (topActivity.getPackageName().equals(context.getPackageName())
					|| topActivity.getPackageName().equals("com.android.packageinstaller")
					|| topActivity.getPackageName().startsWith(classes)) {
				return true;
			}
		}

		return false;
	}

	public static void disableKeyguard(Context mContext) {
		if (mContext == null)
			return;

		Window window = ((Activity) mContext).getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
				"MyWakeLock");

		if (wakeLock != null && !wakeLock.isHeld())
			wakeLock.acquire();
	}

	@SuppressLint("NewApi")
	public static boolean isLocked(Context mContext) {
		if (Build.VERSION.SDK_INT >= 16) {
			KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
			return km.isKeyguardSecure();
		} else
			return false;
	}

}
