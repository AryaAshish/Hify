package com.amsavarthan.hify.ui.activities.Extras.Weather.utils;

import android.content.Context;
import android.text.TextPaint;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.manager.ThreadManager;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.NormalNotificationUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayHorizontalUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayVerticalUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetClockDayWeekUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetDayUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetDayWeekUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.remoteView.WidgetWeekUtils;

import java.util.Calendar;


/**
 * Widget utils.
 */

public class WidgetUtils {

    public static void refreshWidgetInNewThread(final Context context,
                                                final Location location) {
        ThreadManager.getInstance()
                .execute(new Runnable() {
                    @Override
                    public void run() {
                        if (WidgetDayUtils.isEnable(context)) {
                            WidgetDayUtils.refreshWidgetView(context, location, location.weather);
                        }
                        if (WidgetWeekUtils.isEnable(context)) {
                            WidgetWeekUtils.refreshWidgetView(context, location, location.weather);
                        }
                        if (WidgetDayWeekUtils.isEnable(context)) {
                            WidgetDayWeekUtils.refreshWidgetView(context, location, location.weather);
                        }
                        if (WidgetClockDayHorizontalUtils.isEnable(context)) {
                            WidgetClockDayHorizontalUtils.refreshWidgetView(context, location, location.weather);
                        }
                        if (WidgetClockDayVerticalUtils.isEnable(context)) {
                            WidgetClockDayVerticalUtils.refreshWidgetView(context, location, location.weather);
                        }
                        if (WidgetClockDayWeekUtils.isEnable(context)) {
                            WidgetClockDayWeekUtils.refreshWidgetView(context, location, location.weather);
                        }
                        if (NormalNotificationUtils.isEnable(context)) {
                            NormalNotificationUtils.buildNotificationAndSendIt(context, location.weather);
                        }
                    }
                });
    }

    public static String[] buildWidgetDayStyleText(Weather weather, boolean fahrenheit) {
        String[] texts = new String[]{
                weather.realTime.weather,
                ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit),
                ValueUtils.buildAbbreviatedCurrentTemp(weather.dailyList.get(0).temps[0], fahrenheit),
                ValueUtils.buildAbbreviatedCurrentTemp(weather.dailyList.get(0).temps[1], fahrenheit)};

        TextPaint paint = new TextPaint();

        float[] widths = new float[4];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = paint.measureText(texts[i]);
        }

        float maxiWidth = widths[0];
        for (float w : widths) {
            if (w > maxiWidth) {
                maxiWidth = w;
            }
        }

        while (true) {
            boolean[] flags = new boolean[]{false, false, false, false};

            for (int i = 0; i < 2; i++) {
                if (widths[i] < maxiWidth) {
                    texts[i] = texts[i] + " ";
                    widths[i] = paint.measureText(texts[i]);
                } else {
                    flags[i] = true;
                }
            }
            for (int i = 2; i < 4; i++) {
                if (widths[i] < maxiWidth) {
                    texts[i] = " " + texts[i];
                    widths[i] = paint.measureText(texts[i]);
                } else {
                    flags[i] = true;
                }
            }

            int n = 0;
            for (boolean flag : flags) {
                if (flag) {
                    n++;
                }
            }
            if (n == 4) {
                break;
            }
        }

        return new String[]{
                texts[0] + "\n" + texts[1],
                texts[2] + "\n" + texts[3]};
    }

    public static String getWeek(Context context) {
        Calendar c = Calendar.getInstance();
        int week = c.get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case Calendar.SUNDAY:
                return context.getString(R.string.week_7);

            case Calendar.MONDAY:
                return context.getString(R.string.week_1);

            case Calendar.TUESDAY:
                return context.getString(R.string.week_2);

            case Calendar.WEDNESDAY:
                return context.getString(R.string.week_3);

            case Calendar.THURSDAY:
                return context.getString(R.string.week_4);

            case Calendar.FRIDAY:
                return context.getString(R.string.week_5);

            case Calendar.SATURDAY:
                return context.getString(R.string.week_6);

            default:
                return "";
        }
    }
}
