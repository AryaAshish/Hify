package com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.activities.Extras.Weather.receiver.widget.WidgetWeekProvider;
import com.amsavarthan.hify.ui.activities.Extras.Weather.service.NormalUpdateService;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.ValueUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter.IntentHelper;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter.WeatherHelper;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.manager.TimeManager;

import java.util.Calendar;

/**
 * Widget week utils.
 */

public class WidgetWeekUtils {

    private static final int PENDING_INTENT_CODE = 113;

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        // get settings & time.
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_week_setting),
                Context.MODE_PRIVATE);
        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);

        boolean dayTime = TimeManager.getInstance(context).getDayTime(context, weather, false).isDayTime();

        SharedPreferences defaultSharePreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false);
        String iconStyle = defaultSharePreferences.getString(
                context.getString(R.string.key_widget_icon_style),
                "material");
        boolean touchToRefresh = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh),
                false);

        // get text color.
        int textColor;
        if (blackText || showCard) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        // get remote views.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_week);

        // buildWeather view.

        views.setTextViewText(
                R.id.widget_week_week_1,
                getWeek(context, weather, 0));
        views.setTextViewText(
                R.id.widget_week_week_2,
                getWeek(context, weather, 1));
        views.setTextViewText(
                R.id.widget_week_week_3,
                getWeek(context, weather, 2));
        views.setTextViewText(
                R.id.widget_week_week_4,
                getWeek(context, weather, 3));
        views.setTextViewText(
                R.id.widget_week_week_5,
                getWeek(context, weather, 4));

        views.setTextViewText(
                R.id.widget_week_temp_1,
                getTemp(weather, fahrenheit, 0));
        views.setTextViewText(
                R.id.widget_week_temp_2,
                getTemp(weather, fahrenheit, 1));
        views.setTextViewText(
                R.id.widget_week_temp_3,
                getTemp(weather, fahrenheit, 2));
        views.setTextViewText(
                R.id.widget_week_temp_4,
                getTemp(weather, fahrenheit, 3));
        views.setTextViewText(
                R.id.widget_week_temp_5,
                getTemp(weather, fahrenheit, 4));

        views.setImageViewResource(
                R.id.widget_week_icon_1,
                getIconId(weather, dayTime, iconStyle, blackText, 0));
        views.setImageViewResource(
                R.id.widget_week_icon_2,
                getIconId(weather, dayTime, iconStyle, blackText, 1));
        views.setImageViewResource(
                R.id.widget_week_icon_3,
                getIconId(weather, dayTime, iconStyle, blackText, 2));
        views.setImageViewResource(
                R.id.widget_week_icon_4,
                getIconId(weather, dayTime, iconStyle, blackText, 3));
        views.setImageViewResource(
                R.id.widget_week_icon_5,
                getIconId(weather, dayTime, iconStyle, blackText, 4));

        // set text color.
        views.setTextColor(R.id.widget_week_week_1, textColor);
        views.setTextColor(R.id.widget_week_week_2, textColor);
        views.setTextColor(R.id.widget_week_week_3, textColor);
        views.setTextColor(R.id.widget_week_week_4, textColor);
        views.setTextColor(R.id.widget_week_week_5, textColor);
        views.setTextColor(R.id.widget_week_temp_1, textColor);
        views.setTextColor(R.id.widget_week_temp_2, textColor);
        views.setTextColor(R.id.widget_week_temp_3, textColor);
        views.setTextColor(R.id.widget_week_temp_4, textColor);
        views.setTextColor(R.id.widget_week_temp_5, textColor);
        // set card visibility.
        views.setViewVisibility(R.id.widget_week_card, showCard ? View.VISIBLE : View.GONE);
        // set intent.
        PendingIntent pendingIntent;
        if (touchToRefresh) {
            pendingIntent = PendingIntent.getService(
                    context,
                    PENDING_INTENT_CODE,
                    new Intent(context, NormalUpdateService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    PENDING_INTENT_CODE,
                    IntentHelper.buildMainActivityIntent(context, location),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_week_button, pendingIntent);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetWeekProvider.class),
                views);
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetWeekProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    public static String getWeek(Context context, Weather weather, int index) {
        if (index > 1) {
            return weather.dailyList.get(index).week;
        }

        String firstWeekDay;
        String secondWeekDay;
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String[] weatherDates = weather.base.date.split("-");
        if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month + 1
                && Integer.parseInt(weatherDates[2]) == day) {
            firstWeekDay = context.getString(R.string.today);
            secondWeekDay = weather.dailyList.get(1).week;
        } else if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month + 1
                && Integer.parseInt(weatherDates[2]) == day - 1) {
            firstWeekDay = context.getString(R.string.yesterday);
            secondWeekDay = context.getString(R.string.today);
        } else {
            firstWeekDay = weather.dailyList.get(0).week;
            secondWeekDay = weather.dailyList.get(1).week;
        }

        if (index == 0) {
            return firstWeekDay;
        } else {
            return secondWeekDay;
        }
    }

    public static String getTemp(Weather weather, boolean fahrenheit, int index) {
        return ValueUtils.buildDailyTemp(weather.dailyList.get(index).temps, false, fahrenheit);
    }

    public static int getIconId(Weather weather,
                                boolean dayTime, String iconStyle, boolean blackText, int index) {
        return WeatherHelper.getWidgetNotificationIcon(
                weather.dailyList.get(index).weatherKinds[dayTime ? 0 : 1],
                dayTime, iconStyle, blackText);
    }
}
