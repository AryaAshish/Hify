package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.AddFriendAdapter;
import com.amsavarthan.hify.adapters.UsersAdapter;
import com.amsavarthan.hify.models.Friends;
import com.amsavarthan.hify.models.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddFriends extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<Friends> usersList;
    private AddFriendAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String userId;
    private String name,image,email,token;

    public void getUsers() {
        usersList.clear();

        try{
            firestore.collection("Users").addSnapshotListener(this,new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    final FirebaseUser currentUser=mAuth.getCurrentUser();

                    try{
                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                userId = doc.getDocument().getId();
                            if (!userId.equals(currentUser.getUid())) {
                                    firestore.collection("Users").document(userId).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(final DocumentSnapshot docu) {
                                            name=docu.get("name").toString();
                                            image=docu.get("image").toString();
                                            email=docu.get("email").toString();
                                            token=docu.get("token_id").toString();

                                            firestore.collection("Users").document(currentUser.getUid()).collection("Friends").document(email)
                                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                            if(!documentSnapshot.exists()){
                                                                Friends users = docu.toObject(Friends.class).withId(userId);
                                                                usersList.add(users);
                                                                usersAdapter.notifyDataSetChanged();
                                                            }else{
                                                                //Toast.makeText(AddFriends.this, documentSnapshot.getId()+" exists", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Error: ",""+e.getMessage());
                                        }
                                    });
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
        Intent intent=new Intent(context,AddFriends.class);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

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
        usersAdapter = new AddFriendAdapter(usersList, this);
        mRecyclerView.animate()
                .translationY(mRecyclerView.getHeight())
                .alpha(1.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mRecyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(AddFriends.this));
                        mRecyclerView.setHasFixedSize(true);
                        mRecyclerView.setAdapter(usersAdapter);
                    }
                });
        getUsers();

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
