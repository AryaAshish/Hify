package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Planner.utils.AnimationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PostImage extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private EditText mEditText;
    private ImageView mImageView;
    private Uri imageUri;
    private TextView mHelpText;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PostImage.class);
        context.startActivity(intent);
    }

    @NonNull
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("New Image Post");

        try {
            getSupportActionBar().setTitle("New Image Post");
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mEditText = findViewById(R.id.text);
        mImageView = findViewById(R.id.post_image);
        mHelpText = findViewById(R.id.help_text);
        imageUri = null;

    }

    public void onImageSelect(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), PICK_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();
                //start crop activity
                UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                options.setCompressionQuality(100);
                options.setShowCropGrid(true);

                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), String.format("hify_user_post_%s.png", random()))))
                        .withAspectRatio(1, 1)
                        .withOptions(options)
                        .start(this);

            }
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                imageUri = UCrop.getOutput(data);
                mImageView.setImageURI(imageUri);
                mHelpText.animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mHelpText.setVisibility(View.INVISIBLE);
                            }
                        }).start();
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_text_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_post:

                if (TextUtils.isEmpty(imageUri.toString()))
                    AnimationUtil.shakeView(mImageView, PostImage.this);

                if (TextUtils.isEmpty(mEditText.getText().toString()))
                    AnimationUtil.shakeView(mEditText, PostImage.this);

                if (!TextUtils.isEmpty(mEditText.getText().toString()) && !TextUtils.isEmpty(imageUri.toString()))
                    sendPost();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendPost() {

        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Posting...");
        mDialog.setProgress(0);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        StorageReference post_images = FirebaseStorage.getInstance().getReference().child("post_images").child(random() + ".jpg");
        post_images.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                if (task.isComplete() && task.getResult().getDownloadUrl().toString() != null) {

                    mFirestore.collection("Users").document(mCurrentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            Map<String, Object> postMap = new HashMap<>();
                            postMap.put("userId", documentSnapshot.getString("id"));
                            postMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                            postMap.put("image", task.getResult().getDownloadUrl().toString());
                            postMap.put("likes", "0");
                            postMap.put("favourites", "0");
                            postMap.put("description", mEditText.getText().toString());
                            postMap.put("color", "0");

                            mFirestore.collection("Posts")
                                    .document(mCurrentUser.getUid())
                                    .collection("All Posts")
                                    .add(postMap)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            mDialog.dismiss();
                                            Toast.makeText(PostImage.this, "Post sent", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mDialog.dismiss();
                                            Log.e("Error sending post", e.getMessage());
                                        }
                                    });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Log.e("Error getting user", e.getMessage());
                        }
                    });

                } else {
                    mDialog.dismiss();
                    Log.e("Error getting user", task.getException().getMessage() + "..");
                }

            }
        });
    }


}
