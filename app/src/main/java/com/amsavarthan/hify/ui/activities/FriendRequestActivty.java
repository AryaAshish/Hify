package com.amsavarthan.hify.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestActivty extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView name_text, email_text;
    private String id, name, email, image, token;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;

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
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        profile_image = (CircleImageView) findViewById(R.id.profile_pic);
        name_text = (TextView) findViewById(R.id.username);
        email_text = (TextView) findViewById(R.id.email);

        id = getIntent().getStringExtra("f_id");
        name = getIntent().getStringExtra("f_name");
        email = getIntent().getStringExtra("f_email");
        image = getIntent().getStringExtra("f_image");
        token = getIntent().getStringExtra("f_token");

        name_text.setText(name);
        email_text.setText(email);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(getResources().getDrawable(R.mipmap.profile_black)))
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

        Map<String, Object> updateAccepted = new HashMap<>();
        updateAccepted.put("accepted", true);

        mFirestore.collection("Users").document(mAuth.getCurrentUser().getUid())
                .collection("Friend_Requests").document(email).update(updateAccepted).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Map<String, Object> friendInfo = new HashMap<>();
                friendInfo.put("name", name);
                friendInfo.put("email", email);
                friendInfo.put("id", id);
                friendInfo.put("accepted", true);
                friendInfo.put("image", image);
                friendInfo.put("token_id", token);
                friendInfo.put("notification_id", String.valueOf(System.currentTimeMillis()));
                friendInfo.put("timestamp", String.valueOf(System.currentTimeMillis()));

                mFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Friends/").document(email).set(friendInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFirestore.collection("Users")
                                .document(mAuth.getCurrentUser().getUid())
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                currentuserInfo.put("accepted", true);
                                currentuserInfo.put("image", image_c);
                                currentuserInfo.put("token_id", token_c);
                                currentuserInfo.put("notification_id", String.valueOf(System.currentTimeMillis()));
                                currentuserInfo.put("timestamp", String.valueOf(System.currentTimeMillis()));

                                mFirestore.collection("Users/" + id + "/Friends/")
                                        .document(email_c)
                                        .set(currentuserInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mDialog.dismiss();
                                        Toast.makeText(FriendRequestActivty.this, "Friend request accepted", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mDialog.dismiss();
                                        Log.w("fourth", "listen:error", e);
                                    }
                                });
                                ;

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mDialog.dismiss();
                                Log.w("third", "listen:error", e);
                            }
                        });
                        ;

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


}
