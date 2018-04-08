package com.amsavarthan.hify.ui.extras.Weather.utils;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.WeatherExtra;
import com.amsavarthan.hify.ui.extras.Weather.basic.GeoActivity;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.verticalScrollView.SwipeSwitchLayout;


/**
 * Notification utils.
 */

public class SnackbarUtils {

    public static void showSnackbar(String txt) {
        GeoActivity activity = WeatherExtra.getInstance().getTopActivity();
        if (activity != null) {
            View view = activity.provideSnackbarContainer();
            if (view instanceof SwipeSwitchLayout) {
                SwipeSwitchLayout switchView = (SwipeSwitchLayout) view;
                Snackbar.make(
                        switchView,
                        txt,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(
                        view,
                        txt,
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    public static void showSnackbar(String txt, String action, View.OnClickListener l) {
        GeoActivity activity = WeatherExtra.getInstance().getTopActivity();
        if (activity != null) {
            View view = activity.provideSnackbarContainer();
            if (view instanceof SwipeSwitchLayout) {
                SwipeSwitchLayout switchView = (SwipeSwitchLayout) view;
                Snackbar.make(
                        switchView,
                        txt,
                        Snackbar.LENGTH_LONG)
                        .setAction(action, l)
                        .setActionTextColor(
                                ContextCompat.getColor(
                                        WeatherExtra.getInstance(),
                                        R.color.colorTextAlert))
                        .show();
            } else {
                Snackbar.make(
                        view,
                        txt,
                        Snackbar.LENGTH_LONG)
                        .setAction(action, l)
                        .setActionTextColor(
                                ContextCompat.getColor(
                                        WeatherExtra.getInstance(),
                                        R.color.colorTextAlert))
                        .show();
            }
        }
    }
}
