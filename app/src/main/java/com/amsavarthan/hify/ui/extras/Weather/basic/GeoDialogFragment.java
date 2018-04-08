package com.amsavarthan.hify.ui.extras.Weather.basic;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.amsavarthan.hify.ui.extras.Weather.WeatherExtra;


/**
 * Geometric weather dialog fragment.
 */

public abstract class GeoDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        GeoActivity activity = WeatherExtra.getInstance().getTopActivity();
        if (activity != null) {
            activity.getDialogList().add(this);
        }
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GeoActivity activity = WeatherExtra.getInstance().getTopActivity();
        if (activity != null) {
            activity.getDialogList().remove(this);
        }
    }

    public abstract View getSnackbarContainer();
}
