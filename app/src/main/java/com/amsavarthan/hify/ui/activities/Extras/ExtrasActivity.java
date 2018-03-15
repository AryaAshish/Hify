package com.amsavarthan.hify.ui.activities.Extras;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.ExtrasAdapter;
import com.amsavarthan.hify.models.Extras;
import com.amsavarthan.hify.ui.activities.Extras.Weather.ui.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ExtrasActivity extends AppCompatActivity {

    private List<Extras> extrasList;
    private RecyclerView mRecyclerView;
    private ExtrasAdapter mAdapter;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ExtrasActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extras);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        extrasList = new ArrayList<>();
        mAdapter = new ExtrasAdapter(extrasList, this);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAlpha(0);

        Extras extras = new Extras("Weather", "Get instant weather details", R.mipmap.weather_logo, new Intent(this, MainActivity.class));
        extrasList.add(extras);

        mRecyclerView.setItemAnimator(new FlipInTopXAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);


        mRecyclerView.animate().translationY(mRecyclerView.getHeight())
                .alpha(1.0f).setDuration(100);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mRecyclerView.animate().translationY(0)
                .alpha(.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finish();
                mRecyclerView.setVisibility(View.INVISIBLE);
            }
        });
    }
}
