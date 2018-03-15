package com.amsavarthan.hify.ui.activities.Extras.Weather.service;

import android.app.job.JobParameters;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.WeatherExtra;
import com.amsavarthan.hify.ui.activities.Extras.Weather.basic.JobUpdateService;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter.JobHelper;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.ForecastNotificationUtils;


/**
 * Job today forecast updateRotation service.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobTodayForecastUpdateService extends JobUpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (ForecastNotificationUtils.isEnable(this, true)) {
            ForecastNotificationUtils.buildForecastAndSendIt(context, weather, true);
        }
    }

    @Override
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean openTodayForecast = sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false);
        String todayForecastTime = sharedPreferences.getString(
                getString(R.string.key_forecast_today_time),
                WeatherExtra.DEFAULT_TODAY_FORECAST_TIME);
        if (openTodayForecast) {
            JobHelper.setJobForTodayForecast(this, todayForecastTime);
        }
    }

    @Override
    public void onUpdateCompleted(Location location, Weather weather, Weather old, boolean succeed) {

    }
}
