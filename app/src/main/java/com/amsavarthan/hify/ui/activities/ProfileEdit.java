package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileEdit extends AppCompatActivity {

    private static final int PICK_IMAGE =100 ;
    private FirebaseFirestore mFireStore;
    private CircleImageView imageView;
    private EditText nameText;
    private Uri imageUri;
    private StorageReference storageReference;
    private UserHelper userHelper;
    private String imag;
    private RelativeLayout mRelativeLayout;
    private Button button;
    private String nam;
    private ProgressDialog mDialog;

    public static void startActivity(Context context){
        Intent intent=new Intent(context,ProfileEdit.class);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
        mRelativeLayout = (RelativeLayout) findViewById(R.id.layout);

        imageView=(CircleImageView)findViewById(R.id.profile_image);
        nameText=(EditText)findViewById(R.id.name) ;
        button=(Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUpdateClick(view);
            }
        });

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

        if (!rs.isClosed()) {
            rs.close();
        }

        nameText.setText(nam);
        Glide.with(ProfileEdit.this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.mipmap.profile_black))
                .load(imag)
                .into(imageView);
        imageUri=Uri.parse(imag);
        userHelper.close();

        mRelativeLayout.setVisibility(View.VISIBLE);
        mRelativeLayout.setAlpha(0.0f);
        mRelativeLayout.animate()
                .translationY(mRelativeLayout.getHeight())
                .setDuration(500)
                .alpha(1.0f);

        /*mFireStore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name,image;
                name=documentSnapshot.getString("name");
                image=documentSnapshot.getString("image");

                imageUri=Uri.parse(image);
                RequestOptions placeholderOprions=new RequestOptions();
                placeholderOprions.placeholder(getResources().getDrawable(R.mipmap.profile_black));

                nameText.setText(name);

                Glide.with(getApplicationContext())
                        .setDefaultRequestOptions(placeholderOprions)
                        .load(image)
                        .into(imageView);

                mProgressbar.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
                Log.e("Error: ",".."+e.getMessage());
            }
        });*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                imageUri=data.getData();
                CropImage.activity(imageUri)
                        .start(this);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri= result.getUri();
                imageView.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error cropping: "+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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

        if (!TextUtils.isEmpty(nameText.getText().toString()))
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

            userHelper.updateContactName(1, nameText.getText().toString());
            Toast.makeText(ProfileEdit.this, "Profile updated.", Toast.LENGTH_SHORT).show();

        }else{

            userHelper.updateContactNameandImage(1,nameText.getText().toString(),imageUri.toString());
            Toast.makeText(ProfileEdit.this, "Profile updated.", Toast.LENGTH_SHORT).show();

        }

    }

    public void performOnlineTask(){
        if (!imageUri.equals(Uri.parse(imag))) {
            mDialog.show();
            final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference user_profile = storageReference.child(userUid + ".jpg");
            user_profile.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        final String downloadUri = task.getResult().getDownloadUrl().toString();
                        String token_id = FirebaseInstanceId.getInstance().getToken();
                        final String name_ = nameText.getText().toString();

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name_);
                        userMap.put("image", downloadUri);
                        userMap.put("token_id", token_id);

                        mFireStore.collection("Users").document(userUid).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mDialog.dismiss();
                                userHelper.updateContactNameandImage(1, name_, downloadUri);
                                Toast.makeText(ProfileEdit.this, "Profile updated.", Toast.LENGTH_SHORT).show();

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

            mDialog.show();
            final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String token_id = FirebaseInstanceId.getInstance().getToken();
            final String name_ = nameText.getText().toString();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("name", name_);
            userMap.put("token_id", token_id);

            mFireStore.collection("Users").document(userUid).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mDialog.dismiss();
                    userHelper.updateContactName(1, name_);
                    Toast.makeText(ProfileEdit.this, "Profile updated.", Toast.LENGTH_SHORT).show();

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

}
