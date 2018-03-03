package com.amsavarthan.hify.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity {

    private EditText email,password;
    private Button login,register;
    private FirebaseAuth mAuth;
    private ProgressBar mBar;
    private FirebaseFirestore mFirestore;

    public static void startActivity(Context context){
        Intent intent=new Intent(context,LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mAuth=FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();

        mBar=(ProgressBar)findViewById(R.id.progressBar2) ;

        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);

        login=(Button)findViewById(R.id.button);
        register=(Button)findViewById(R.id.button2);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email_,pass_;
                email_=email.getText().toString();
                pass_=password.getText().toString();
                if(!TextUtils.isEmpty(email_)||!TextUtils.isEmpty(pass_)){
                    mBar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email_,pass_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                String token_id= FirebaseInstanceId.getInstance().getToken();
                                String current_id=mAuth.getCurrentUser().getUid();

                                Map<String,Object> tokenMap=new HashMap<>();
                                tokenMap.put("token_id",token_id);

                                mFirestore.collection("Users").document(current_id).update(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        MainActivity.startActivity(LoginActivity.this);
                                        finish();
                                        mBar.setVisibility(View.INVISIBLE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(LoginActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                        }
                    });

                }else{
                    Toast.makeText(LoginActivity.this, "Invaild details provided", Toast.LENGTH_SHORT).show();

                }

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity.startActivity(LoginActivity.this);
            }
        });

    }
}
