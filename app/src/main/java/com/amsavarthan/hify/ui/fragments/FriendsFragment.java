package com.amsavarthan.hify.ui.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amsavarthan.hify.utils.CardAdapter;
import com.amsavarthan.hify.adapters.UsersAdapter;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private View view;
    private RecyclerView mRecyclerView;
    private List<Users> usersList;
    private UsersAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private CardView mCardView;

    public FriendsFragment() {
        // Required empty public constructor
    }

    public CardView getCardView() {
        return mCardView;
    }

    @Override
    public void onStart() {
        super.onStart();
        usersList.clear();

        try{
            firestore.collection("Users").addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friends, container, false);

        if(view.getContext()!=null){
            firestore = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            mRecyclerView = (RecyclerView) view.findViewById(R.id.usersList);

            mCardView = (CardView) view.findViewById(R.id.cardView);
            mCardView.setMaxCardElevation(mCardView.getCardElevation()
                    * CardAdapter.MAX_ELEVATION_FACTOR);

            mCardView.setVisibility(View.VISIBLE);
            mCardView.setAlpha(0.0f);
            usersList = new ArrayList<>();
            usersAdapter = new UsersAdapter(usersList, view.getContext());
            mCardView.animate()
                    .translationY(mCardView.getHeight())
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                            mRecyclerView.setHasFixedSize(true);
                            mRecyclerView.setAdapter(usersAdapter);
                        }
                    });
        }

        return view;
    }

}
