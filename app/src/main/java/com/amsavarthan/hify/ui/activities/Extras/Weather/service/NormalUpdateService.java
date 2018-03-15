package com.amsavarthan.hify.ui.activities.Extras.Weather.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.basic.UpdateService;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.ValueUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter.AlarmHelper;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.NormalNotificationUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayDetailsUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayHorizontalUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayVerticalUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayWeekUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetDayUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetDayWeekUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetWeekUtils;

/**
 * Normal updateRotation service.
 */

public class NormalUpdateService extends UpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (WidgetDayUtils.isEnable(context)) {
            WidgetDayUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetWeekUtils.isEnable(context)) {
            WidgetWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetDayWeekUtils.isEnable(context)) {
            WidgetDayWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayHorizontalUtils.isEnable(context)) {
            WidgetClockDayHorizontalUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayDetailsUtils.isEnable(context)) {
            WidgetClockDayDetailsUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayVerticalUtils.isEnable(context)) {
            WidgetClockDayVerticalUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayWeekUtils.isEnable(context)) {
            WidgetClockDayWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (NormalNotificationUtils.isEnable(context)) {
            NormalNotificationUtils.buildNotificationAndSendIt(context, weather);
        }
    }

    @Override
    public void setDelayTask(boolean notifyFailed) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean backgroundFree = sharedPreferences.getBoolean(getString(R.string.key_background_free), false);
        String refreshRate = sharedPreferences.getString(getString(R.string.key_refresh_rate), "1:30");
        if (notifyFailed) {
            if (backgroundFree) {
                AlarmHelper.setAlarmForNormalView(this, 0.25F);
            } else {
                startService(
                        new Intent(this, PollingService.class)
                                .putExtra(PollingService.KEY_POLLING_UPDATE_FAILED, true));
            }
        } else if (backgroundFree) {
            AlarmHelper.setAlarmForNormalView(this, ValueUtils.getRefreshRateScale(refreshRate));
        }
    }
}
