package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileView extends AppCompatActivity {

    TextView name, email, friends;
    CircleImageView profilePic;
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    FrameLayout mFrameLayout;
    UserHelper userHelper;
    private String imag;

    public static void startActivity(Context context){
        Intent intent=new Intent(context,ProfileView.class);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mFrameLayout.animate()
                .translationY(0)
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mFrameLayout.setVisibility(View.GONE);
                        finish();
                        overridePendingTransitionExit();
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Cursor rs = userHelper.getData(1);
        rs.moveToFirst();

        String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
        String emai = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
        imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));

        if (!rs.isClosed()) {
            rs.close();
        }

        name.setText(nam);
        email.setText(emai);

        Glide.with(ProfileView.this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_6))
                .load(imag)
                .into(profilePic);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        name = findViewById(R.id.username);
        mFrameLayout = findViewById(R.id.layout);
        email = findViewById(R.id.email);
        friends = findViewById(R.id.friends);
        profilePic = findViewById(R.id.profile_pic);

        mFirestore.collection("Users").document(mAuth.getCurrentUser().getUid()).collection("Friends").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                friends.setText(String.format("Total Friends: %d", documentSnapshots.size()));
            }
        });

        userHelper = new UserHelper(this);
        mFrameLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setAlpha(0.0f);

        Cursor rs = userHelper.getData(1);
        rs.moveToFirst();

        String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
        String emai = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
        imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));

        if (!rs.isClosed()) {
            rs.close();
        }

        name.setText(nam);
        email.setText(emai);

        Glide.with(ProfileView.this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_6))
                .load(imag)
                .into(profilePic);


        mFrameLayout.animate()
                .setDuration(500)
                .translationY(mFrameLayout.getHeight())
                .alpha(1.0f);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileView.this, ImagePreviewSave.class)
                        .putExtra("url", imag);
                startActivity(intent);

            }
        });

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

    public void onEditClicked(View view) {

        ProfileEdit.startActivity(this);

    }
}
