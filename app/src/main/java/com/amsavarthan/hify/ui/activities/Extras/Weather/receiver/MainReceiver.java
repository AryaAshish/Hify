package com.amsavarthan.hify.ui.activities.Extras.Weather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter.ServiceHelper;


/**
 * Main receiver.
 */

public class MainReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action) && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            try {
                ServiceHelper.resetNormalService(context, false, true);
                ServiceHelper.resetForecastService(context, true);
                ServiceHelper.resetForecastService(context, false);
            } catch (Exception ignored) {

            }
        }
    }
}