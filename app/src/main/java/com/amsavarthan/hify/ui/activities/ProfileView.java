package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileView extends AppCompatActivity {

    TextView name,email,phone,friends;
    CircleImageView profilePic;
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    RelativeLayout mRelativeLayout;
    UserHelper userHelper;
    private int id_To_Update=0;
    private String imag;

    public static void startActivity(Context context){
        Intent intent=new Intent(context,ProfileView.class);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mRelativeLayout.animate()
                .translationY(0)
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mRelativeLayout.setVisibility(View.GONE);
                        finish();
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

        name = (TextView) findViewById(R.id.username);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.layout);
        email = (TextView) findViewById(R.id.email);
        phone = (TextView) findViewById(R.id.phone);
        friends = (TextView) findViewById(R.id.friends);
        profilePic = (CircleImageView) findViewById(R.id.profile_pic);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProfileView.this,ImagePreviewSave.class)
                        .putExtra("url",imag);
                startActivity(intent);

            }
        });

        userHelper = new UserHelper(this);
        mRelativeLayout.setVisibility(View.VISIBLE);
        mRelativeLayout.setAlpha(0.0f);

        Cursor rs = userHelper.getData(1);
        rs.moveToFirst();

        String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
        String phon = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_PHONE));
        String emai = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
        imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));

        if (!rs.isClosed()) {
            rs.close();
        }

        name.setText(nam);
        phone.setText(phon);
        email.setText(emai);

        Glide.with(ProfileView.this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.mipmap.profile_black))
                .load(imag)
                .into(profilePic);

        mRelativeLayout.animate()
                .setDuration(500)
                .translationY(mRelativeLayout.getHeight())
                .alpha(1.0f);
    }
}
