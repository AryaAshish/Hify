package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.fragments.FriendsFragment;
import com.amsavarthan.hify.ui.fragments.HifiFragment;
import com.amsavarthan.hify.ui.fragments.ProfileFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FragmentHolder extends AppCompatActivity {

    private String name;
    private ImageView imageView;
    private TextView title;
    private RelativeLayout layout;
    private FrameLayout container;
    private FragmentTransaction transaction;


    public static void startActivity(Context context, String extra, View viewStart, Activity activity,int icon,String color){
        Intent intent=new Intent(context,FragmentHolder.class);
        intent.putExtra("name",extra);
        intent.putExtra("icon",icon);
        intent.putExtra("color",color);
        if(Build.VERSION.SDK_INT>=21){
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                            viewStart,   // Starting view
                            "card"
                    );

            ActivityCompat.startActivity(context, intent, options.toBundle());

        }else{
            context.startActivity(intent);
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        container.animate()
                .translationY(0)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        finish();
                        container.setVisibility(View.GONE);
                        switch (name){
                            case "v_profile":
                                removeFragment(new ProfileFragment());
                                return;
                            case "v_friends":
                                removeFragment(new FriendsFragment());
                                return;
                            case "v_messages":
                                removeFragment(new HifiFragment());
                                return;
                        }
                    }
                });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_holder);

        title=(TextView) findViewById(R.id.title);
        imageView=(ImageView) findViewById(R.id.image);
        layout=(RelativeLayout)findViewById(R.id.layout);
        container=(FrameLayout)findViewById(R.id.container);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        name=getIntent().getStringExtra("name");

        container.setVisibility(View.VISIBLE);
        container.setAlpha(0.0f);

        container.animate()
                .translationY(container.getHeight())
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        switch (name){
                            case "v_profile":
                                setFragment(new ProfileFragment());
                                title.setText("Profile");
                                return;
                            case "v_friends":
                                setFragment(new FriendsFragment());
                                title.setText("Friends");
                                return;
                            case "v_messages":
                                setFragment(new HifiFragment());
                                title.setText("Messages");
                                return;
                        }
                    }
                });


    }

    public void setFragment(Fragment fragment){
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, fragment);
        transaction.commit();
    }

    public void removeFragment(Fragment fragment){
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
    }
}
