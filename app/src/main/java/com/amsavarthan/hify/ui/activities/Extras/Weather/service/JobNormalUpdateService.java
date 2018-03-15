package com.amsavarthan.hify.ui.activities.Extras.Weather.service;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.amsavarthan.hify.ui.activities.Extras.Weather.basic.JobUpdateService;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.NormalNotificationUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayDetailsUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayHorizontalUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayVerticalUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayWeekUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetDayUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetDayWeekUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetWeekUtils;


/**
 * Job updateRotation service.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobNormalUpdateService extends JobUpdateService {

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
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, failed);
    }

    @Override
    public void onUpdateCompleted(Location location, Weather weather, Weather old, boolean succeed) {

    }
}
