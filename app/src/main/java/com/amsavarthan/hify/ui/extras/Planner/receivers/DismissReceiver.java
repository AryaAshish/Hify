package com.amsavarthan.hify.ui.extras.Planner.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.amsavarthan.hify.ui.extras.Planner.utils.AlarmUtil;
import com.amsavarthan.hify.ui.extras.Planner.utils.NotificationUtil;


public class DismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra("NOTIFICATION_ID", 0);
        NotificationUtil.cancelNotification(context, reminderId);

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("checkBoxNagging", false)) {
            Intent alarmIntent = new Intent(context, NagReceiver.class);
            AlarmUtil.cancelAlarm(context, alarmIntent, reminderId);
        }
    }
}
