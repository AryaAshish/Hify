package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FriendsViewForMessage extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    public static List<Users> usersList;
    public static UsersAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    public void getUsers() {
        usersList.clear();

        try{
            firestore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("Friends").addSnapshotListener(this,new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    FirebaseUser currentUser=mAuth.getCurrentUser();
                    try{
                        for(DocumentChange doc: documentSnapshots.getDocumentChanges()) {
                            final String userId = doc.getDocument().getId();
                             if (doc.getType() == DocumentChange.Type.ADDED) {
                                 Log.i("Users",userId);
                                    /*Users users = doc.getDocument().toObject(Users.class).withId(userId);
                                    usersList.add(users);
                                    usersAdapter.notifyItemInserted(usersList.size()-1);*/
                                 firestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                     @Override
                                     public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                         for(DocumentChange doc:documentSnapshots.getDocumentChanges()){
                                             String Uid=doc.getDocument().getId();
                                             if(doc.getType()== DocumentChange.Type.ADDED){

                                                 firestore.collection("Users").document(Uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                     @Override
                                                     public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                         if(documentSnapshot.get("email").equals(userId)){
                                                             Log.i("Uid",documentSnapshot.getId());
                                                             Users users = new Users(documentSnapshot.getString("name"),documentSnapshot.getString("image")).withId(documentSnapshot.getId());
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
        Intent intent=new Intent(context,FriendsViewForMessage.class);
        context.startActivity(intent);
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
