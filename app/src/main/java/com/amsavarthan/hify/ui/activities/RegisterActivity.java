package com.amsavarthan.hify.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.extras.Planner.utils.AnimationUtil;
import com.amsavarthan.hify.utils.database.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE =100 ;
    private static final int PIC_CROP = 1;
    public Uri imageUri;
    public StorageReference storageReference;
    public ProgressDialog mDialog;
    public String name_, pass_, email_;
    private EditText name,email,password;
    private Button register;
    private CircleImageView profile_image;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private UserHelper userHelper;

    public static void startActivity(Activity activity, Context context, View view) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("images");
        firebaseFirestore=FirebaseFirestore.getInstance();
        imageUri=null;
        userHelper = new UserHelper(this);


        name=(EditText)findViewById(R.id.name);
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        register=(Button)findViewById(R.id.button);

        profile_image=(CircleImageView)findViewById(R.id.profile_image);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Fade fade = new Fade();
            fade.excludeTarget(findViewById(R.id.layout), true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fade.excludeTarget(android.R.id.statusBarBackground, true);
                fade.excludeTarget(android.R.id.navigationBarBackground, true);
                getWindow().setEnterTransition(fade);
                getWindow().setExitTransition(fade);
            }
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(imageUri!=null){
                    name_=name.getText().toString();
                    email_=email.getText().toString();
                    pass_=password.getText().toString();

                    mDialog.show();

                    if (TextUtils.isEmpty(name_)) {

                        AnimationUtil.shakeView(name, RegisterActivity.this);
                        mDialog.dismiss();

                    }
                    if (TextUtils.isEmpty(email_)) {

                        AnimationUtil.shakeView(email, RegisterActivity.this);
                        mDialog.dismiss();

                    }
                    if (TextUtils.isEmpty(pass_)) {

                        AnimationUtil.shakeView(password, RegisterActivity.this);
                        mDialog.dismiss();

                    }

                    if (!TextUtils.isEmpty(name_) || !TextUtils.isEmpty(email_) || !TextUtils.isEmpty(pass_)) {

                        registerUser();

                    }else{

                        AnimationUtil.shakeView(email, RegisterActivity.this);
                        AnimationUtil.shakeView(name, RegisterActivity.this);
                        AnimationUtil.shakeView(password, RegisterActivity.this);
                        mDialog.dismiss();

                    }

                }else{
                    AnimationUtil.shakeView(profile_image, RegisterActivity.this);
                    Toast.makeText(RegisterActivity.this, "We recommend you to set a profile picture", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }

            }
        });

        /*profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Profile Picture"),PICK_IMAGE);
            }
        });*/

    }

    private void registerUser() {

        mAuth.createUserWithEmailAndPassword(email_, pass_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    final String userUid = mAuth.getCurrentUser().getUid();
                    final StorageReference user_profile = storageReference.child(userUid + ".jpg");
                    user_profile.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                final String downloadUri = task.getResult().getDownloadUrl().toString();

                                String token_id = FirebaseInstanceId.getInstance().getToken();

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("id", userUid);
                                userMap.put("name", name_);
                                userMap.put("image", downloadUri);
                                userMap.put("email", email_);
                                userMap.put("token_id", token_id);

                                firebaseFirestore.collection("Users").document(userUid).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        MainActivity.startActivity(RegisterActivity.this);
                                        userHelper.insertContact(name_, email_, downloadUri, pass_);
                                        userHelper.close();
                                        mDialog.dismiss();
                                        LoginActivity.activity.finish();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                            } else {
                                mDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    mDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                imageUri=data.getData();
                // start crop activity
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
                profile_image.setImageURI(imageUri);
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
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

    public void setProfilepic(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE);
    }

}
