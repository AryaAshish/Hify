package com.amsavarthan.hify.ui.activities;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.DrawerAdapter;
import com.amsavarthan.hify.models.DrawerItem;
import com.amsavarthan.hify.models.SimpleItem;
import com.amsavarthan.hify.ui.fragment.DashboardFragment;
import com.amsavarthan.hify.ui.notification.NotificationActivity;
import com.amsavarthan.hify.ui.notification.NotificationImage;
import com.amsavarthan.hify.ui.notification.NotificationImageReply;
import com.amsavarthan.hify.ui.notification.NotificationReplyActivity;
import com.amsavarthan.hify.utils.Config;
import com.amsavarthan.hify.utils.NetworkUtil;
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
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {

    private static final int POS_DASHBOARD = 0;
    private static final int POS_SETTINGS = 1;
    private static final int POS_ABOUT = 2;
    private static final int POS_LOGOUT = 4;
    public static String userId;
    DrawerAdapter adapter;
    View sheetView;
    private String[] screenTitles;
    private Drawable[] screenIcons;
    private SlidingRootNav slidingRootNav;
    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private FirebaseFirestore firestore;
    private UserHelper userHelper;
    private StorageReference storageReference;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Intent resultIntent;
    private CircleImageView imageView;
    private TextView username;
    private AuthCredential credential;
    public BroadcastReceiver NetworkChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            Log.i("Network reciever", "OnReceive");
            if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if (status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    performUploadTask();
                    try {
                        Snackbar.make(findViewById(R.id.activity_main), "Syncing...", Snackbar.LENGTH_LONG).show();
                    } catch (Exception e) {

                    }
                }
            }
        }

    };
    private BottomSheetDialog mBottomSheetDialog;

    public static void startActivity(Context context) {
        Intent intent=new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        username = findViewById(R.id.username);
        imageView = findViewById(R.id.profile_image);
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
            LoginActivity.startActivityy(this);
            finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        userHelper=new UserHelper(this);
        firestore = FirebaseFirestore.getInstance();

        registerReceiver(NetworkChangeReceiver
                ,new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        mAuth=FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();

        if(currentuser==null){

            LoginActivity.startActivityy(this);
            finish();

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
                            Toast.makeText(MainActivity.this, "We need storage permission for downloading and saving images, Grant it from settings", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        }
                    }).check();

            userId = currentuser.getUid();
            storageReference = FirebaseStorage.getInstance().getReference().child("images").child(currentuser.getUid() + ".jpg");;

            slidingRootNav = new SlidingRootNavBuilder(this)
                    .withToolbarMenuToggle(toolbar)
                    .withMenuOpened(false)
                    .withContentClickableWhenMenuOpened(false)
                    .withSavedState(savedInstanceState)
                    .withMenuLayout(R.layout.activity_main_drawer)
                    .inject();

            screenIcons = loadScreenIcons();
            screenTitles = loadScreenTitles();

            adapter = new DrawerAdapter(Arrays.asList(
                    createItemFor(POS_DASHBOARD).setChecked(true),
                    createItemFor(POS_SETTINGS),
                    createItemFor(POS_ABOUT),
                    new SpaceItem(48),
                    createItemFor(POS_LOGOUT)));
            adapter.setListener(this);

            RecyclerView list = findViewById(R.id.list);
            list.setNestedScrollingEnabled(false);
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(adapter);

            adapter.setSelected(POS_DASHBOARD);
            setUserProfile();

            mBottomSheetDialog = new BottomSheetDialog(this);
            sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
            mBottomSheetDialog.setContentView(sheetView);

            LinearLayout text_post = (LinearLayout) sheetView.findViewById(R.id.text_post);
            LinearLayout photo_post = (LinearLayout) sheetView.findViewById(R.id.image_post);

            text_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBottomSheetDialog.dismiss();
                    PostText.startActivity(MainActivity.this);

                }
            });

            photo_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBottomSheetDialog.dismiss();
                    PostImage.startActivity(MainActivity.this);
                }
            });

        }
    }

    private void setUserProfile() {

        Cursor rs = userHelper.getData(1);
        rs.moveToFirst();

        String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
        String imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));

        if (!rs.isClosed()) {
            rs.close();
        }

        username = findViewById(R.id.username);
        imageView = findViewById(R.id.profile_image);
        username.setText(nam);
        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(imag)
                .into(imageView);

    }

    @Override
    public void onItemSelected(int position) {

        Fragment selectedScreen;

        switch (position) {

            case POS_DASHBOARD:
                selectedScreen = new DashboardFragment();
                showFragment(selectedScreen);
                return;

            case POS_SETTINGS:
                FriendsView.startActivity(MainActivity.this);
                return;

            case POS_ABOUT:
                AddFriends.startActivity(MainActivity.this);
                return;

            case POS_LOGOUT:

                if (currentuser != null && isOnline()) {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("Are you sure do you want to logout from this account?")
                            .positiveText("Yes")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    logout();
                                    dialog.dismiss();
                                }
                            }).negativeText("No")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();

                } else {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("A technical occurred while logging you out, Check your network connection and try again.")
                            .positiveText("Done")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();

                }

                return;

            default:
                selectedScreen = new DashboardFragment();
                showFragment(selectedScreen);

        }
        slidingRootNav.closeMenu();

    }

    public void logout() {

        performUploadTask();
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage("Logging you out...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_id", "");

        firestore.collection("Users").document(userId).update(tokenRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                userHelper.deleteContact(1);
                mAuth.signOut();
                LoginActivity.startActivityy(MainActivity.this);
                mDialog.dismiss();
                finish();
                overridePendingTransitionExit();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Logout Error", e.getMessage());
            }
        });

    }

    private void showFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.textColorSecondary))
                .withTextTint(color(R.color.textColorPrimary))
                .withSelectedIconTint(color(R.color.colorAccentt))
                .withSelectedTextTint(color(R.color.colorAccentt));
    }

    @NonNull
    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
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
                .setDuration(5000)//5sec
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
                }).show();


    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    public void performUploadTask(){

        if(isOnline()){
            Cursor rc =userHelper.getData(1);
            rc.moveToFirst();

            final String nam = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
            final String emai = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
            final String imag = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
            final String password = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_PASS));

            if(!rc.isClosed()){
                rc.close();
            }

            FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    String name = documentSnapshot.getString("name");
                    String image = documentSnapshot.getString("image");
                    final String email = documentSnapshot.getString("email");

                    username.setText(name);
                    Glide.with(MainActivity.this)
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                            .load(image)
                            .into(imageView);


                    if (!image.equals(imag)) {
                        storageReference.putFile(Uri.parse(imag)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    final String downloadUri = task.getResult().getDownloadUrl().toString();

                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("image", downloadUri);

                                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            userHelper.updateContactImage(1, downloadUri);
                                            Glide.with(MainActivity.this)
                                                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                                    .load(downloadUri)
                                                    .into(imageView);

                                        }

                                    });
                                }
                            }
                        });
                    }
                    if (!name.equals(nam)) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", nam);

                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                userHelper.updateContactName(1, nam);
                                username.setText(nam);

                            }

                        });
                    }


                    if (!currentuser.getEmail().equals(emai)) {


                        credential = EmailAuthProvider
                                .getCredential(currentuser.getEmail(), password);

                        currentuser.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        currentuser.updateEmail(emai).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    if (!email.equals(emai)) {
                                                        Map<String, Object> userMap = new HashMap<>();
                                                        userMap.put("email", emai);

                                                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                userHelper.updateContactEmail(1, emai);
                                                            }

                                                        });
                                                    }

                                                } else {

                                                    Log.e("Update email error", task.getException().getMessage() + "..");

                                                }

                                            }
                                        });

                                    }
                                });
                    }
                }
            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(NetworkChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onViewProfileClicked(View view) {

        ProfileView.startActivity(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_posts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new:
                mBottomSheetDialog.show();
                return true;

            case R.id.action_refresh:
                showFragment(new DashboardFragment());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
