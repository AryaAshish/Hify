package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.UsersAdapter;
import com.amsavarthan.hify.models.Users;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FriendsViewForMessage extends AppCompatActivity {

    public static List<Users> usersList;
    public static UsersAdapter usersAdapter;
    private RecyclerView mRecyclerView;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private Query mQuery;
    private ListenerRegistration mRegistration;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, FriendsViewForMessage.class);
        context.startActivity(intent);
    }

    public void getUsers() {
        if (mQuery != null && mRegistration == null) {
            try {
                mRegistration = mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        if (e != null) {
                            Log.w("Error", "listen:error", e);
                            return;
                        }

                        try {
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                final String userId = doc.getDocument().getId();
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Log.i("Users", userId);

                                    firestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                            if (e != null) {
                                                Log.w("Error", "listen:error", e);
                                                return;
                                            }

                                            for (final DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                                String Uid = doc.getDocument().getId();
                                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                                    Log.w("ADDED", doc.getDocument().getData().toString());

                                                    firestore.collection("Users").document(Uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            if (documentSnapshot.get("email").equals(userId)) {
                                                                Log.i("Uid", documentSnapshot.getId());
                                                                Users users = new Users(documentSnapshot.getString("name"), documentSnapshot.getString("image")).withId(documentSnapshot.getId());
                                                                usersList.add(users);
                                                                usersAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    });

                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        } catch (Exception ex) {
                            Log.e("Error: ", ".." + ex.getLocalizedMessage());

                        }
                        stopListening();
                    }
                });
            } catch (Exception e) {
                Log.e("Error: ", ".." + e.getLocalizedMessage());
            }
        }
    }

    public void startListening() {
        usersList.clear();
        mQuery = firestore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends");
        getUsers();
    }

    public void stopListening() {
        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_view_send);

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
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsViewForMessage.this));
                        mRecyclerView.setHasFixedSize(true);
                        mRecyclerView.addItemDecoration(new DividerItemDecoration(FriendsViewForMessage.this, DividerItemDecoration.VERTICAL));
                        mRecyclerView.setAdapter(usersAdapter);
                    }
                });

        startListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopListening();
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
