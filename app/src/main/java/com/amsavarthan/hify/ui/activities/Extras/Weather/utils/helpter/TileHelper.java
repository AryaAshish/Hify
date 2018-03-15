package com.amsavarthan.hify.ui.activities.Extras.Weather.utils.helpter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.support.annotation.RequiresApi;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.ValueUtils;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.manager.TimeManager;

/**
 * Tile helper.
 */

public class TileHelper {
    // data
    private static final String PREFERENCE_NAME = "geometric_weather_tile";
    private static final String KEY_ENABLE = "enable";

    /**
     * <br> data
     */

    public static void setEnable(Context context, boolean enable) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_ENABLE, enable);
        editor.apply();

        ServiceHelper.resetNormalService(context, false, true);
    }

    public static boolean isEnable(Context context) {
        return context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ENABLE, false);
    }

    /**
     * <br> UI.
     */

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void refreshTile(Context context, Tile tile) {
        if (tile == null) {
            return;
        }
        Location location = DatabaseHelper.getInstance(context).readLocationList().get(0);
        location.weather = DatabaseHelper.getInstance(context).readWeather(location);
        if (location.weather != null) {
            boolean f = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.key_fahrenheit), false);
            tile.setIcon(
                    Icon.createWithResource(
                            context,
                            WeatherHelper.getNotificationWeatherIcon(
                                    location.weather.realTime.weatherKind,
                                    TimeManager.getInstance(context).isDayTime())));
            tile.setLabel(
                    ValueUtils.buildCurrentTemp(
                            location.weather.realTime.temp,
                            false,
                            f));
            tile.updateTile();
        }
    }
}
