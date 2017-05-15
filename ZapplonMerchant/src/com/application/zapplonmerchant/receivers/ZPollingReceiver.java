package com.application.zapplonmerchant.receivers;

import com.application.zapplonmerchant.services.ZNewBookingService;
import com.application.zapplonmerchant.utils.CommonLib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class ZPollingReceiver extends WakefulBroadcastReceiver {

	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		if (!CommonLib.isServiceRunning(ZNewBookingService.class, context)) {

			Intent service = new Intent(context, ZNewBookingService.class);
			startWakefulService(context, service);

		}
	}
	
	public void setAlarm(Context context, long syncTime) {
		alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, ZPollingReceiver.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, syncTime, syncTime, alarmIntent);

	}

	public void cancelAlarm(Context context) {
		if (alarmMgr != null) {
			alarmMgr.cancel(alarmIntent);
		}
	}
}