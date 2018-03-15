package com.amsavarthan.hify.ui.activities.Extras.Weather.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Alert;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter.IntentHelper;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.manager.ThreadManager;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.NormalNotificationUtils;
import com.amsavarthan.hify.utils.Config;

import java.util.ArrayList;
import java.util.List;


/**
 * Notification utils.
 */

public class NotificationUtils {

    private static final String NOTIFICATION_GROUP_KEY = "geometric_weather_alert_notification_group";
    private static final String PREFERENCE_NOTIFICATION = "NOTIFICATION_PREFERENCE";
    private static final String KEY_NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final int NOTIFICATION_GROUP_SUMMARY_ID = 10001;
    private static final String CHANNEL_ID_ALERT = "Hify Weather";

    public static void refreshNotificationInNewThread(final Context c, final Location location) {
        ThreadManager.getInstance()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        if (NormalNotificationUtils.isEnable(c)) {
                            NormalNotificationUtils.buildNotificationAndSendIt(c, location.weather);
                        }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void setupChannels(NotificationManager notificationManager, String channelName, String channelDesc) {
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(Config.ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        adminChannel.setDescription(channelDesc);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.canShowBadge();
        adminChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    public static void checkAndSendAlert(Context c, Weather weather, Weather oldResult) {
        if (!PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean(c.getString(R.string.key_alert_notification_switch), true)) {
            return;
        }

        List<Alert> alertList = new ArrayList<>();
        if (oldResult != null) {
            for (int i = 0; i < weather.alertList.size(); i++) {
                boolean newAlert = true;
                for (int j = 0; j < oldResult.alertList.size(); j++) {
                    if (weather.alertList.get(i).id == oldResult.alertList.get(j).id) {
                        newAlert = false;
                        break;
                    }
                }
                if (newAlert) {
                    alertList.add(weather.alertList.get(i));
                }
            }
        }

        for (int i = 0; i < alertList.size(); i++) {
            sendAlertNotification(c, weather.base.city, alertList.get(i));
        }
    }

    private static void sendAlertNotification(Context c, String cityName, Alert alert) {
        NotificationManager manager = ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE));
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupChannels(manager, "Hify Weather", "Provides you with the weather details");
            }
            manager.notify(
                    getNotificationId(c),
                    buildSingleNotification(c, cityName, alert));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                manager.notify(NOTIFICATION_GROUP_SUMMARY_ID, buildGroupSummaryNotification(c, cityName, alert));
            }
        }
    }

    private static Notification buildSingleNotification(Context c, String cityName, Alert alert) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c, CHANNEL_ID_ALERT)
                .setSmallIcon(R.drawable.ic_alert)
                .setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher))
                .setContentTitle(c.getString(R.string.action_alert))
                .setSubText(alert.publishTime)
                .setContentText(alert.description)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentIntent(buildIntent(c, cityName));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setGroup(NOTIFICATION_GROUP_KEY);
        }
        return builder.build();
    }

    private static Notification buildGroupSummaryNotification(Context c, String cityName, Alert alert) {
        return new NotificationCompat.Builder(c, CHANNEL_ID_ALERT)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(alert.description)
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setGroupSummary(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(buildIntent(c, cityName))
                .build();
    }

    private static int getNotificationId(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(
                PREFERENCE_NOTIFICATION,
                Context.MODE_PRIVATE);
        int id = sharedPreferences.getInt(KEY_NOTIFICATION_ID, 1000) + 1;
        if (id > NOTIFICATION_GROUP_SUMMARY_ID - 1) {
            id = 1001;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_NOTIFICATION_ID, id);
        editor.apply();

        return id;
    }

    private static PendingIntent buildIntent(Context c, String cityName) {
        return PendingIntent.getActivity(
                c, 0, IntentHelper.buildMainActivityIntent(cityName), 0);
    }
}
