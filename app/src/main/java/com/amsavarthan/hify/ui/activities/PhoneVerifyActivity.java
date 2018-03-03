package com.amsavarthan.hify.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.utils.database.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.amsavarthan.hify.ui.activities.RegisterActivity.email_;
import static com.amsavarthan.hify.ui.activities.RegisterActivity.name_;
import static com.amsavarthan.hify.ui.activities.RegisterActivity.phonenumber;

public class PhoneVerifyActivity extends AppCompatActivity {

    private static final String TAG = "PhoneVerifyActivity";
    LinearLayout bottomLayout,progressLayout;
    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth mAuth;
    private EditText codeText;
    private FirebaseFirestore firebaseFirestore;
    private TextView phone;
    private TextView progressText;
    private UserHelper userHelper;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verify);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        bottomLayout=(LinearLayout)findViewById(R.id.bottomLayout);
        progressLayout=(LinearLayout)findViewById(R.id.progressBar);
        codeText=(EditText)findViewById(R.id.codeText);
        phone=(TextView)findViewById(R.id.phone);
        progressText=(TextView)findViewById(R.id.progressText);
        userHelper=new UserHelper(this);

        phone.setText(phonenumber);
        progressText.setText("Waiting to automatically verify..");
        sendCode();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        verificationCallbacks=null;
    }

    public void sendCode() {

        setUpVerificatonCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
                20,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks);
    }

    private void setUpVerificatonCallbacks() {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            PhoneAuthCredential credential) {
                        codeText.setText("");
                       signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            if(e.getLocalizedMessage().contains("phone number provided is incorrect")){
                                Toast.makeText(PhoneVerifyActivity.this, "Invalid phone number, no country code has been provided", Toast.LENGTH_SHORT).show();
                            finish();
                            }else{
                                Log.d(TAG, "Invalid credential: "
                                        + e.getLocalizedMessage());
                                Toast.makeText(PhoneVerifyActivity.this, "Invalid credential: "
                                        + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }

                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                            Toast.makeText(PhoneVerifyActivity.this, "Try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {

                        phoneVerificationId = verificationId;
                        resendToken = token;

                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        bottomLayout.setVisibility(View.VISIBLE);
                        progressLayout.setVisibility(View.GONE);
                    }
                };
    }

    public void verifyCode(View view) {

        String code = codeText.getText().toString();

        if(!TextUtils.isEmpty(code)) {
            PhoneAuthCredential credential =
                    PhoneAuthProvider.getCredential(phoneVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        }
    }

    public static void startActivity(Context context){
        Intent intent=new Intent(context,PhoneVerifyActivity.class);
        context.startActivity(intent);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.createUserWithEmailAndPassword(email_, RegisterActivity.pass_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    progressText.setText("Setting up your account..");
                    final String userUid=mAuth.getCurrentUser().getUid();
                    StorageReference user_profile= RegisterActivity.storageReference.child(userUid+".jpg");
                    user_profile.putFile(RegisterActivity.imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){

                                final String downloadUri=task.getResult().getDownloadUrl().toString();

                                String token_id= FirebaseInstanceId.getInstance().getToken();

                                Map<String,Object> userMap=new HashMap<>();
                                userMap.put("name", name_);
                                userMap.put("image",downloadUri);
                                userMap.put("email",email_);
                                userMap.put("token_id",token_id);
                                userMap.put("phone", phonenumber);

                                firebaseFirestore.collection("Users").document(userUid).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        MainActivity.startActivity(PhoneVerifyActivity.this);
                                        userHelper.insertContact(name_, phonenumber, email_,downloadUri);
                                        RegisterActivity.mBar.setVisibility(View.INVISIBLE);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        RegisterActivity.mBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(PhoneVerifyActivity.this,"Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }else{
                                RegisterActivity.mBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(PhoneVerifyActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    RegisterActivity.mBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(PhoneVerifyActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void resendCode(View view) {

        setUpVerificatonCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
                20,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);

        bottomLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);

    }


}
