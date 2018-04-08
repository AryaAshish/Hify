package com.amsavarthan.hify.ui.extras.Weather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.ServiceHelper;

/**
 * Widget clock day week provider.
 */

public class WidgetClockDayWeekProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ServiceHelper.resetNormalService(context, false, true);
    }
}
