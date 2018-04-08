package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserDetail extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String userid = null, image = null, name = null, email = null, current_user_id = null, user_token = null;
    private CircleImageView profile;
    private TextView nameT, emailT, friendsT;
    private HashMap<String, Object> userMap;
    private List<String> mutual;
    private View view;
    private TextView mutualfriendsT;

    public static void startActivity(Context context, String userid, String name, String email, String image, String token) {

        Intent intent = new Intent(context, UserDetail.class)
                .putExtra("f_id", userid)
                .putExtra("f_name", name)
                .putExtra("f_email", email)
                .putExtra("f_image", image)
                .putExtra("f_token", token);
        context.startActivity(intent);

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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#212121"));
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mutual = new ArrayList<>();
        mutual.clear();

        current_user_id = mAuth.getCurrentUser().getUid();
        userid = getIntent().getStringExtra("f_id");
        name = getIntent().getStringExtra("f_name");
        email = getIntent().getStringExtra("f_email");
        image = getIntent().getStringExtra("f_image");
        user_token = getIntent().getStringExtra("f_token");

        profile = findViewById(R.id.profile_pic);
        nameT = findViewById(R.id.username);
        emailT = findViewById(R.id.email);
        friendsT = findViewById(R.id.friends);
        mutualfriendsT = findViewById(R.id.friends_mutual);
        view = findViewById(R.id.layout);

        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(image)
                .into(profile);

        getDetails();
        getFriendsCount();
        getMutualFriends();

    }

    private void getDetails() {

        mFirestore.collection("Users").document(userid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {

                nameT.setVisibility(View.VISIBLE);
                nameT.setAlpha(0.0f);
                nameT.animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);

                                name = documentSnapshot.getString("name");
                                nameT.setText(name);

                            }
                        }).start();

                emailT.setVisibility(View.VISIBLE);
                emailT.setAlpha(0.0f);
                emailT.animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);

                                email = documentSnapshot.getString("email");
                                emailT.setText(email);

                            }
                        }).start();

            }
        });

    }

    private void getMutualFriends() {
        mFirestore.collection("Users").document(current_user_id).collection("Friends").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                    final String docid = doc.getDocument().getId();

                    mFirestore.collection("Users")
                            .document(userid)
                            .collection("Friends")
                            .document(docid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                mutual.add(documentSnapshot.getString("name"));
                                Log.i("mutual", documentSnapshot.getString("name"));
                            }
                        }
                    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isComplete() && task.isSuccessful()) {

                                if (mutual.size() != 0) {
                                    mutualfriendsT.setVisibility(View.VISIBLE);
                                    mutualfriendsT.setAlpha(0.0f);

                                    mutualfriendsT.animate()
                                            .alpha(1.0f)
                                            .setDuration(200)
                                            .setListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationStart(Animator animation) {
                                                    super.onAnimationStart(animation);

                                                    if (mutual.size() >= 4) {
                                                        mutualfriendsT.setText(String.format(Locale.ENGLISH, "Mutual Friends: %s, %s, %s and %d more", mutual.get(0), mutual.get(1), mutual.get(2), mutual.size() - 3));
                                                    } else if (mutual.size() < 4 && mutual.size() > 1) {
                                                        mutualfriendsT.setText(String.format(Locale.ENGLISH, "Mutual Friends: %s, %s and %d more", mutual.get(0), mutual.get(1), mutual.size() - 2));
                                                    } else if (mutual.size() == 1) {
                                                        mutualfriendsT.setText(String.format(Locale.ENGLISH, "Mutual Friend: %s", mutual.get(0)));
                                                    }

                                                }
                                            }).start();
                                }
                            }

                        }
                    });

                }

            }
        });
    }

    private void getFriendsCount() {
        mFirestore.collection("Users").document(userid).collection("Friends").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot documentSnapshots) {

                friendsT.setVisibility(View.VISIBLE);
                friendsT.setAlpha(0.0f);
                friendsT.animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);

                                friendsT.setText(String.format(Locale.ENGLISH, "Total Friends: %d", documentSnapshots.size()));

                            }
                        }).start();
            }
        });
    }

    public void onSendClick(View view) {

        SendActivity.startActivityExtra(this, userid);

    }


    public void onRemoveFriend(View view) {

        new BottomDialog.Builder(this)
                .setTitle("Unfriend " + name)
                .setContent("Are you sure do you want to remove " + name + " from your friend list?")
                .setPositiveText("Yes")
                .setPositiveBackgroundColorResource(R.color.colorAccentt)
                .setNegativeText("No")
                .setCancelable(false)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        executeUnfriend();
                        dialog.dismiss();
                    }
                }).onNegative(new BottomDialog.ButtonCallback() {
            @Override
            public void onClick(@NonNull BottomDialog dialog) {
                dialog.dismiss();
            }
        }).show();

    }

    private void executeUnfriend() {

        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends").document(email).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(userid)
                        .collection("Friends")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                        overridePendingTransitionExit();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Error", "listen:error", e);
            }
        });

    }

    public void onFriendsClick(View view) {


    }
}
