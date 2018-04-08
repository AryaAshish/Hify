package com.amsavarthan.hify.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.PostsAdapter;
import com.amsavarthan.hify.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
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

/**
 * Created by amsavarthan on 29/3/18.
 */

public class DashboardFragment extends Fragment {

    List<Post> mPostsList;
    ListenerRegistration mRegistration;
    Query mQuery;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    RecyclerView mPostsRecyclerView;
    PostsAdapter mAdapter;
    View mView;
    boolean isFirstPageFirstLoad = true;
    private DocumentSnapshot lastVisible;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.dashboard_fragment, container, false);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mPostsList = new ArrayList<>();
        mAdapter = new PostsAdapter(mPostsList, view.getContext());
        mPostsRecyclerView = view.findViewById(R.id.posts_recyclerview);

        mPostsRecyclerView.setItemAnimator(new SlideInLeftAnimator());
        mPostsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostsRecyclerView.setHasFixedSize(true);
        mPostsRecyclerView.setAdapter(mAdapter);

        mPostsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) {
                    //loadMorePost();
                }
            }
        });

        getPosts();

    }


    public void stopListening() {

        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }

    }

    public void startListening() {

        if (mQuery != null && mRegistration == null) {
            mRegistration = mQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot querySnapshot, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w("Error", "listen:error", e);
                        return;
                    }

                    for (DocumentChange doc : querySnapshot.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                            mPostsList.add(post);
                            mAdapter.notifyDataSetChanged();

                        }

                    }

                }
            });
        }


    }

    public void getPosts() {
        mPostsList.clear();

        mFirestore.collection("Users")
                .document(currentUser.getUid())
                .collection("Friends")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {

                        if (!querySnapshot.getDocuments().isEmpty()) {
                            for (DocumentChange doc : querySnapshot.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    mQuery = mFirestore.collection("Posts")
                                            .document(doc.getDocument().getId())
                                            .collection("All Posts");
                                    startListening();
                                }
                            }
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "listen:error", e);
                    }
                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopListening();
    }
}
