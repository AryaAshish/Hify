package com.amsavarthan.hify.ui.extras.Weather.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.WeatherExtra;
import com.amsavarthan.hify.ui.extras.Weather.basic.UpdateService;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.AlarmHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.remoteView.ForecastNotificationUtils;

/**
 * Today forecast updateRotation service.
 */

public class TodayForecastUpdateService extends UpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (ForecastNotificationUtils.isEnable(this, true)) {
            ForecastNotificationUtils.buildForecastAndSendIt(context, weather, true);
        }
    }

    @Override
    public void setDelayTask(boolean notifyFailed) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean openTodayForecast = sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false);
        String todayForecastTime = sharedPreferences.getString(
                getString(R.string.key_forecast_today_time),
                WeatherExtra.DEFAULT_TODAY_FORECAST_TIME);
        if (openTodayForecast) {
            AlarmHelper.setAlarmForTodayForecast(this, todayForecastTime);
        }
    }
}
