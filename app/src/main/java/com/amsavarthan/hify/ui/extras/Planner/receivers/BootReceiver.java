package com.amsavarthan.hify.ui.extras.Planner.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.amsavarthan.hify.ui.extras.Planner.database.DatabaseHelper;
import com.amsavarthan.hify.ui.extras.Planner.models.Reminder;
import com.amsavarthan.hify.ui.extras.Planner.utils.AlarmUtil;
import com.amsavarthan.hify.ui.extras.Planner.utils.DateAndTimeUtil;

import java.util.Calendar;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper database = DatabaseHelper.getInstance(context);
        List<Reminder> reminderList = database.getNotificationList(Reminder.ACTIVE);
        database.close();
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        for (Reminder reminder : reminderList) {
            Calendar calendar = DateAndTimeUtil.parseDateAndTime(reminder.getDateAndTime());
            calendar.set(Calendar.SECOND, 0);
            AlarmUtil.setAlarm(context, alarmIntent, reminder.getId(), calendar);
        }
    }
}