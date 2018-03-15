package com.amsavarthan.hify.ui.activities.Extras.Weather.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.basic.GeoActivity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.ui.fragment.SettingsFragment;


/**
 * Settings activity.
 */

public class SettingsActivity extends GeoActivity
        implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
    }

    @Override
    public View getSnackbarContainer() {
        return findViewById(R.id.activity_settings_container);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initToolbar();
            SettingsFragment settingsFragment = new SettingsFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.activity_settings_fragment, settingsFragment)
                    .commit();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // do nothing.
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_settings_toolbar);
        toolbar.setTitle(getString(R.string.action_settings));
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case -1:
                finish();
                break;
        }
    }
}