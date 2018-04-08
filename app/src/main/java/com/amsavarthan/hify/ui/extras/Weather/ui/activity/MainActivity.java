package com.amsavarthan.hify.ui.extras.Weather.ui.activity;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.WeatherExtra;
import com.amsavarthan.hify.ui.extras.Weather.basic.GeoActivity;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.History;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.Location;
import com.amsavarthan.hify.ui.extras.Weather.data.entity.model.weather.Weather;
import com.amsavarthan.hify.ui.extras.Weather.ui.adapter.DetailsAdapter;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.InkPageIndicator;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.NoneSlipRecyclerView;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.StatusBarView;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.trendView.TrendRecyclerView;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.trendView.TrendViewController;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.verticalScrollView.SwipeSwitchLayout;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.verticalScrollView.VerticalNestedScrollView;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.verticalScrollView.VerticalSwipeRefreshLayout;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.WeatherView;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.WeatherViewController;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;
import com.amsavarthan.hify.ui.extras.Weather.utils.DisplayUtils;
import com.amsavarthan.hify.ui.extras.Weather.utils.NotificationUtils;
import com.amsavarthan.hify.ui.extras.Weather.utils.SafeHandler;
import com.amsavarthan.hify.ui.extras.Weather.utils.SnackbarUtils;
import com.amsavarthan.hify.ui.extras.Weather.utils.ValueUtils;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.DatabaseHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.IntentHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.LocationHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.ServiceHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.WeatherHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.manager.ShortcutsManager;
import com.amsavarthan.hify.ui.extras.Weather.utils.manager.ThreadManager;
import com.amsavarthan.hify.ui.extras.Weather.utils.manager.TimeManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Main activity.
 */

