package com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.circularSkyView;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.StatusBarView;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.WeatherView;
import com.amsavarthan.hify.ui.extras.Weather.ui.widget.weatherView.WeatherViewController;
import com.amsavarthan.hify.ui.extras.Weather.utils.DisplayUtils;
import com.amsavarthan.hify.ui.extras.Weather.utils.helpter.WeatherHelper;
import com.amsavarthan.hify.ui.extras.Weather.utils.manager.TimeManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Circular sky view.
 */

public class CircularSkyView extends FrameLayout
        implements WeatherView, WeatherIconControlView.OnWeatherIconChangingListener {

    @WeatherKindRule
    private int weatherKind = WEATHER_KING_NULL;

    private LinearLayout container;
    private StatusBarView statusBar;
    private WeatherIconControlView controlView;
    private CircleView circleView;
    private FrameLayout starContainer;
    private ImageView[] flagIcons;

    private int[] imageIds;
    private boolean animating = false;

    private AnimatorSet[] iconTouchAnimators;
    private AnimatorSet[] starShineAnimators;

    private int firstCardMarginTop;
    private AnimatorListenerAdapter[] starShineAnimatorListeners = new AnimatorListenerAdapter[]{

            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    starShineAnimators[0].start();
                }
            },

            new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    starShineAnimators[1].start();
                }
            }
    };

    public CircularSkyView(Context context) {
        super(context);
        this.initialize();
    }

    public CircularSkyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public CircularSkyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_circular_sky_view, null);
        addView(view);

        this.container = findViewById(R.id.container_circular_sky_view);

        this.statusBar = findViewById(R.id.container_circular_sky_view_statusBar);
        setStatusBarColor();

        this.controlView = findViewById(R.id.container_circular_sky_view_controller);
        controlView.setOnWeatherIconChangingListener(this);

        this.circleView = findViewById(R.id.container_circular_sky_view_circularSkyView);

        this.starContainer = findViewById(R.id.container_circular_sky_view_starContainer);
        if (TimeManager.getInstance(getContext()).isDayTime()) {
            starContainer.setAlpha(0);
        } else {
            starContainer.setAlpha(1);
        }

        findViewById(R.id.container_circular_sky_view_iconContainer).setVisibility(GONE);

        this.flagIcons = new ImageView[]{
                findViewById(R.id.container_circular_sky_view_icon_1),
                findViewById(R.id.container_circular_sky_view_icon_2),
                findViewById(R.id.container_circular_sky_view_icon_3)};
        imageIds = new int[3];
        iconTouchAnimators = new AnimatorSet[3];

        ImageView[] starts = new ImageView[]{
                findViewById(R.id.container_circular_sky_view_star_1),
                findViewById(R.id.container_circular_sky_view_star_2)};
        Glide.with(getContext())
                .load(R.drawable.star_1)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(starts[0]);
        Glide.with(getContext())
                .load(R.drawable.star_2)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(starts[1]);

        this.starShineAnimators = new AnimatorSet[]{
                (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.start_shine_1),
                (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.start_shine_2)};
        for (int i = 0; i < starShineAnimators.length; i++) {
            starShineAnimators[i].addListener(starShineAnimatorListeners[i]);
            starShineAnimators[i].setTarget(starts[i]);
            starShineAnimators[i].start();
        }

        this.firstCardMarginTop = (int) (getResources().getDisplayMetrics().widthPixels / 6.8 * 5.0
                + DisplayUtils.getStatusBarHeight(getResources()) - DisplayUtils.dpToPx(getContext(), 28));
    }

    public void showCircles() {
        circleView.showCircle(TimeManager.getInstance(getContext()).isDayTime());
        changeStarAlPha();
    }

    private void setStatusBarColor() {
        if (TimeManager.getInstance(getContext()).isDayTime()) {
            statusBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightPrimary_5));
        } else {
            statusBar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.darkPrimary_5));
        }
    }

    private void setFlagIconsImage() {
        for (int i = 0; i < flagIcons.length; i++) {
            if (imageIds[i] == 0) {
                flagIcons[i].setVisibility(GONE);
            } else {
                Glide.with(getContext())
                        .load(imageIds[i])
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                        .into(flagIcons[i]);
                flagIcons[i].setVisibility(VISIBLE);
            }
        }
    }

    private void changeStarAlPha() {
        starContainer.clearAnimation();

        StarAlphaAnimation animation = new StarAlphaAnimation(
                starContainer.getAlpha(), TimeManager.getInstance(getContext()).isDayTime() ? 0 : 1);
        animation.setDuration(500);
        starContainer.startAnimation(animation);
    }

    @Override
    public void setWeather(@WeatherKindRule int weatherKind) {
        this.weatherKind = weatherKind;

        String entityWeatherKind = WeatherViewController.getEntityWeatherKind(weatherKind);
        boolean isDay = TimeManager.getInstance(getContext()).isDayTime();

        int[] animatorIds = WeatherHelper.getAnimatorId(entityWeatherKind, isDay);
        iconTouchAnimators = new AnimatorSet[animatorIds.length];
        for (int i = 0; i < iconTouchAnimators.length; i++) {
            if (animatorIds[i] != 0) {
                iconTouchAnimators[i] = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), animatorIds[i]);
                iconTouchAnimators[i].addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        animating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animating = false;
                    }
                });
                iconTouchAnimators[i].setTarget(flagIcons[i]);
            }
        }

        imageIds = WeatherHelper.getWeatherIcon(entityWeatherKind, isDay);

        setStatusBarColor();
        showCircles();
        controlView.showWeatherIcon();
    }

    // interface.

    @Override
    public void onClick() {
        circleView.touchCircle();
        if (!animating) {
            for (int i = 0; i < flagIcons.length; i++) {
                if (imageIds[i] != 0 && iconTouchAnimators[i] != null) {
                    iconTouchAnimators[i].start();
                }
            }
        }
    }

    @Override
    public void onScroll(int scrollY) {
        container.setTranslationY(
                (float) (-(circleView.getMeasuredHeight() + statusBar.getMeasuredHeight())
                        * Math.min(1, 1.0 * scrollY / firstCardMarginTop)));
    }

    @Override
    public int getWeatherKind() {
        return weatherKind;
    }

    @Override
    public int[] getThemeColors() {
        return new int[]{
                TimeManager.getInstance(getContext()).isDayTime() ?
                        ContextCompat.getColor(getContext(), R.color.lightPrimary_3)
                        :
                        ContextCompat.getColor(getContext(), R.color.darkPrimary_1),
                ContextCompat.getColor(getContext(), R.color.lightPrimary_5),
                ContextCompat.getColor(getContext(), R.color.darkPrimary_1)
        };
    }

    @Override
    public int getFirstCardMarginTop() {
        return firstCardMarginTop;
    }

    @Override
    public void OnWeatherIconChanging() {
        setFlagIconsImage();
        for (int i = 0; i < flagIcons.length; i++) {
            if (imageIds[i] != 0 && iconTouchAnimators[i] != null) {
                iconTouchAnimators[i].start();
            }
        }
    }

    private class StarAlphaAnimation extends Animation {

        private float startAlpha;
        private float endAlpha;

        StarAlphaAnimation(float startAlpha, float endAlpha) {
            this.startAlpha = startAlpha;
            this.endAlpha = endAlpha;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            starContainer.setAlpha(startAlpha + (endAlpha - startAlpha) * interpolatedTime);
        }
    }
}
