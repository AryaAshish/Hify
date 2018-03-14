package com.amsavarthan.hify.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.FriendRequestAdapter;
import com.amsavarthan.hify.adapters.viewFriends.RecyclerViewTouchHelper;
import com.amsavarthan.hify.adapters.viewFriends.ViewFriendAdapter;
import com.amsavarthan.hify.models.FriendRequest;
import com.amsavarthan.hify.models.ViewFriends;
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

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FriendsView extends AppCompatActivity {

    public static FriendsView activity;
    private RecyclerView mRecyclerView, mRequestView;
    private List<ViewFriends> usersList;
    private List<FriendRequest> requestList;
    private ViewFriendAdapter usersAdapter;
    private FriendRequestAdapter requestAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    private ListenerRegistration mRegistration, mRegistrationRequest;
    private Query mQuery, mRequestQuery;
    private RelativeLayout mLayout;
    private ProgressDialog mDialog;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, FriendsView.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usersList.clear();
        requestList.clear();
        stopListening();
    }

    public void stopListening() {
        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }
        if (mRegistrationRequest != null) {
            mRegistrationRequest.remove();
            mRegistrationRequest = null;
        }

    }

    public void startListening() {
        try {
            if (mQuery != null && mRegistration == null) {
                mRegistration = mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (e != null) {
                            Log.w("Error", "listen:error", e);
                            return;
                        }
                        try {
                            for (final DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                final String userId = doc.getDocument().getId();
                                final boolean accepted = doc.getDocument().getBoolean("accepted");
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Log.i("Users", userId);
                                    firestore.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w("Error", "listen:error", e);
                                                return;
                                            }

                                            if (!documentSnapshots.getDocumentChanges().isEmpty()) {
                                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                                    final String Uid = doc.getDocument().getId();
                                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                                        firestore.collection("Users").document(Uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                if (documentSnapshot.get("email").equals(userId)) {

                                                                    ViewFriends users = new ViewFriends(
                                                                            Uid,
                                                                            documentSnapshot.getString("name"),
                                                                            documentSnapshot.getString("image"),
                                                                            documentSnapshot.getString("email"),
                                                                            documentSnapshot.getString("token_id"),
                                                                            accepted);
                                                                    usersList.add(users);
                                                                    usersAdapter.notifyDataSetChanged();

                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                            mDialog.dismiss();
                        } catch (Exception ex) {
                            Log.e("Error: ", ".." + ex.getLocalizedMessage());

                        }

                    }
                });
            }
        } catch (Exception e) {
            Log.e("Error: ", ".." + e.getLocalizedMessage());
        }
    }

    public void startListeningRequest() {
        try {

            if (mRequestQuery != null && mRegistrationRequest == null) {

                mRegistrationRequest = mRequestQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                if (!doc.getDocument().getBoolean("accepted")) {
                                    FriendRequest request = new FriendRequest(
                                            doc.getDocument().getString("id"),
                                            doc.getDocument().getString("name"),
                                            doc.getDocument().getString("image"),
                                            doc.getDocument().getId(),
                                            doc.getDocument().getString("token")
                                    );
                                    requestList.add(request);
                                    requestAdapter.notifyDataSetChanged();
                                }

                            }
                        }
                        mDialog.dismiss();

                    }
                });


            }

        } catch (Exception ex) {

        }
    }

    public void getUsers() {
        usersList.clear();
        requestList.clear();
        mDialog.show();

        mQuery = firestore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends");
        mRequestQuery = firestore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friend_Requests");

        startListening();
        startListeningRequest();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUsers();
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

        activity = this;

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = (RecyclerView)findViewById(R.id.usersList);
        mRequestView = (RecyclerView) findViewById(R.id.requestList);
        mLayout = (RelativeLayout) findViewById(R.id.layout);

        itemTouchHelperCallback = new RecyclerViewTouchHelper(0, ItemTouchHelper.LEFT, new RecyclerViewTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                if (viewHolder instanceof ViewFriendAdapter.ViewHolder) {
                    // get the removed item name to display it in snack bar
                    String name = usersList.get(viewHolder.getAdapterPosition()).getName();

                    // backup of removed item for undo purpose
                    final ViewFriends deletedItem = usersList.get(viewHolder.getAdapterPosition());
                    final int deletedIndex = viewHolder.getAdapterPosition();

                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.layout), name + " has been removed from your friends list", Snackbar.LENGTH_LONG);

                    // remove the item from recycler view
                    usersAdapter.removeItem(viewHolder.getAdapterPosition(), snackbar, deletedIndex, deletedItem);

                }
            }
        });


        mLayout.setVisibility(View.VISIBLE);
        mLayout.setAlpha(0.0f);

        usersList = new ArrayList<>();
        requestList = new ArrayList<>();
        usersAdapter = new ViewFriendAdapter(usersList, this);
        requestAdapter = new FriendRequestAdapter(requestList, this);

        mLayout.animate()
                .translationY(mLayout.getHeight())
                .alpha(1.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        //Friends Recyclerview
                        mRecyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(1f)));
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsView.this));
                        mRecyclerView.setHasFixedSize(true);
                        mRecyclerView.addItemDecoration(new DividerItemDecoration(FriendsView.this, DividerItemDecoration.VERTICAL));
                        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
                        mRecyclerView.setAdapter(usersAdapter);

                        //FriendRequest Recyclerview
                        mRequestView.setItemAnimator(new SlideInLeftAnimator());
                        mRequestView.setLayoutManager(new LinearLayoutManager(FriendsView.this, LinearLayoutManager.HORIZONTAL, false));
                        mRequestView.setHasFixedSize(true);
                        mRequestView.setAdapter(requestAdapter);
                    }
                });
        getUsers();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopListening();
        mLayout.animate()
                .translationY(0)
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        finish();
                        usersList.clear();
                        requestList.clear();
                        mLayout.setVisibility(View.INVISIBLE);
                    }
                });

    }
}

