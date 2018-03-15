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
 * Job tomorrow forecast updateRotation service.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobTomorrowForecastUpdateService extends JobUpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (ForecastNotificationUtils.isEnable(this, false)) {
            ForecastNotificationUtils.buildForecastAndSendIt(context, weather, false);
        }
    }

    @Override
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean openTomorrowForecast = sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false);
        String tomorrowForecastTime = sharedPreferences.getString(
                getString(R.string.key_forecast_tomorrow_time),
                WeatherExtra.DEFAULT_TOMORROW_FORECAST_TIME);
        if (openTomorrowForecast) {
            JobHelper.setJobForTomorrowForecast(this, tomorrowForecastTime);
        }
    }

    @Override
    public void onUpdateCompleted(Location location, Weather weather, Weather old, boolean succeed) {

    }
}
