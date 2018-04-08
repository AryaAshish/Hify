/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amsavarthan.hify.ui.extras.OurStreets.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.OurStreets.fragment.BackPressAware;
import com.amsavarthan.hify.ui.extras.OurStreets.fragment.GalleryFragment;
import com.firebase.client.Config;
import com.firebase.client.Firebase;

/**
 * The activity hosting all fragments for this application.
 */
public class MainActivity extends AppCompatActivity {

    private FragmentManager mFragmentManager;

    public static void setWindowTopColor(Activity a, @ColorInt int color) {
        if (color == 0) {
            ContextCompat.getColor(a, R.color.colorPrimary);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int c = a instanceof com.amsavarthan.hify.ui.extras.Weather.ui.activity.MainActivity ?
                    color : ContextCompat.getColor(a, R.color.colorPrimary);

            ActivityManager.TaskDescription taskDescription;
            Bitmap topIcon = BitmapFactory.decodeResource(a.getResources(), R.mipmap.world_360);
            taskDescription = new ActivityManager.TaskDescription(
                    a.getString(R.string.app_name_world),
                    topIcon,
                    c);
            a.setTaskDescription(taskDescription);
            topIcon.recycle();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
        // FIXME: This call is necessary when not calling setContentView.
        getDelegate().onPostCreate(null);
        findViewById(android.R.id.content)
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            mFragmentManager.beginTransaction()
                    .replace(android.R.id.content, getGalleryFragment(), GalleryFragment.TAG)
                    .commit();
        }
        setWindowTopColor(this, 0);
    }

    private void initFirebase() {
        Firebase.setAndroidContext(getApplicationContext());
        Config defaultConfig = Firebase.getDefaultConfig();
        if (!defaultConfig.isFrozen()) {
            defaultConfig.setPersistenceEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        /** @see BackPressAware */
        for (Fragment fragment : mFragmentManager.getFragments()) {
            if (fragment instanceof BackPressAware && fragment.isAdded()) {
                // Enable a single fragment to intercept the back press.
                ((BackPressAware) fragment).onBackPressed();
                return;
            }
        }
        // Intercept all back press calls until there's no more fragments on the back stack.
        mFragmentManager.popBackStack();
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
    }

    @NonNull
    private GalleryFragment getGalleryFragment() {
        GalleryFragment fragment = (GalleryFragment) mFragmentManager
                .findFragmentByTag(GalleryFragment.TAG);
        if (fragment == null) {
            fragment = GalleryFragment.newInstance();
        }
        return fragment;
    }
}
