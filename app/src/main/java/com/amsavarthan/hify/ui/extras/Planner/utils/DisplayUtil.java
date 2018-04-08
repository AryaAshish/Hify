package com.amsavarthan.hify.ui.extras.Planner.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Weather.ui.activity.MainActivity;

/**
 * Created by amsavarthan on 20/3/18.
 */

public class DisplayUtil {

    public static void setWindowTopColor(Activity a, @ColorInt int color) {
        if (color == 0) {
            ContextCompat.getColor(a, R.color.primary);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int c = a instanceof MainActivity ?
                    color : ContextCompat.getColor(a, R.color.primary);

            ActivityManager.TaskDescription taskDescription;
            Bitmap topIcon = BitmapFactory.decodeResource(a.getResources(), R.mipmap.planner);
            taskDescription = new ActivityManager.TaskDescription(
                    a.getString(R.string.app_name_planner),
                    topIcon,
                    c);
            a.setTaskDescription(taskDescription);
            topIcon.recycle();
        }
    }

}
