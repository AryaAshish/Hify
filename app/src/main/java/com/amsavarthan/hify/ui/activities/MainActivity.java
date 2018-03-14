package com.amsavarthan.hify.ui.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.CardPagerAdapter;
import com.amsavarthan.hify.adapters.PagerViewAdapter;
import com.amsavarthan.hify.anim.Transformer;
import com.amsavarthan.hify.models.CardItem;
import com.amsavarthan.hify.ui.activities.Notification.NotificationActivity;
import com.amsavarthan.hify.ui.activities.Notification.NotificationImage;
import com.amsavarthan.hify.ui.activities.Notification.NotificationImageReply;
import com.amsavarthan.hify.ui.activities.Notification.NotificationReplyActivity;
import com.amsavarthan.hify.utils.Config;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.tapadoo.alerter.Alerter;
import com.tapadoo.alerter.OnHideAlertListener;
import com.tapadoo.alerter.OnShowAlertListener;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    public static String userId;
    public static Activity activity;
    private ImageView profileLabel,friendsLabel,hifiLabel;
    private ViewPager mainpager;
    private PagerViewAdapter mPagerViewAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private UserHelper userHelper;

    private CardPagerAdapter mCardAdapter;
    private Transformer mCardShadowTransformer;
    private String nam,imag;
    private StorageReference storageReference;
    public BroadcastReceiver NetworkChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            Log.i("Network reciever", "OnReceive");
            if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if (status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    performUploadTask();
                }
            }
        }
    };
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Intent resultIntent;

    public static void startActivityy(Context context) {
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

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.REGISTRATION_COMPLETE));

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.PUSH_NOTIFICATION));


        }else{
            LoginActivity.startActivityy(this, this);
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
            LoginActivity.startActivityy(this, this);
        }else {

            firebaseMessagingService();

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
            mCardAdapter.addCardItem(new CardItem(R.string.messages, R.string.text_1, R.mipmap.message, "#db3236", "Send a message", "View Messages"));
            mCardAdapter.addCardItem(new CardItem(R.string.friends, R.string.text_1, R.mipmap.friends, "#4885ed", "My Friends", "Add a Friend"));
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


    }

    private void firebaseMessagingService() {

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("OnBroadcastReceiver", "received");

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);


                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    Log.i("OnBroadcastReceiver", "push_received");

                    // new push notification is received
                    String click_action = intent.getStringExtra("click_action");
                    Log.i("OnBroadcastReceiver", click_action);

                    if (click_action.equals("com.amsavarthan.hify.TARGETNOTIFICATION")) {

                        resultIntent = new Intent(MainActivity.this, NotificationActivity.class);

                        showAlert(resultIntent, intent);

                    } else if (click_action.equals("com.amsavarthan.hify.TARGETNOTIFICATIONREPLY")) {

                        resultIntent = new Intent(MainActivity.this, NotificationReplyActivity.class);

                        showAlert(resultIntent, intent);

                    } else if (click_action.equals("com.amsavarthan.hify.TARGETNOTIFICATION_IMAGE")) {

                        resultIntent = new Intent(MainActivity.this, NotificationImage.class);

                        showAlert(resultIntent, intent);


                    } else if (click_action.equals("com.amsavarthan.hify.TARGETNOTIFICATIONREPLY_IMAGE")) {

                        resultIntent = new Intent(MainActivity.this, NotificationImageReply.class);

                        showAlert(resultIntent, intent);

                    } else if (click_action.equals("com.amsavarthan.hify.TARGET_FRIENDREQUEST")) {

                        resultIntent = new Intent(MainActivity.this, FriendRequestActivty.class);

                        showAlert(resultIntent, intent);


                    } else {
                        resultIntent = null;
                    }

                }
            }
        };
    }

    private void showAlert(final Intent resultIntent, Intent intent) {

        String name = intent.getStringExtra("name");
        String from_image = intent.getStringExtra("from_image");
        String message = intent.getStringExtra("message");
        String from_id = intent.getStringExtra("from_id");
        int notification_id = intent.getIntExtra("notification_id", (int) System.currentTimeMillis());
        String reply_for = intent.getStringExtra("reply_for");
        final String imageUrl = intent.getStringExtra("image");
        final String body = intent.getStringExtra("body");
        final String title = intent.getStringExtra("title");

        String f_id = intent.getStringExtra("f_id");
        String f_name = intent.getStringExtra("f_name");
        String f_email = intent.getStringExtra("f_email");
        String f_token = intent.getStringExtra("f_token");
        String f_image = intent.getStringExtra("f_image");


        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("from_image", from_image);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("from_id", from_id);
        resultIntent.putExtra("notification_id", notification_id);
        resultIntent.putExtra("reply_for", reply_for);
        resultIntent.putExtra("image", imageUrl);
        resultIntent.putExtra("reply_image", from_image);

        resultIntent.putExtra("f_id", f_id);
        resultIntent.putExtra("f_name", f_name);
        resultIntent.putExtra("f_email", f_email);
        resultIntent.putExtra("f_image", f_image);
        resultIntent.putExtra("f_token", f_token);


        Alerter.create(MainActivity.this)
                .setTitle(title)
                .setText(body)
                .enableSwipeToDismiss()
                .setDuration(5000)//6sec
                .enableProgress(true)
                .enableVibration(true)
                .setBackgroundColorRes(R.color.colorAccent)
                .setProgressColorRes(R.color.colorPrimary)
                .setTitleAppearance(R.style.AlertTextAppearance_Title)
                .setTextAppearance(R.style.AlertTextAppearance_Text)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(resultIntent);
                    }
                }).setOnShowListener(new OnShowAlertListener() {
            @Override
            public void onShow() {
                clearLightStatusBar(activity, getWindow().getDecorView());
            }
        }).setOnHideListener(new OnHideAlertListener() {
            @Override
            public void onHide() {
                setLightStatusBar(getWindow().getDecorView(), activity);
            }
        }).show();


    }

    @Override
    protected void onPause() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void performLogintask() {


        Cursor rs = userHelper.getData(1);
        rs.moveToFirst();

        String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));

        if (!rs.isClosed())
            rs.close();

        if (nam == null) {
            FirebaseFirestore.getInstance().collection("Users").document(currentuser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    userHelper.insertContact(
                            documentSnapshot.getString("name")
                            , documentSnapshot.getString("email")
                            , documentSnapshot.getString("image")
                    );
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Error", ".." + e.getMessage());
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
        //unregisterReceiver(NetworkChangeReceiver);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