public class MainActivity extends GeoActivity
        implements View.OnClickListener, Toolbar.OnMenuItemClickListener,
        SwipeSwitchLayout.OnSwitchListener, SwipeRefreshLayout.OnRefreshListener,
        LocationHelper.OnRequestLocationListener, WeatherHelper.OnRequestWeatherListener,
        SafeHandler.HandlerContainer {

    public static final int SETTINGS_ACTIVITY = 1;
    public static final int MANAGE_ACTIVITY = 2;
    public static final int MESSAGE_WHAT_STARTUP_SERVICE = 1;
    public static final String KEY_MAIN_ACTIVITY_LOCATION = "MAIN_ACTIVITY_LOCATION";
    private final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;
    public Location locationNow;
    private SafeHandler<MainActivity> handler;
    private StatusBarView statusBar;
    private WeatherView weatherView;
    private LinearLayout appBar;
    private Toolbar toolbar;
    private InkPageIndicator indicator;
    private SwipeSwitchLayout switchLayout;
    private VerticalSwipeRefreshLayout refreshLayout;
    private VerticalNestedScrollView scrollView;
    private LinearLayout cardContainer;
    private TextView realtimeTemp;
    private TextView realtimeWeather;
    private TextView realtimeSendibleTemp;
    private TextView aqiOrWind;
    private ImageView timeIcon;
    private TextView refreshTime;
    private TextView firstTitle;
    private TrendRecyclerView firstTrendRecyclerView;
    private TextView secondTitle;
    private TrendRecyclerView secondTrendRecyclerView;
    private TextView detailsTitle;
    private NoneSlipRecyclerView detailRecyclerView;
    private AnimatorSet initAnimator;
    private List<Location> locationList;
    private WeatherHelper weatherHelper;
    private LocationHelper locationHelper;
    private View.OnTouchListener indicatorStateListener = new View.OnTouchListener() {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    indicator.setDisplayState(true);
                    break;

                case MotionEvent.ACTION_UP:
                    indicator.setDisplayState(false);
                    break;
            }
            return false;
        }
    };


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.key_ui_style), "material")
                .equals("material")) {
            setContentView(R.layout.activity_main_material);
        } else {
            setContentView(R.layout.activity_main_circular);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Location old = locationNow;
        readLocationList();
        readIntentData(intent);
        if (!old.equals(locationNow)) {
            reset();
        }
    }

    @Override
    @SuppressLint("SimpleDateFormat")
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();
            reset();
        } else {
            Weather old = locationNow.weather;
            readLocationList();
            for (int i = 0; i < locationList.size(); i++) {
                if (locationList.get(i).equals(locationNow)) {
                    locationNow = locationList.get(i);
                    break;
                }
            }
            if (!refreshLayout.isRefreshing()
                    && locationNow.weather != null && old != null
                    && locationNow.weather.base.timeStamp > old.base.timeStamp) {
                reset();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_ACTIVITY:
                DisplayUtils.setNavigationBarColor(this, weatherView.getThemeColors()[0]);
                NotificationUtils.refreshNotificationInNewThread(this, locationList.get(0));
                break;

            case MANAGE_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    readLocationList();
                    readIntentData(data);
                    switchLayout.setData(locationList, locationNow);
                    reset();
                } else {
                    readLocationList();
                    for (int i = 0; i < locationList.size(); i++) {
                        if (locationNow.equals(locationList.get(i))) {
                            switchLayout.setData(locationList, locationNow);
                            return;
                        }
                    }
                    locationNow = locationList.get(0);
                    switchLayout.setData(locationList, locationNow);
                    reset();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        indicator.setSwitchView(switchLayout);
        if (locationList.size() > 1) {
            indicator.setVisibility(View.VISIBLE);
        } else {
            indicator.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHelper.cancel();
        weatherHelper.cancel();
        handler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // do nothing.
    }

    // init.

    @Override
    public View getSnackbarContainer() {
        return switchLayout;
    }

    private void initData() {
        readLocationList();
        readIntentData(getIntent());

        this.weatherHelper = new WeatherHelper();
        this.locationHelper = new LocationHelper(this);
    }

    private void readLocationList() {
        this.locationList = DatabaseHelper.getInstance(this).readLocationList();
        for (int i = 0; i < locationList.size(); i++) {
            locationList.get(i).weather = DatabaseHelper.getInstance(this)
                    .readWeather(locationList.get(i));
            if (locationList.get(i).weather != null) {
                locationList.get(i).history = DatabaseHelper.getInstance(this)
                        .readHistory(locationList.get(i).weather);
            }
        }
    }

    private void readIntentData(Intent intent) {
        String locationName = intent.getStringExtra(KEY_MAIN_ACTIVITY_LOCATION);
        if (TextUtils.isEmpty(locationName) && locationNow == null) {
            locationNow = locationList.get(0);
            return;
        } else if (!TextUtils.isEmpty(locationName)) {
            for (int i = 0; i < locationList.size(); i++) {
                if (locationList.get(i).isLocal() && locationName.equals(getString(R.string.local))) {
                    if (locationNow == null || !locationNow.equals(locationList.get(i))) {
                        locationNow = locationList.get(i);
                        return;
                    }
                } else if (locationList.get(i).city.equals(locationName)) {
                    if (locationNow == null || !locationNow.city.equals(locationName)) {
                        locationNow = locationList.get(i);
                        return;
                    }
                }
            }
        }
        if (locationNow == null) {
            locationNow = locationList.get(0);
        }
    }

    // control.

    private void initWidget() {
        this.handler = new SafeHandler<>(this);

        this.statusBar = findViewById(R.id.activity_main_statusBar);

        this.weatherView = findViewById(R.id.activity_main_weatherView);
        if (weatherView instanceof MaterialWeatherView) {
            int kind;
            if (locationNow.weather == null) {
                kind = WeatherView.WEATHER_KIND_CLEAR_DAY;
            } else {
                kind = WeatherViewController.getWeatherViewWeatherKind(
                        locationNow.weather.realTime.weatherKind,
                        TimeManager.getInstance(this).isDayTime());
            }
            weatherView.setWeather(kind);
            ((MaterialWeatherView) weatherView).setOpenGravitySensor(
                    PreferenceManager.getDefaultSharedPreferences(this)
                            .getBoolean(getString(R.string.key_gravity_sensor_switch), true));
        }

        this.appBar = findViewById(R.id.activity_main_appBar);

        this.toolbar = findViewById(R.id.activity_main_toolbar);
        toolbar.inflateMenu(R.menu.activity_main);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setOnClickListener(this);
        toolbar.setOnMenuItemClickListener(this);

        this.switchLayout = findViewById(R.id.activity_main_switchView);
        switchLayout.setData(locationList, locationNow);
        switchLayout.setOnSwitchListener(this);
        switchLayout.setOnTouchListener(indicatorStateListener);

        this.refreshLayout = findViewById(R.id.activity_main_refreshView);
        int startPosition = (int) (DisplayUtils.getStatusBarHeight(getResources())
                + DisplayUtils.dpToPx(this, 16));
        refreshLayout.setProgressViewOffset(
                false, startPosition, startPosition + refreshLayout.getProgressViewEndOffset());
        refreshLayout.setOnRefreshListener(this);
        if (weatherView instanceof MaterialWeatherView) {
            refreshLayout.setColorSchemeColors(weatherView.getThemeColors()[0]);
        }

        this.scrollView = findViewById(R.id.activity_main_scrollView);
        scrollView.setOnScrollChangeListener(new OnScrollListener(weatherView.getFirstCardMarginTop()));
        scrollView.setOnTouchListener(indicatorStateListener);

        this.cardContainer = findViewById(R.id.activity_main_cardContainer);

        RelativeLayout baseView = findViewById(R.id.container_main_base_view);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) baseView.getLayoutParams();
        params.height = weatherView.getFirstCardMarginTop();
        baseView.setLayoutParams(params);
        baseView.setOnClickListener(this);

        this.realtimeTemp = findViewById(R.id.container_main_base_view_tempTxt);
        this.realtimeWeather = findViewById(R.id.container_main_base_view_weatherTxt);
        this.realtimeSendibleTemp = findViewById(R.id.container_main_base_view_sendibleTempTxt);
        this.aqiOrWind = findViewById(R.id.container_main_base_view_aqiOrWindTxt);

        findViewById(R.id.container_main_trend_first_card_timeContainer).setOnClickListener(this);

        this.timeIcon = findViewById(R.id.container_main_trend_first_card_timeIcon);
        timeIcon.setOnClickListener(this);

        this.refreshTime = findViewById(R.id.container_main_trend_first_card_timeText);

        this.firstTitle = findViewById(R.id.container_main_trend_first_card_title);
        this.firstTrendRecyclerView = findViewById(R.id.container_main_trend_first_card_trendRecyclerView);

        this.secondTitle = findViewById(R.id.container_main_trend_second_card_title);
        this.secondTrendRecyclerView = findViewById(R.id.container_main_trend_second_card_trendRecyclerView);

        this.detailsTitle = findViewById(R.id.container_main_details_card_title);
        this.detailRecyclerView = findViewById(R.id.container_main_details_card_recyclerView);

        this.indicator = findViewById(R.id.activity_main_indicator);
    }

    public void reset() {
        DisplayUtils.setWindowTopColor(this, weatherView.getThemeColors()[0]);
        DisplayUtils.setNavigationBarColor(this, weatherView.getThemeColors()[0]);

        if (locationNow.weather == null) {
            if (TextUtils.isEmpty(locationNow.city)) {
                toolbar.setTitle(getString(R.string.local));
            } else {
                toolbar.setTitle(locationNow.city);
            }
        } else {
            toolbar.setTitle(locationNow.weather.base.city);
        }

        cardContainer.setVisibility(View.GONE);
        scrollView.scrollTo(0, 0);

        switchLayout.reset();
        switchLayout.setEnabled(true);

        if (locationNow.weather == null) {
            setRefreshing(true);
            onRefresh();
        } else {
            boolean valid = locationNow.weather.isValid(4);
            setRefreshing(!valid);
            buildUI();
            if (!valid) {
                onRefresh();
            }
        }
    }

    private void setRefreshing(final boolean b) {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(b);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void buildUI() {
        Weather weather = locationNow.weather;
        History history = locationNow.history;

        if (weather == null) {
            return;
        } else {
            TimeManager.getInstance(this).getDayTime(this, weather, true);
        }
        boolean dayTime = TimeManager.getInstance(this).isDayTime();

        WeatherViewController.setWeatherViewWeatherKind(weatherView, weather, dayTime);

        DisplayUtils.setWindowTopColor(this, weatherView.getThemeColors()[0]);
        DisplayUtils.setNavigationBarColor(this, weatherView.getThemeColors()[0]);

        toolbar.setTitle(weather.base.city);
        refreshLayout.setColorSchemeColors(weatherView.getThemeColors()[0]);

        realtimeTemp.setText(
                ValueUtils.buildAbbreviatedCurrentTemp(
                        weather.realTime.temp,
                        WeatherExtra.getInstance().isFahrenheit()));
        realtimeWeather.setText(weather.realTime.weather);
        realtimeSendibleTemp.setText(
                getString(R.string.feels_like) + " "
                        + ValueUtils.buildAbbreviatedCurrentTemp(
                        weather.realTime.sensibleTemp, WeatherExtra.getInstance().isFahrenheit()));

        if (weather.aqi == null) {
            aqiOrWind.setText(weather.realTime.windLevel);
        } else {
            aqiOrWind.setText(weather.aqi.quality);
        }

        if (weather.alertList.size() == 0) {
            timeIcon.setEnabled(false);
            timeIcon.setImageResource(R.drawable.ic_time);
        } else {
            timeIcon.setEnabled(true);
            timeIcon.setImageResource(R.drawable.ic_alert);
        }
        refreshTime.setText(weather.base.time);

        firstTitle.setTextColor(weatherView.getThemeColors()[0]);
        secondTitle.setTextColor(weatherView.getThemeColors()[0]);
        detailsTitle.setTextColor(weatherView.getThemeColors()[0]);

        if (WeatherExtra.getInstance().getCardOrder().equals("daily_first")) {
            TrendViewController.setDailyTrend(
                    this, firstTitle, firstTrendRecyclerView,
                    weather, history, weatherView.getThemeColors());
            TrendViewController.setHourlyTrend(
                    this, secondTitle, secondTrendRecyclerView,
                    weather, history, weatherView.getThemeColors());
        } else {
            TrendViewController.setHourlyTrend(
                    this, firstTitle, firstTrendRecyclerView,
                    weather, history, weatherView.getThemeColors());
            TrendViewController.setDailyTrend(
                    this, secondTitle, secondTrendRecyclerView,
                    weather, history, weatherView.getThemeColors());
        }

        detailRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        detailRecyclerView.setAdapter(new DetailsAdapter(this, weather));

        cardContainer.setVisibility(View.VISIBLE);
        if (initAnimator != null) {
            initAnimator.cancel();
        }
        initAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_in);
        initAnimator.setTarget(cardContainer);
        initAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        initAnimator.start();

        if (locationNow.equals(locationList.get(0))) {
            startupService();
        }
    }

    private void setLocationAndReset(Location location) {
        this.locationNow = location;
        reset();
    }

    private void refreshLocation(Location location) {
        for (int i = 0; i < locationList.size(); i++) {
            if (locationList.get(i).equals(location)) {
                locationList.set(i, location);
                return;
            }
        }
    }

    // permission.

    private void startupService() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.obtainMessage(MESSAGE_WHAT_STARTUP_SERVICE).sendToTarget();
            }
        }, 1500);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission(int permissionCode) {
        switch (permissionCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(
                            new String[]{
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSIONS_REQUEST_CODE);
                } else {
                    locationHelper.requestLocation(this, locationNow, this);
                }
                break;
        }
    }

    // interface.

    // on click listener.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permission, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_request_location_permission_success));
                    if (locationNow.isLocal()) {
                        locationHelper.requestLocation(this, locationNow, this);
                    }
                } else {
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_request_location_permission_failed));
                }
                break;
        }
    }

    // on menu item click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_main_base_view:
                weatherView.onClick();
                break;

            case R.id.container_main_trend_first_card_timeIcon:
                IntentHelper.startAlertActivity(this, locationNow.weather);
                break;

            case R.id.container_main_trend_first_card_timeContainer:
                IntentHelper.startManageActivityForResult(this);
                break;
        }
    }

    // on touch listener.

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_manage:
                IntentHelper.startManageActivityForResult(this);
                break;

            case R.id.action_settings:
                IntentHelper.startSettingsActivityForResult(this);
                break;


        }
        return true;
    }

    // on swipe listener(swipe switch layout).

    @Override
    public void swipeTakeEffect(int direction) {
        switchLayout.setEnabled(false);
        for (int i = 0; i < locationList.size(); i++) {
            if (locationList.get(i).equals(locationNow)) {
                int position = direction == SwipeSwitchLayout.DIRECTION_LEFT ?
                        i + 1 : i - 1;
                if (position < 0) {
                    position = locationList.size() - 1;
                } else if (position > locationList.size() - 1) {
                    position = 0;
                }
                setLocationAndReset(locationList.get(position));
                return;
            }
        }
        setLocationAndReset(locationList.get(0));
    }

    // on refresh listener.

    @Override
    public void onRefresh() {
        locationHelper.cancel();
        weatherHelper.cancel();

        if (locationNow.isLocal()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                locationHelper.requestLocation(this, locationNow, this);
            } else {
                requestPermission(LOCATION_PERMISSIONS_REQUEST_CODE);
            }
        } else {
            weatherHelper.requestWeather(this, locationNow, this);
        }
    }

    // on scroll changed listener.

    @Override
    public void requestLocationSuccess(Location requestLocation, boolean locationChanged) {
        if (!requestLocation.isUsable()) {
            requestLocationFailed(requestLocation);
        } else if (locationNow.equals(requestLocation)) {
            locationNow = requestLocation;
            refreshLocation(locationNow);
            DatabaseHelper.getInstance(this).writeLocation(locationNow);
            weatherHelper.requestWeather(this, locationNow, this);
        }
    }

    // on request location listener.

    @Override
    public void requestLocationFailed(Location requestLocation) {
        if (locationNow.equals(requestLocation)) {
            if (locationNow.weather == null && locationNow.isUsable()) {
                weatherHelper.requestWeather(this, locationNow, this);
            } else {
                setRefreshing(false);
            }

            IntentHelper.startManageActivityForResult(this);
            Toast.makeText(this, "Location failed, Search for your city manually", Toast.LENGTH_SHORT).show();
            SnackbarUtils.showSnackbar(getString(R.string.feedback_location_failed));
        }
    }

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        if (locationNow.equals(requestLocation)) {
            if (weather == null) {
                requestWeatherFailed(requestLocation);
            } else if (locationNow.weather == null
                    || !locationNow.weather.base.date.equals(weather.base.date)
                    || !locationNow.weather.base.time.equals(weather.base.time)) {
                locationNow.weather = weather;
                locationNow.history = DatabaseHelper.getInstance(this).readHistory(weather);
                refreshLocation(locationNow);
                DatabaseHelper.getInstance(this).writeWeather(locationNow, weather);
                DatabaseHelper.getInstance(this).writeHistory(weather);

                setRefreshing(false);
                buildUI();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutsManager.refreshShortcuts(this, locationList);
                }
            } else {
                setRefreshing(false);
            }
        }
    }

    // on request weather listener.

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        if (locationNow.equals(requestLocation)) {
            if (locationNow.weather == null) {
                locationNow.weather = DatabaseHelper.getInstance(this).readWeather(locationNow);
                if (locationNow.weather != null) {
                    locationNow.history = DatabaseHelper.getInstance(this).readHistory(locationNow.weather);
                }

                refreshLocation(locationNow);
                SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));

                setRefreshing(false);
                buildUI();
            } else {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
                setRefreshing(false);
            }
        }
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case MESSAGE_WHAT_STARTUP_SERVICE:
                NotificationUtils.refreshNotificationInNewThread(this, locationList.get(0));
                ThreadManager.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        ServiceHelper.resetNormalService(MainActivity.this, true, false);
                        ServiceHelper.resetForecastService(MainActivity.this, true);
                        ServiceHelper.resetForecastService(MainActivity.this, false);
                    }
                });
                break;
        }
    }

    // handler container.

    private class OnScrollListener implements NestedScrollView.OnScrollChangeListener {

        private int firstCardMarginTop;
        private int overlapTriggerDistance;

        OnScrollListener(int firstCardMarginTop) {
            this.firstCardMarginTop = firstCardMarginTop;
            this.overlapTriggerDistance = firstCardMarginTop
                    - DisplayUtils.getStatusBarHeight(getResources());
        }

        @Override
        public void onScrollChange(NestedScrollView v,
                                   int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            weatherView.onScroll(scrollY);

            // set translation y of toolbar.
            if (scrollY < firstCardMarginTop - appBar.getMeasuredHeight() - realtimeTemp.getMeasuredHeight()) {
                appBar.setTranslationY(0);
            } else if (scrollY > firstCardMarginTop - appBar.getY()) {
                appBar.setTranslationY(-appBar.getMeasuredHeight());
            } else {
                appBar.setTranslationY(
                        firstCardMarginTop - realtimeTemp.getMeasuredHeight() - scrollY
                                - appBar.getMeasuredHeight());
            }

            // set status bar style.
            if (oldScrollY < overlapTriggerDistance && overlapTriggerDistance <= scrollY) {
                DisplayUtils.setStatusBarStyleWithScrolling(getWindow(), statusBar, true);
            } else if (oldScrollY >= overlapTriggerDistance && overlapTriggerDistance > scrollY) {
                DisplayUtils.setStatusBarStyleWithScrolling(getWindow(), statusBar, false);
            }
        }
    }
}