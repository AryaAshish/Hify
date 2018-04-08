package com.amsavarthan.hify.ui.extras.Weather.basic;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.extras.Weather.utils.NotificationUtils;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.DatabaseHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.PollingUpdateHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.manager.ShortcutsManager;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public abstract class JobUpdateService extends JobService
        implements PollingUpdateHelper.OnPollingUpdateListener {

    private PollingUpdateHelper helper;
    private List<Location> locationList;
    private JobParameters parameters;
    private boolean failed;

    @Override
    public void onCreate() {
        super.onCreate();
        parameters = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        parameters = null;
        if (helper != null) {
            helper.setOnPollingUpdateListener(null);
            helper.cancel();
            helper = null;
        }
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (parameters == null) {
            parameters = jobParameters;
            failed = false;
            locationList = DatabaseHelper.getInstance(this).readLocationList();
            helper = new PollingUpdateHelper(this, locationList);
            helper.setOnPollingUpdateListener(this);
            helper.pollingUpdate();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        parameters = null;
        if (helper != null) {
            helper.setOnPollingUpdateListener(null);
            helper.cancel();
            helper = null;
        }
        return false;
    }

    // control.

    public abstract void updateView(Context context, Location location, @Nullable Weather weather);

    // call jobFinish() in here.
    public abstract void setDelayTask(JobParameters jobParameters, boolean failed);

    // interface.

    // on polling updateRotation listener.

    @Override
    public void onUpdateCompleted(Location location, Weather weather, Weather old, boolean succeed) {
        for (int i = 0; i < locationList.size(); i++) {
            if (locationList.get(i).equals(location)) {
                location.weather = weather;
                locationList.set(i, location);
                if (i == 0) {
                    updateView(this, location, weather);
                    if (succeed) {
                        NotificationUtils.checkAndSendAlert(this, weather, old);
                    } else {
                        failed = true;
                        Toast.makeText(
                                JobUpdateService.this,
                                getString(R.string.feedback_get_weather_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                return;
            }
        }
    }

    @Override
    public void onPollingCompleted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcuts(this, locationList);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDelayTask(parameters, failed);
        }
    }
}
