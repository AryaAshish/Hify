package com.amsavarthan.hify.ui.extras.Weather.basic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.WeatherExtra;
import com.amsavarthan.hify.ui.extras.Weather.utils.DisplayUtils;
import com.amsavarthan.hify.ui.extras.Weather.utils.LanguageUtils;

import java.util.ArrayList;
import java.util.List;


public abstract class GeoActivity extends AppCompatActivity {

    private List<GeoDialogFragment> dialogList;
    private boolean started;

    private BroadcastReceiver localeChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && !TextUtils.isEmpty(intent.getAction())
                    && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                LanguageUtils.setLanguage(
                        GeoActivity.this,
                        WeatherExtra.getInstance().getLanguage());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(localeChangedReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
        LanguageUtils.setLanguage(this, WeatherExtra.getInstance().getLanguage());

        WeatherExtra.getInstance().addActivity(this);
        DisplayUtils.setWindowTopColor(this, 0);
        DisplayUtils.setStatusBarTranslate(getWindow());
        DisplayUtils.setNavigationBarColor(this, 0);

        this.dialogList = new ArrayList<>();
        this.started = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WeatherExtra.getInstance().removeActivity();
        unregisterReceiver(localeChangedReceiver);
        overridePendingTransitionExit();
    }

    public View provideSnackbarContainer() {
        if (dialogList.size() > 0) {
            return dialogList.get(dialogList.size() - 1).getSnackbarContainer();
        } else {
            return getSnackbarContainer();
        }
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public abstract View getSnackbarContainer();

    public boolean isStarted() {
        return started;
    }

    public void setStarted() {
        started = true;
    }

    public List<GeoDialogFragment> getDialogList() {
        return dialogList;
    }
}
