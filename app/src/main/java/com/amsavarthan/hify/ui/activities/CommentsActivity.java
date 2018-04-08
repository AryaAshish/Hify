package com.amsavarthan.hify.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.adapters.CommentsAdapter;
import com.amsavarthan.hify.models.Comment;
import com.amsavarthan.hify.models.Post;
import com.amsavarthan.hify.ui.extras.Planner.utils.AnimationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class CommentsActivity extends AppCompatActivity {

    String user_id, name, color, desc, favs, likes, timestamp, user_image, image, post_id;
    private FirebaseFirestore mFirestore;
    private CommentsAdapter mAdapter;
    private List<Comment> commentList;
    private ProgressBar mProgress;
    private RecyclerView mCommentsRecycler;
    private EditText mCommentText;
    private Button mCommentsSend;
    private FirebaseUser mCurrentUser;

    public static void startActivity(Context context, List<Post> post, int pos, String name, String image) {
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra("user_id", post.get(pos).getUserId());
        intent.putExtra("color", post.get(pos).getColor());
        intent.putExtra("desc", post.get(pos).getDescription());
        intent.putExtra("favs", post.get(pos).getFavourites());
        intent.putExtra("likes", post.get(pos).getLikes());
        intent.putExtra("timestamp", post.get(pos).getTimestamp());
        intent.putExtra("image", post.get(pos).getImage());
        intent.putExtra("post_id", post.get(pos).postId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comments);

        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        setupCommentView();
        mAdapter.notifyDataSetChanged();
    }

    private void setupCommentView() {

        user_id = getIntent().getStringExtra("user_id");
        color = getIntent().getStringExtra("color");
        desc = getIntent().getStringExtra("desc");
        favs = getIntent().getStringExtra("favs");
        likes = getIntent().getStringExtra("likes");
        timestamp = getIntent().getStringExtra("timestamp");
        image = getIntent().getStringExtra("image");
        post_id = getIntent().getStringExtra("post_id");

        mCommentsRecycler = findViewById(R.id.recyclerView);
        mCommentText = findViewById(R.id.text);
        mCommentsSend = findViewById(R.id.send);
        mProgress = findViewById(R.id.progressBar);

        commentList = new ArrayList<>();
        mAdapter = new CommentsAdapter(commentList, this);

        mCommentText.setHint("Add a comment..");
        mCommentsSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = mCommentText.getText().toString();
                if (!TextUtils.isEmpty(comment))
                    sendComment(comment, mCommentText, mProgress);
                else
                    AnimationUtil.shakeView(mCommentText, CommentsActivity.this);
            }
        });

        mCommentsRecycler.setItemAnimator(new SlideInLeftAnimator());
        mCommentsRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecycler.setHasFixedSize(true);
        mCommentsRecycler.setAdapter(mAdapter);

        getComments(mProgress);

    }

    private void sendComment(final String comment, final EditText comment_text, final ProgressBar mProgress) {

        mProgress.setVisibility(View.VISIBLE);

        mFirestore.collection("Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Map<String, Object> commentMap = new HashMap<>();
                        commentMap.put("id", documentSnapshot.getString("id"));
                        commentMap.put("post_id", post_id);
                        commentMap.put("comment", comment);
                        commentMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

                        mFirestore.collection("Posts")
                                .document(user_id)
                                .collection("All Posts")
                                .document(post_id)
                                .collection("Comments")
                                .add(commentMap)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        mCommentText.setHint("Add a comment..");
                                        mCommentText.setHint(String.format("Comment as %s...", name));
                                        //Toast.makeText(CommentsActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                                        commentList.clear();
                                        getComments(mProgress);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CommentsActivity.this, "Error sending comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Error send comment", e.getMessage());
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error getuser", e.getMessage());
                    }
                });

    }

    private void getComments(final ProgressBar mProgress) {
        mProgress.setVisibility(View.VISIBLE);
        mFirestore.collection("Posts")
                .document(user_id)
                .collection("All Posts")
                .document(post_id)
                .collection("Comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {

                        for (DocumentChange doc : querySnapshot.getDocumentChanges()) {

                            if (doc.getDocument().exists()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    Comment comment = doc.getDocument().toObject(Comment.class).withId(doc.getDocument().getId());
                                    commentList.add(comment);
                                    mAdapter.notifyDataSetChanged();

                                }

                                if (querySnapshot.getDocuments().size() == commentList.size()) {
                                    mProgress.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                mProgress.setVisibility(View.INVISIBLE);
                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error get comment", e.getMessage());
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete())
                    mProgress.setVisibility(View.INVISIBLE);
            }
        });
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

    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

}
