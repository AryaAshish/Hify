package com.amsavarthan.hify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

import com.amsavarthan.hify.adapters.NotificationAdapter;
import com.amsavarthan.hify.models.Notification;
import com.amsavarthan.hify.utils.CardAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MessagesView extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<Notification> notificationList;
    private NotificationAdapter notificationAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    public static void startActivity(Context context){
        Intent intent = new Intent(context,MessagesView.class);
        context.startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        notificationList.clear();
        final FirebaseUser currentUser=mAuth.getCurrentUser();

        try{
            firestore.collection("Users").document(currentUser.getUid()).collection("Notifications")
                    .addSnapshotListener(this,new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            try{
                                for(DocumentChange doc: documentSnapshots.getDocumentChanges()) {

                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        Notification notification = doc.getDocument().toObject(Notification.class);
                                        notificationList.add(notification);
                                        notificationAdapter.notifyDataSetChanged();

                                    }

                                }
                            }catch (Exception ex){
                                Log.e("Error: ",".."+ex.getLocalizedMessage());

                            }

                        }
                    });

            firestore.collection("Users").document(currentUser.getUid()).collection("Notifications_reply")
                    .addSnapshotListener(this,new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            try{
                                for(DocumentChange doc: documentSnapshots.getDocumentChanges()) {

                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        Notification notification = doc.getDocument().toObject(Notification.class);
                                        notificationList.add(notification);
                                        notificationAdapter.notifyDataSetChanged();

                                    }

                                }
                            }catch (Exception ex){
                                Log.e("Error: ",".."+ex.getLocalizedMessage());

                            }

                        }
                    });

        }catch (Exception e){
            Log.e("Error: ",".."+e.getLocalizedMessage());
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_view);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList, this);

        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAlpha(0.0f);
        mRecyclerView.animate()
                .translationY(mRecyclerView.getHeight())
                .alpha(1.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mRecyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(MessagesView.this));
                        mRecyclerView.setHasFixedSize(true);
                        mRecyclerView.setAdapter(notificationAdapter);
                    }
                });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mRecyclerView.animate()
                .translationY(0)
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                       finish();
                    }
                });
    }
}
