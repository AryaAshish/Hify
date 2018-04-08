package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileEdit extends AppCompatActivity {

    private static final int PICK_IMAGE =100 ;
    private FirebaseFirestore mFireStore;
    private CircleImageView imageView;
    private EditText nameText, emailText;
    private Uri imageUri;
    private StorageReference storageReference;
    private UserHelper userHelper;
    private String imag;
    private FrameLayout mFrameLayout;
    private String nam, emai;
    private ProgressDialog mDialog;

    public static void startActivity(Context context){
        Intent intent=new Intent(context,ProfileEdit.class);
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

    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        imageUri=null;
        mFireStore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("images");
        userHelper=new UserHelper(this);
        mFrameLayout = findViewById(R.id.layout);

        imageView = findViewById(R.id.profile_image);
        nameText = findViewById(R.id.name);
        emailText = findViewById(R.id.email);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Profile Picture"),PICK_IMAGE);
            }
        });

        Cursor rs = userHelper.getData(1);
        rs.moveToFirst();

        nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
        imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
        emai = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));

        if (!rs.isClosed()) {
            rs.close();
        }

        nameText.setText(nam);
        emailText.setText(emai);
        Glide.with(ProfileEdit.this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(imag)
                .into(imageView);
        imageUri=Uri.parse(imag);
        userHelper.close();

        /*mFrameLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setAlpha(0.0f);
        mFrameLayout.animate()
                .translationY(mFrameLayout.getHeight())
                .setDuration(500)
                .alpha(1.0f);*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE){
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();
                //start crop activity
                UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                options.setCompressionQuality(100);
                options.setShowCropGrid(true);

                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), "hify_user_profile_picture.png")))
                        .withAspectRatio(1, 1)
                        .withOptions(options)
                        .start(this);

            }
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                imageUri = UCrop.getOutput(data);
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onUpdateClick(View view) {

        if (!TextUtils.isEmpty(nameText.getText().toString()) && !TextUtils.isEmpty(emailText.getText().toString()))
        {

            if(isOnline()){
                performOnlineTask();
            }else{
                performOfflineTask();
            }

        }

    }

    public void performOfflineTask(){

        if(imageUri.equals(Uri.parse(imag))){

            userHelper.updateContactNameandEmail(1, nameText.getText().toString(), emailText.getText().toString());
            Snackbar.make(mFrameLayout, "Profile saved and will be updated, when you get online.", Snackbar.LENGTH_LONG).show();

        }else{

            userHelper.updateContact(1, nameText.getText().toString(), emailText.getText().toString(), imageUri.toString());
            Snackbar.make(mFrameLayout, "Profile saved and will be updated, when you get online.", Snackbar.LENGTH_LONG).show();

        }

    }

    public void performOnlineTask(){

        mDialog.show();

        try {

            Cursor rc = userHelper.getData(1);
            rc.moveToFirst();

            final String password = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_PASS));

            if (!rc.isClosed()) {
                rc.close();
            }


            if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(emailText.getText().toString())) {

                AuthCredential credential = EmailAuthProvider
                        .getCredential(FirebaseAuth.getInstance().getCurrentUser().getEmail(), password);

                FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                FirebaseAuth.getInstance().getCurrentUser().updateEmail(emailText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            if (!imageUri.equals(Uri.parse(imag))) {
                                                final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                StorageReference user_profile = storageReference.child(userUid + ".jpg");
                                                user_profile.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                                                        if (task.isSuccessful()) {

                                                            final String downloadUri = task.getResult().getDownloadUrl().toString();
                                                            String token_id = FirebaseInstanceId.getInstance().getToken();
                                                            final String name_ = nameText.getText().toString();
                                                            final String email_ = emailText.getText().toString();

                                                            Map<String, Object> userMap = new HashMap<>();
                                                            userMap.put("name", name_);
                                                            userMap.put("image", downloadUri);
                                                            userMap.put("email", email_);
                                                            userMap.put("token_id", token_id);

                                                            mFireStore.collection("Users").document(userUid).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    mDialog.dismiss();
                                                                    userHelper.updateContact(1, name_, email_, downloadUri);
                                                                    Snackbar.make(mFrameLayout, "Profile updated..", Snackbar.LENGTH_LONG).show();

                                                                }

                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                    mDialog.dismiss();
                                                                    Toast.makeText(ProfileEdit.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                                }
                                                            });


                                                        } else {
                                                            mDialog.dismiss();
                                                            Toast.makeText(ProfileEdit.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {

                                                final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                                String token_id = FirebaseInstanceId.getInstance().getToken();
                                                final String name_ = nameText.getText().toString();
                                                final String email_ = emailText.getText().toString();

                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("name", name_);
                                                userMap.put("token_id", token_id);
                                                userMap.put("email", email_);

                                                mFireStore.collection("Users").document(userUid).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mDialog.dismiss();
                                                        userHelper.updateContactNameandEmail(1, name_, email_);
                                                        Snackbar.make(mFrameLayout, "Profile updated..", Snackbar.LENGTH_LONG).show();

                                                    }

                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        mDialog.dismiss();
                                                        Toast.makeText(ProfileEdit.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                            }
                                        } else {
                                            mDialog.dismiss();
                                            Toast.makeText(ProfileEdit.this, "Error updating profile, try again!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });

                            }
                        });


            } else {


                if (!imageUri.equals(Uri.parse(imag))) {
                    final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    StorageReference user_profile = storageReference.child(userUid + ".jpg");
                    user_profile.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                final String downloadUri = task.getResult().getDownloadUrl().toString();
                                String token_id = FirebaseInstanceId.getInstance().getToken();
                                final String name_ = nameText.getText().toString();
                                final String email_ = emailText.getText().toString();

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("name", name_);
                                userMap.put("image", downloadUri);
                                userMap.put("email", email_);
                                userMap.put("token_id", token_id);

                                mFireStore.collection("Users").document(userUid).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mDialog.dismiss();
                                        userHelper.updateContact(1, name_, email_, downloadUri);
                                        Snackbar.make(mFrameLayout, "Profile updated..", Snackbar.LENGTH_LONG).show();

                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        mDialog.dismiss();
                                        Toast.makeText(ProfileEdit.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                            } else {
                                mDialog.dismiss();
                                Toast.makeText(ProfileEdit.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {

                    final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    String token_id = FirebaseInstanceId.getInstance().getToken();
                    final String name_ = nameText.getText().toString();
                    final String email_ = emailText.getText().toString();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", name_);
                    userMap.put("token_id", token_id);
                    userMap.put("email", email_);

                    mFireStore.collection("Users").document(userUid).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mDialog.dismiss();
                            userHelper.updateContactNameandEmail(1, name_, email_);
                            Snackbar.make(mFrameLayout, "Profile updated..", Snackbar.LENGTH_LONG).show();

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mDialog.dismiss();
                            Toast.makeText(ProfileEdit.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            }
        } catch (Exception e) {
            mDialog.dismiss();
            Log.e("Update email error", e.getMessage());
        }


    }

    public void onChangePassClicked(View view) {


    }
}
