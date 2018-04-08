package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.FriendRequestAdapter;
import com.amsavarthan.hify.utils.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FriendRequestActivty extends AppCompatActivity {

    private static Activity activity;
    private CircleImageView profile_image;
    private TextView name_text, email_text;
    private String id, name, email, image, token;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;
    private boolean job_done = false;
    private HttpsURLConnection connection;

    public static void startActivity(Context context, String name, String email, String image, String id, String token) {
        Intent intent = new Intent(context, FriendRequestActivty.class);
        intent.putExtra("f_id", id)
                .putExtra("f_name", name)
                .putExtra("f_email", email)
                .putExtra("f_image", image)
                .putExtra("f_token", token);
        context.startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (job_done) {
            FriendRequestAdapter.activity.finish();
            finish();
            overridePendingTransitionExit();
        } else {
            finish();
            overridePendingTransitionExit();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        activity = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#212121"));
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        profile_image = (CircleImageView) findViewById(R.id.profile_pic);
        name_text = (TextView) findViewById(R.id.username);
        email_text = (TextView) findViewById(R.id.email);

        job_done = false;

        id = getIntent().getStringExtra("f_id");
        name = getIntent().getStringExtra("f_name");
        email = getIntent().getStringExtra("f_email");
        image = getIntent().getStringExtra("f_image");
        token = getIntent().getStringExtra("f_token");

        name_text.setVisibility(View.VISIBLE);
        name_text.setAlpha(0.0f);

        name_text.animate()
                .setDuration(200)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        name_text.setText(name);
                    }
                }).start();

        email_text.setVisibility(View.VISIBLE);
        email_text.setAlpha(0.0f);

        mFirestore.collection("Users")
                .document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        email_text.animate()
                                .setDuration(200)
                                .alpha(1.0f)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        super.onAnimationStart(animation);
                                        email_text.setText(documentSnapshot.getString("email"));
                                    }
                                }).start();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                });


        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(image)
                .into(profile_image);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendRequestActivty.this, ImagePreview.class)
                        .putExtra("url", image)
                        .putExtra("uri", "");
                startActivity(intent);
            }
        });

    }

    public void onAcceptClick(View view) {

        new BottomDialog.Builder(this)
                .setTitle("Accept Friend Request")
                .setContent("Are you sure do you want to accept " + name + "'s friend request?")
                .setPositiveText("Yes")
                .setPositiveBackgroundColorResource(R.color.colorAccent)
                .setNegativeText("No")
                .setCancelable(false)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        executeTask();
                        dialog.dismiss();
                    }
                }).onNegative(new BottomDialog.ButtonCallback() {
            @Override
            public void onClick(@NonNull BottomDialog dialog) {
                dialog.dismiss();
            }
        }).show();


    }


    public void executeTask() {

        mDialog.show();

        //Delete from friend request
        mFirestore.collection("Users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("Friend_Requests")
                .document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Map<String, Object> friendInfo = new HashMap<>();
                friendInfo.put("name", name);
                friendInfo.put("email", email);
                friendInfo.put("id", id);
                friendInfo.put("image", image);
                friendInfo.put("token_id", token);
                friendInfo.put("notification_id", String.valueOf(System.currentTimeMillis()));
                friendInfo.put("timestamp", String.valueOf(System.currentTimeMillis()));

                //Add data friend to current user
                mFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Friends/")
                        .document(id)
                        .set(friendInfo)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //get the current user data
                        mFirestore.collection("Users")
                                .document(mAuth.getCurrentUser().getUid())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                String name_c = documentSnapshot.getString("name");
                                String email_c = documentSnapshot.getString("email");
                                String id_c = documentSnapshot.getId();
                                String image_c = documentSnapshot.getString("image");
                                String token_c = documentSnapshot.getString("token_id");


                                Map<String, Object> currentuserInfo = new HashMap<>();
                                currentuserInfo.put("name", name_c);
                                currentuserInfo.put("email", email_c);
                                currentuserInfo.put("id", id_c);
                                currentuserInfo.put("image", image_c);
                                currentuserInfo.put("token_id", token_c);
                                currentuserInfo.put("notification_id", String.valueOf(System.currentTimeMillis()));
                                currentuserInfo.put("timestamp", String.valueOf(System.currentTimeMillis()));

                                //Save current user data to Friend
                                mFirestore.collection("Users/" + id + "/Friends/")
                                        .document(id_c)
                                        .set(currentuserInfo)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mDialog.dismiss();
                                        Toast.makeText(FriendRequestActivty.this, "Friend request accepted", Toast.LENGTH_SHORT).show();
                                        job_done = true;
                                        FriendRequestAdapter.activity.finish();
                                        finish();
                                        overridePendingTransitionExit();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mDialog.dismiss();
                                        Log.w("fourth", "listen:error", e);
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mDialog.dismiss();
                                Log.w("third", "listen:error", e);
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Log.w("second", "listen:error", e);
                    }
                });
                ;

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                Log.w("first", "listen:error", e);
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

    public void onDecline(View view) {

        new BottomDialog.Builder(this)
                .setTitle("Decline Friend Request")
                .setContent("Are you sure do you want to decline " + name + "'s friend request?")
                .setPositiveText("Yes")
                .setPositiveBackgroundColorResource(R.color.colorAccent)
                .setNegativeText("No")
                .setCancelable(false)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        executeDeclineTask();
                        dialog.dismiss();
                    }
                }).onNegative(new BottomDialog.ButtonCallback() {
            @Override
            public void onClick(@NonNull BottomDialog dialog) {
                dialog.dismiss();
            }
        }).show();

    }

    private void executeDeclineTask() {

        //delete friend request data
        mFirestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Friend_Requests").document(email).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //new sendNotification().execute(token);
                Toast.makeText(FriendRequestActivty.this, "Friend request denied", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FriendRequestActivty.this, "Some technical error occurred while declining friend request, Try again later.", Toast.LENGTH_SHORT).show();
                Log.i("Error decline", e.getMessage());
            }
        });

    }

    static class sendNotification extends AsyncTask<String, Void, Boolean> {

        private HttpsURLConnection connection;
        private boolean job_done;
        private ProgressDialog mDialog;

        @Override
        protected Boolean doInBackground(final String... strings) {

            //send Push Notification
            connection = null;

            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    try {

                        URL url = new URL("https://fcm.googleapis.com/fcm/send");
                        connection = (HttpsURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("Authorization", "key=" + Config.FIREBASE_AUTH_KEY);

                        JSONObject root = new JSONObject();
                        final JSONObject data = new JSONObject();

                        data.put("notification_id", System.currentTimeMillis());
                        data.put("timestamp", System.currentTimeMillis());
                        data.put("body", "Declined your friend request");
                        data.put("friend_id", documentSnapshot.getString("id"));
                        data.put("friend_name", documentSnapshot.getString("name"));
                        data.put("friend_email", documentSnapshot.getString("email"));
                        data.put("friend_image", documentSnapshot.getString("image"));
                        data.put("friend_token", documentSnapshot.getString("token_id"));
                        data.put("title", documentSnapshot.getString("name"));
                        data.put("click_action", "com.amsavarthan.hify.TARGET_DECLINED");

                        root.put("data", data);
                        root.put("to", strings[0]);

                        byte[] outputBytes = root.toString().getBytes("UTF-8");
                        OutputStream os = connection.getOutputStream();
                        os.write(outputBytes);
                        os.flush();
                        os.close();
                        connection.getInputStream();

                        job_done = true;

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (connection != null) connection.disconnect();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Error", e.getMessage());
                }
            });

            return job_done;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(activity);
            mDialog.setMessage("Please wait...");
            mDialog.setIndeterminate(true);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (job_done) {
                mDialog.dismiss();
                FriendRequestAdapter.activity.finish();
                FriendRequestActivty.activity.finish();
            }
        }
    }


}
