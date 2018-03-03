package com.amsavarthan.hify.ui.activities;

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

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.UsersAdapter;
import com.amsavarthan.hify.models.Users;
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

public class FriendsView extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<Users> usersList;
    private UsersAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        usersList.clear();

        try{
            firestore.collection("Users").addSnapshotListener(this,new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    FirebaseUser currentUser=mAuth.getCurrentUser();

                    try{
                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()) {
                            String userId = doc.getDocument().getId();

                            if (!userId.equals(currentUser.getUid())) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    Users users = doc.getDocument().toObject(Users.class).withId(userId);
                                    usersList.add(users);
                                    usersAdapter.notifyDataSetChanged();

                                }
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

    public static void startActivity(Context context){
        Intent intent=new Intent(context,FriendsView.class);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_view);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = (RecyclerView)findViewById(R.id.usersList);

        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAlpha(0.0f);
        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersList, this);
        mRecyclerView.animate()
                .translationY(mRecyclerView.getHeight())
                .alpha(1.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mRecyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsView.this));
                        mRecyclerView.setHasFixedSize(true);
                        mRecyclerView.setAdapter(usersAdapter);
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
