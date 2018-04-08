package com.amsavarthan.hify.ui.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.ui.activities.ImagePreview;
import com.amsavarthan.hify.ui.activities.SendActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NotificationImageReply extends AppCompatActivity {

    private TextView nameTxt, messageTxt, replyTxt;
    private String msg;
    private CircleImageView imageView;

    private TextView username;
    private String user_id, current_id;
    private Button mSend;
    private EditText message;
    private FirebaseFirestore mFirestore;
    private ProgressBar mBar;

    private ImageView messageImage;
    private String imageUri;
    private String reply;
    private String name;


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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_image_reply);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        nameTxt = (TextView) findViewById(R.id.name);
        messageTxt = (TextView) findViewById(R.id.messagetxt);
        imageView = (CircleImageView) findViewById(R.id.circleImageView);
        replyTxt = (TextView) findViewById(R.id.replytxt);

        current_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        username = (TextView) findViewById(R.id.user_name);
        mSend = (Button) findViewById(R.id.send);
        message = (EditText) findViewById(R.id.message);
        mBar = (ProgressBar) findViewById(R.id.progressBar);
        messageImage = (ImageView) findViewById(R.id.messageImage);

        msg = getIntent().getStringExtra("message");
        reply = getIntent().getStringExtra("reply_for");
        user_id = getIntent().getStringExtra("from_id");
        imageUri = getIntent().getStringExtra("reply_image");

        mFirestore = FirebaseFirestore.getInstance();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(getResources().getDrawable(R.mipmap.image));

        Glide.with(NotificationImageReply.this)
                .setDefaultRequestOptions(requestOptions)
                .load(imageUri)
                .into(messageImage);

        mFirestore.collection("Users").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                name = documentSnapshot.getString("name");
                nameTxt.setText(name);

                String image_ = documentSnapshot.getString("image");

                Glide.with(NotificationImageReply.this)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                        .load(image_)
                        .into(imageView);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                imageView.setVisibility(View.GONE);
                nameTxt.setVisibility(View.GONE);
            }
        });

        messageTxt.setText(reply);
        replyTxt.setText(msg);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null && getIntent().getStringExtra("notification_id") != null) {
            notificationManager.cancel(Integer.parseInt(getIntent().getStringExtra("notification_id")));
        }

        initReply();

    }

    private void initReply() {


        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message_ = message.getText().toString();

                if (!TextUtils.isEmpty(message_)) {
                    mBar.setVisibility(View.VISIBLE);
                    Map<String, Object> notificationMessage = new HashMap<>();
                    notificationMessage.put("reply_for", msg);
                    notificationMessage.put("message", message_);
                    notificationMessage.put("from", current_id);
                    notificationMessage.put("notification_id", String.valueOf(System.currentTimeMillis()));
                    notificationMessage.put("timestamp", String.valueOf(System.currentTimeMillis()));

                    mFirestore.collection("Users/" + user_id + "/Notifications_reply").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {

                            Toast.makeText(NotificationImageReply.this, "Hify sent!", Toast.LENGTH_SHORT).show();
                            message.setText("");
                            mBar.setVisibility(View.INVISIBLE);
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NotificationImageReply.this, "Error sending Hify: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            mBar.setVisibility(View.INVISIBLE);
                        }
                    });

                }

            }
        });

    }


    public void SendNew(View view) {

        SendActivity.startActivityExtra(NotificationImageReply.this, user_id);

    }

    public void onPreviewImage(View view) {

        Intent intent = new Intent(this, ImagePreview.class)
                .putExtra("url", imageUri)
                .putExtra("uri", "");
        startActivity(intent);
        overridePendingTransitionExit();

    }
}

