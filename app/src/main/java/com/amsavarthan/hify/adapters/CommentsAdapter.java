package com.amsavarthan.hify.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Comment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private List<Comment> commentList;
    private Context context;
    private FirebaseFirestore mFirestore;

    public CommentsAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        mFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.username.setText("Username");

        String timeAgo = TimeAgo.using(Long.parseLong(commentList.get(holder.getAdapterPosition()).getTimestamp()));
        holder.timestamp.setText(String.format("Commented %s", timeAgo));

        holder.comment.setText(commentList.get(holder.getAdapterPosition()).getComment());

        mFirestore.collection("Users")
                .document(commentList.get(position).getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        holder.username.setText(documentSnapshot.getString("name"));

                        Glide.with(context)
                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_6))
                                .load(documentSnapshot.getString("image"))
                                .into(holder.image);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                });


    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView image;
        private TextView username, comment, timestamp;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            image = mView.findViewById(R.id.comment_user_image);
            username = mView.findViewById(R.id.comment_username);
            comment = mView.findViewById(R.id.comment_text);
            timestamp = mView.findViewById(R.id.comment_timestamp);

        }
    }
}
