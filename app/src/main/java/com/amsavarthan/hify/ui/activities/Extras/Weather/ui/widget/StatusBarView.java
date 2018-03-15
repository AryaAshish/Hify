package com.amsavarthan.hify.ui.activities.Extras.Weather.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.amsavarthan.hify.ui.activities.Extras.Weather.utils.DisplayUtils;


/**
 * Status bar view.
 */

public class StatusBarView extends FrameLayout {

    public StatusBarView(Context context) {
        super(context);
    }

    public StatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                getResources().getDisplayMetrics().widthPixels,
                DisplayUtils.getStatusBarHeight(getResources()));
    }
}