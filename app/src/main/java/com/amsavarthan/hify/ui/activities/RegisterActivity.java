package com.amsavarthan.hify.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE =100 ;
    private EditText name,email,password;
    private Button register,login;
    private CircleImageView profile_image;
    public static Uri imageUri;
    private FirebaseAuth mAuth;
    public static StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    public static ProgressBar mBar;
    private EditText phone;
    public static String phonenumber,name_,pass_,email_;

    public static void startActivity(Context context){
        Intent intent=new Intent(context,RegisterActivity.class);
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

        mBar=(ProgressBar)findViewById(R.id.progressBar3) ;

        name=(EditText)findViewById(R.id.name);
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        phone=(EditText)findViewById(R.id.phone);

        login=(Button)findViewById(R.id.button2);
        register=(Button)findViewById(R.id.button);

        profile_image=(CircleImageView)findViewById(R.id.profile_image);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(imageUri!=null){
                    mBar.setVisibility(View.VISIBLE);
                    name_=name.getText().toString();
                    email_=email.getText().toString();
                    pass_=password.getText().toString();
                    phonenumber=phone.getText().toString();


                    if(!TextUtils.isEmpty(phonenumber)&&!TextUtils.isEmpty(name_)||!TextUtils.isEmpty(email_)||!TextUtils.isEmpty(pass_)){

                        PhoneVerifyActivity.startActivity(RegisterActivity.this);

                    }else{
                        Toast.makeText(RegisterActivity.this, "Invaild details provided", Toast.LENGTH_SHORT).show();
                        mBar.setVisibility(View.INVISIBLE);

                    }

                }else{
                    Toast.makeText(RegisterActivity.this, "We recommend you to set a profile picture", Toast.LENGTH_SHORT).show();
                    mBar.setVisibility(View.INVISIBLE);
                }

            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Profile Picture"),PICK_IMAGE);
            }
        });

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
                Uri resultUri = result.getUri();
                imageUri=resultUri;
                profile_image.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
