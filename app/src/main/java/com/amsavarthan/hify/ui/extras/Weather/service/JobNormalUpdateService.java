package com.amsavarthan.hify.ui.extras.Weather.service;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.amsavarthan.hify.ui.extras.Weather.basic.JobUpdateService;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.extras.Weather.utils.remoteView.NormalNotificationUtils;


/**
 * Job updateRotation service.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobNormalUpdateService extends JobUpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {

        if (NormalNotificationUtils.isEnable(context)) {
            NormalNotificationUtils.buildNotificationAndSendIt(context, weather);
        }
    }

    @Override
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, failed);
    }

    @Override
    public void onUpdateCompleted(Location location, Weather weather, Weather old, boolean succeed) {

    }
}
