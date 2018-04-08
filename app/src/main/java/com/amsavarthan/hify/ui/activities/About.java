package com.amsavarthan.hify.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.amsavarthan.hify.BuildConfig;
import com.amsavarthan.hify.R;

public class About extends AppCompatActivity {

    private TextView versionText;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, About.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        versionText = findViewById(R.id.version);
        versionText.setText(BuildConfig.VERSION_NAME);

    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
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

    public void launchFPEmail(View view) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, "HIFY: User experience (Report or Feedback) from Device [" + getDeviceName() + "]");
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
    }

    public void launchSEmail(View view) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, "HIFY: Feature suggestion from Device [" + getDeviceName() + "]");
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
    }

    public void launchWeb(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(R.string.web_url)));
        startActivity(intent);
    }

}
