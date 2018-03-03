package com.amsavarthan.hify.ui.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.amsavarthan.hify.utils.CardAdapter;
import com.amsavarthan.hify.adapters.NotificationAdapter;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Notification;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class HifiFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private List<Notification> notificationList;
    private NotificationAdapter notificationAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private Button clearBtn;
    private CardView mCardView;

    public HifiFragment() {
        // Required empty public constructor
    }

    public CardView getCardView() {
        return mCardView;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        notificationList.clear();
        final FirebaseUser currentUser=mAuth.getCurrentUser();

        try{
            firestore.collection("Users").document(currentUser.getUid()).collection("Notifications")
                    .addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
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
                    .addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_hifi, container, false);

        if(view.getContext()!=null) {
            firestore = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            clearBtn = (Button) view.findViewById(R.id.clear);

            clearBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClearClick(view);
                }
            });

            mCardView = (CardView) view.findViewById(R.id.cardView);
            mCardView.setMaxCardElevation(mCardView.getCardElevation()
                    * CardAdapter.MAX_ELEVATION_FACTOR);

            notificationList = new ArrayList<>();
            notificationAdapter = new NotificationAdapter(notificationList, view.getContext());

            mCardView.setVisibility(View.VISIBLE);
            mCardView.setAlpha(0.0f);
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
                            mRecyclerView.setAdapter(notificationAdapter);
                        }
                    });


        }

        return view;
    }

    public void onClearClick(View view) {

        firestore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Notifications_reply").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for(DocumentSnapshot doc: documentSnapshots.getDocuments()) {
                    Toast.makeText(getContext(), doc.getId(), Toast.LENGTH_SHORT).show();
                    firestore.collection("Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .collection("Notification_reply")
                            .document(doc.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Reply data clean", Toast.LENGTH_SHORT).show();
                            notificationAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Error deleting some messages", Toast.LENGTH_SHORT).show();
                            notificationAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        firestore.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Notifications").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                for(DocumentSnapshot doc: documentSnapshots.getDocuments()) {
                    firestore.collection("Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .collection("Notification")
                            .document(doc.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Data clean", Toast.LENGTH_SHORT).show();
                            notificationAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Error deleting some messages", Toast.LENGTH_SHORT).show();
                            notificationAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        notificationAdapter.notifyDataSetChanged();

    }
}
