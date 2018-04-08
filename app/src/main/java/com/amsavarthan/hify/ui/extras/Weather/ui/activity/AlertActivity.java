package com.amsavarthan.hify.ui.extras.Weather.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.basic.GeoActivity;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.weather.Alert;
import com.amsavarthan.hify.ui.extras.Weather.ui.adapter.AlertAdapter;
import com.amsavarthan.hify.ui.extras.Weather.ui.decotarion.ListDecoration;

import java.util.List;

/**
 * Alert activity.
 */

public class AlertActivity extends GeoActivity
        implements View.OnClickListener {

    public static final String KEY_ALERT_ACTIVITY_ALERT_LIST = "ALERT_ACTIVITY_ALERT_LIST";
    private CoordinatorLayout container;
    private List<Alert> alarmList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // do nothing.
        super.onSaveInstanceState(outState);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    private void initData() {
        this.alarmList = getIntent().getParcelableArrayListExtra(KEY_ALERT_ACTIVITY_ALERT_LIST);
    }

    private void initWidget() {
        this.container = (CoordinatorLayout) findViewById(R.id.activity_alert_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_alert_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_alert_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new ListDecoration(this));
        recyclerView.setAdapter(new AlertAdapter(alarmList));
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case -1:
                finish();
                overridePendingTransitionExit();
        }
    }
}