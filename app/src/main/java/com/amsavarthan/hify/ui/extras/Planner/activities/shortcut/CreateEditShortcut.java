package com.amsavarthan.hify.ui.extras.Planner.activities.shortcut;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.inputmethod.InputMethodManager;

import com.amsavarthan.hify.ui.extras.Planner.activities.CreateEditActivity;

public class CreateEditShortcut extends CreateEditActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
        }, 300);
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }
}