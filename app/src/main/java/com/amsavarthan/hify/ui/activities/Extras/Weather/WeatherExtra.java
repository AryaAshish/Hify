package com.amsavarthan.hify.ui.activities.Extras.Weather;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.basic.GeoActivity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.LanguageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Geometric realTimeWeather.
 */

public class WeatherExtra extends Application {

    public static final String DEFAULT_TODAY_FORECAST_TIME = "07:00";
    public static final String DEFAULT_TOMORROW_FORECAST_TIME = "21:00";
    private static WeatherExtra instance;
    private List<GeoActivity> activityList;
    private String cardOrder;
    private boolean colorNavigationBar;
    private boolean fahrenheit;
    private boolean imperial;
    private String language;

    public static WeatherExtra getInstance() {
        return instance;
    }

    public static String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = getProcessName();
        if (!TextUtils.isEmpty(processName)
                && processName.equals(this.getPackageName())) {
            initialize();
        }
    }

    private void initialize() {
        instance = this;
        activityList = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        cardOrder = sharedPreferences.getString(getString(R.string.key_card_order), "daily_first");
        colorNavigationBar = sharedPreferences.getBoolean(getString(R.string.key_navigationBar_color), false);
        fahrenheit = sharedPreferences.getBoolean(getString(R.string.key_fahrenheit), false);
        imperial = sharedPreferences.getBoolean(getString(R.string.key_imperial), false);
        language = sharedPreferences.getString(getString(R.string.key_language), "follow_system");

        LanguageUtils.setLanguage(this, language);
    }

    public void addActivity(GeoActivity a) {
        activityList.add(a);
    }

    public void removeActivity() {
        activityList.remove(activityList.size() - 1);
    }

    @Nullable
    public GeoActivity getTopActivity() {
        if (activityList.size() == 0) {
            return null;
        }
        return activityList.get(activityList.size() - 1);
    }

    public String getCardOrder() {
        return cardOrder;
    }

    public void setCardOrder(String cardOrder) {
        this.cardOrder = cardOrder;
    }

    public boolean isColorNavigationBar() {
        return colorNavigationBar;
    }

    public void setColorNavigationBar() {
        this.colorNavigationBar = !colorNavigationBar;
    }

    public boolean isFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(boolean fahrenheit) {
        this.fahrenheit = fahrenheit;
    }

    public boolean isImperial() {
        return imperial;
    }

    public void setImperial(boolean imperial) {
        this.imperial = imperial;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
