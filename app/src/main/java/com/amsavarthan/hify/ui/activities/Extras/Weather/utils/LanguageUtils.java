package com.amsavarthan.hify.ui.activities.Extras.Weather.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Language utils.
 */

public class LanguageUtils {

    public static void setLanguage(Context c, String language) {
        Locale target = buildLocale(language);
        if (!c.getResources().getConfiguration().locale.equals(target)) {
            Resources resources = c.getResources();
            Configuration configuration = resources.getConfiguration();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(target);
            }
            resources.updateConfiguration(configuration, metrics);
        }
    }

    private static Locale buildLocale(String language) {
        switch (language) {
            case "follow_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return Resources.getSystem().getConfiguration().getLocales().get(0);
                } else {
                    return Resources.getSystem().getConfiguration().locale;
                }

            case "chinese":
                return new Locale("zh", "CN");

            case "unsimplified_chinese":
                return new Locale("zh", "TW");

            case "english_america":
                return new Locale("en", "US");

            case "english_britain":
                return new Locale("en", "GB");

            case "english_australia":
                return new Locale("en", "AU");

            case "turkish":
                return new Locale("tr");

            case "french":
                return new Locale("fr");

            case "russian":
                return new Locale("ru");

            case "german":
                return new Locale("de");

            case "serbian":
                return new Locale("sr");

            case "spanish":
                return new Locale("es");

            case "italian":
                return new Locale("it");

            default:
                return new Locale("en");
        }
    }

    public static String getLanguageCode(Context c) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = c.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = c.getResources().getConfiguration().locale;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (!TextUtils.isEmpty(country)
                && (country.toLowerCase().equals("tw") || country.toLowerCase().equals("hk"))) {
            return language.toLowerCase() + "-" + country.toLowerCase();
        } else {
            return language.toLowerCase();
        }
    }
}
