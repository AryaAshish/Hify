package com.amsavarthan.hify.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.addFriends.AddFriendAdapter;
import com.amsavarthan.hify.adapters.addFriends.RecyclerViewTouchHelper;
import com.amsavarthan.hify.models.Friends;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddFriends extends AppCompatActivity {

    public static FloatingActionButton fab;
    private RecyclerView mRecyclerView;
    private List<Friends> usersList;
    private AddFriendAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String userId;
    private String name,image,email,token;
    private ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    private ListenerRegistration mRegistration;
    private Query mQuery;
    private String id;
    private boolean fab_visible = true;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, AddFriends.class);
        context.startActivity(intent);
    }

    public void stopListening() {

        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }

    }

    public void startListening() {

        try {
            if (mQuery != null && mRegistration == null) {
                mRegistration = mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Error", "listen:error", e);
                            return;
                        }

                        for (final DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                firestore.collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(doc.getDocument().getString("email")).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (!documentSnapshot.exists()) {
                                                    if (!doc.getDocument().getId().equals(FirebaseAuth.getInstance()
                                                            .getCurrentUser().getUid())) {
                                                        Friends friends = doc.getDocument().toObject(Friends.class).withId(doc.getDocument().getString("id"));
                                                        usersList.add(friends);
                                                        usersAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        });

                            }
                        }


                    }
                });
            }


        } catch (Exception e) {
            Log.e("Error: ", ".." + e.getLocalizedMessage());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
        overridePendingTransitionExit();
    }

    public void getUsers() {
        usersList.clear();
        mQuery = firestore.collection("Users");
        startListening();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.tf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = (RecyclerView)findViewById(R.id.usersList);
        fab = findViewById(R.id.searchFab);

        itemTouchHelperCallback = new RecyclerViewTouchHelper(0, ItemTouchHelper.LEFT, new RecyclerViewTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                if (viewHolder instanceof AddFriendAdapter.ViewHolder) {
                    // get the removed item name to display it in snack bar
                    String name = usersList.get(viewHolder.getAdapterPosition()).getName();

                    // backup of removed item for undo purpose
                    final Friends deletedItem = usersList.get(viewHolder.getAdapterPosition());
                    final int deletedIndex = viewHolder.getAdapterPosition();

                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.layout), "Friend request sent to " + name, Snackbar.LENGTH_LONG);

                    // remove the item from recycler view
                    usersAdapter.removeItem(viewHolder.getAdapterPosition(), snackbar, deletedIndex, deletedItem);

                }
            }
        });

        usersList = new ArrayList<>();
        usersAdapter = new AddFriendAdapter(usersList, this, findViewById(R.id.layout));

        mRecyclerView.setItemAnimator(new FlipInTopXAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(AddFriends.this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(AddFriends.this, DividerItemDecoration.VERTICAL));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(usersAdapter);

        getUsers();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopListening();
        finish();
        overridePendingTransitionExit();
    }

    public void gotoSearch(View view) {
        SearchUsersActivity.startActivity(this, this, fab);
    }
}
