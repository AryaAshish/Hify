package com.amsavarthan.hify.ui.extras.Planner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Planner.fragments.PreferenceFragment;
import com.amsavarthan.hify.ui.extras.Planner.utils.DisplayUtil;

;

public class PreferenceActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_with_toolbar);

        DisplayUtil.setWindowTopColor(this, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new PreferenceFragment()).commit();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                overridePendingTransitionExit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}