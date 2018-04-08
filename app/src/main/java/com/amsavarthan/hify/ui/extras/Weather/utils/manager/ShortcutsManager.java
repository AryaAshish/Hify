package com.amsavarthan.hify.ui.extras.Weather.utils.manager;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.extras.Weather.utils.DisplayUtils;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.DatabaseHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.IntentHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.WeatherHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Shortcuts utils.
 */

public class ShortcutsManager {

    @TargetApi(25)
    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static void refreshShortcuts(final Context c, List<Location> locationList) {
        final List<Location> list = new ArrayList<>(locationList.size());
        list.addAll(locationList);

        ThreadManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ShortcutManager shortcutManager = c.getSystemService(ShortcutManager.class);
                if (shortcutManager == null) {
                    return;
                }

                List<ShortcutInfo> shortcutList = new ArrayList<>();
                for (int i = 0; i < list.size() && i < 5; i++) {
                    Icon icon;
                    Weather weather = DatabaseHelper.getInstance(c).readWeather(list.get(i));
                    if (weather != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            try {
                                int size = Math.min((int) DisplayUtils.dpToPx(c, 108), 768);
                                Bitmap foreground = Glide.with(c)
                                        .asBitmap()
                                        .load(WeatherHelper.getShortcutForeground(
                                                weather.realTime.weatherKind,
                                                TimeManager.getInstance(c).isDayTime()))
                                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                                        .apply(new RequestOptions().centerCrop())
                                        .into(size, size)
                                        .get();
                                icon = Icon.createWithAdaptiveBitmap(foreground);
                            } catch (InterruptedException | ExecutionException e) {
                                icon = Icon.createWithResource(
                                        c,
                                        WeatherHelper.getShortcutIcon(
                                                weather.realTime.weatherKind, TimeManager.getInstance(c).isDayTime()));
                            }
                        } else {
                            icon = Icon.createWithResource(
                                    c,
                                    WeatherHelper.getShortcutIcon(
                                            weather.realTime.weatherKind, TimeManager.getInstance(c).isDayTime()));
                        }
                    } else {
                        icon = Icon.createWithResource(c, R.drawable.ic_shortcut_sun_day);
                    }

                    String title = list.get(i).isLocal() ? c.getString(R.string.local) : list.get(i).city;

                    shortcutList.add(
                            new ShortcutInfo.Builder(c, list.get(i).cityId)
                                    .setIcon(icon)
                                    .setShortLabel(title)
                                    .setLongLabel(title)
                                    .setIntent(IntentHelper.buildMainActivityIntent(c, list.get(i)))
                                    .build());
                }

                //shortcutManager.setDynamicShortcuts(shortcutList);
            }
        });
    }
}
