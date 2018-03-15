package com.amsavarthan.hify.ui.activities.Extras.Weather.ui.activity;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.Extras.Weather.basic.GeoActivity;
import com.amsavarthan.hify.ui.activities.Extras.Weather.data.entity.model.weather.Alert;
import com.amsavarthan.hify.ui.activities.Extras.Weather.ui.adapter.AlertAdapter;
import com.amsavarthan.hify.ui.activities.Extras.Weather.ui.decotarion.ListDecoration;

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
        }
    }
}