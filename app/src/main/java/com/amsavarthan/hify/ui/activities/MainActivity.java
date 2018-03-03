package com.amsavarthan.hify.ui.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.amsavarthan.hify.Manifest;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.CardPagerAdapter;
import com.amsavarthan.hify.adapters.PagerViewAdapter;
import com.amsavarthan.hify.anim.Transformer;
import com.amsavarthan.hify.models.CardItem;
import com.amsavarthan.hify.utils.NetworkUtil;
import com.amsavarthan.hify.utils.database.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private ImageView profileLabel,friendsLabel,hifiLabel;
    private ViewPager mainpager;
    private PagerViewAdapter mPagerViewAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    public static String userId;
    public static Activity activity;
    private UserHelper userHelper;

    private CardPagerAdapter mCardAdapter;
    private Transformer mCardShadowTransformer;
    private String nam,imag;
    private StorageReference storageReference;



    public static void startActivity(Context context){
        Intent intent=new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentuser!=null)
        {
            try {
                performUploadTask();
            }catch (Exception e){
                Log.e("Error","."+e.getLocalizedMessage());
            }
        }else{
            LoginActivity.startActivity(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        userHelper=new UserHelper(this);
        activity=this;

        registerReceiver(NetworkChangeReceiver
                ,new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();

        if(currentuser==null){
            LoginActivity.startActivity(MainActivity.this);
        }else {

            Dexter.withActivity(this)
                    .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(MainActivity.this, "We need storage permission for downloading images, please grant it from settings", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        }
                    });

            userId = currentuser.getUid();
            storageReference = FirebaseStorage.getInstance().getReference().child("images").child(currentuser.getUid() + ".jpg");;

            performLogintask();

            mainpager = (ViewPager) findViewById(R.id.mainpager);
            profileLabel = (ImageView) findViewById(R.id.profileLabel);
            friendsLabel = (ImageView) findViewById(R.id.usersLabel);
            hifiLabel = (ImageView) findViewById(R.id.notificationLabel);

            //mPagerViewAdapter=new PagerViewAdapter(getSupportFragmentManager());
            mCardAdapter = new CardPagerAdapter(this);
            mCardAdapter.addCardItem(new CardItem(R.string.posts, R.string.text_1, R.mipmap.feed_white, "#3cba54", "View Feed", "Add a new post" ));
            mCardAdapter.addCardItem(new CardItem(R.string.friends, R.string.text_1, R.mipmap.friends, "#4885ed", "My Friends", "Add a Friend"));
            mCardAdapter.addCardItem(new CardItem(R.string.messages, R.string.text_1, R.mipmap.message, "#db3236", "Send a message", "View Messages"));
            mCardAdapter.addCardItem(new CardItem(R.string.profile, R.string.text_1, R.mipmap.profile, "#f4c20d", "View Profile", "Edit Profile"));

            try {
                performUploadTask();
            }catch (Exception e){
                Log.e("Error","."+e.getLocalizedMessage());
            }

            mCardShadowTransformer = new Transformer(mainpager, mCardAdapter);

            mainpager.setAdapter(mCardAdapter);
            mainpager.setPageTransformer(false, mCardShadowTransformer);
            mainpager.setOffscreenPageLimit(3);

        }
        /*
        profileLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainpager.setCurrentItem(0);
            }
        });

        friendsLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainpager.setCurrentItem(1);
            }
        });

        hifiLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainpager.setCurrentItem(2);
            }
        });
        */

    }

    private void performLogintask() {

        Cursor rs=userHelper.getData(1);
        rs.moveToFirst();

        String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));

        if(!rs.isClosed())
            rs.close();

        if(nam==null) {
            FirebaseFirestore.getInstance().collection("Users").document(currentuser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    userHelper.insertContact(
                            documentSnapshot.getString("name")
                            ,documentSnapshot.getString("phone")
                            ,documentSnapshot.getString("email")
                            ,documentSnapshot.getString("image")
                    );
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Error",".."+e.getMessage());
                }
            });
        }

    }

    public void performUploadTask(){
        if(isOnline()){
            Cursor rc =userHelper.getData(1);
            rc.moveToFirst();

            nam=rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
            imag=rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));

            if(!rc.isClosed()){
                rc.close();
            }

            FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    String name=documentSnapshot.getString("name");
                    String image=documentSnapshot.getString("image");
                    if(!image.equals(imag)){
                        storageReference.putFile(Uri.parse(imag)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){
                                    final String downloadUri = task.getResult().getDownloadUrl().toString();

                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("image", downloadUri);

                                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            userHelper.updateContactImage(1, downloadUri);

                                        }

                                    });
                                }
                            }
                        });
                    }
                    if(!name.equals(nam)){
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", nam);

                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                userHelper.updateContactName(1, nam);

                            }

                        });
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(NetworkChangeReceiver);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    //add it in onPageSelected
    private void changeTabs(int position) {

       if(position==0){

           profileLabel.setBackgroundColor(getResources().getColor(R.color.blackDarkOverlay));
           //profileLabel.setTextSize(22);

           friendsLabel.setBackgroundColor(getResources().getColor(android.R.color.transparent));
           //friendsLabel.setTextSize(16);

           hifiLabel.setBackgroundColor(getResources().getColor(android.R.color.transparent));
           //hifiLabel.setTextSize(16);

       }else if(position==1){

           profileLabel.setBackgroundColor(getResources().getColor(android.R.color.transparent));
           //profileLabel.setTextSize(16);

           friendsLabel.setBackgroundColor(getResources().getColor(R.color.blackDarkOverlay));
           //friendsLabel.setTextSize(22);

           hifiLabel.setBackgroundColor(getResources().getColor(android.R.color.transparent));
           //hifiLabel.setTextSize(16);

       }else if(position==2){

           profileLabel.setBackgroundColor(getResources().getColor(android.R.color.transparent));
           //profileLabel.setTextSize(16);

           friendsLabel.setBackgroundColor(getResources().getColor(android.R.color.transparent));
           //friendsLabel.setTextSize(16);

           hifiLabel.setBackgroundColor(getResources().getColor(R.color.blackDarkOverlay));
           //hifiLabel.setTextSize(22);

       }
    }

    public BroadcastReceiver NetworkChangeReceiver =new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            Log.e("Network reciever", "OnReceive");
            if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if(status!= NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    performUploadTask();
                }
            }
        }
    };

}
