package com.application.zapplonmerchant.services;

import java.util.zip.Inflater;

import org.json.JSONException;
import org.json.JSONObject;

import com.application.zapplonmerchant.R;
import com.application.zapplonmerchant.receivers.GcmBroadcastReceiver;
import com.application.zapplonmerchant.utils.CommonLib;
import com.application.zapplonmerchant.views.Home;
import com.application.zapplonmerchant.views.SplashScreen;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

public class GcmIntentService extends IntentService {

	public static boolean notificationDismissed = false;

	public Context context;
	private SharedPreferences prefs;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	public static final int NOTIFICATION_ID = 1;

	public GcmIntentService() {
		super("GcmIntentService");
		context = this;
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		prefs = getSharedPreferences("application_settings", 0);

		if (extras != null && !extras.isEmpty()) {
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */

			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				CommonLib.ZLog("Send error:", extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				CommonLib.ZLog("Deleted messages on server:", extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				sendNotification(extras);
			}
		}

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(Bundle extras) {
		if (extras == null)
			return;

		if (extras.containsKey("notification")) {
			String notification = String.valueOf(extras.get("notification"));

			String command = null;
			String type = null;
			Intent notificationActivity = null;
			boolean showNotification = true;

			if (extras.containsKey("command")) {
				command = extras.getString("command");
			} else {
				command = "Zapplon";
			}

			if (extras.containsKey("type")) {

				type = String.valueOf(extras.get("type"));

				if (type != null && type.equals(CommonLib.MERCHANT_GCM_TABLE_BOOKING)) {

					try {
						String bookingId = null;
						JSONObject jsonObject = new JSONObject(notification);
						if (jsonObject.has("booking_id")) {
							bookingId = String.valueOf("booking_id");
						}
						// if the home activity is not running, you need to
						// start from splash
						boolean inForeground = CommonLib.isApplicationInForeGround(this);
						if (inForeground && bookingId != null) {
							notificationActivity = new Intent(CommonLib.LOCAL_BROADCAST_NOTIFICATIONS);
							notificationActivity.putExtra("booking_id", bookingId);
							LocalBroadcastManager.getInstance(this).sendBroadcast(notificationActivity);
							showNotification = false;
						} else if (!inForeground) {
							if (prefs.getInt("merchant_id", 0) == 0) {
								notificationActivity = new Intent(this, SplashScreen.class);
							} else {
								notificationActivity = new Intent(this, Home.class);
								notificationActivity.putExtra("booking_id", bookingId);
							}

							notificationActivity
									.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
							startActivity(notificationActivity);
							showNotification = false;
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			}

			if (type == null) {
				// check if app is alive, do not push the message notifiication
				// then
				mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
				if (notificationActivity == null)
					notificationActivity = new Intent(this, SplashScreen.class);
				int flags = PendingIntent.FLAG_CANCEL_CURRENT;
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationActivity, flags);
				Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.ic_notification_icon).setContentTitle("Zapplon")
						.setStyle(new NotificationCompat.BigTextStyle().bigText(command)).setAutoCancel(true)
						.setContentText(notification).setSound(soundUri);
				mBuilder.setContentIntent(contentIntent);
				if (showNotification)
					mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
			}
		}
	}

	public static String decompress(byte[] compressed, int len) {
		String outputStr = null;
		try {
			Inflater decompresor = new Inflater();
			decompresor.setInput(compressed, 0, compressed.length);
			byte[] result = new byte[len];
			int resultLength = decompresor.inflate(result);
			decompresor.end();

			outputStr = new String(result, 0, resultLength, "UTF-8");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputStr;
	}
}