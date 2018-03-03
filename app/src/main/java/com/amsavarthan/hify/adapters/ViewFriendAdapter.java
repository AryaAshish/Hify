package com.amsavarthan.hify.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Friends;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class ViewFriendAdapter extends RecyclerView.Adapter<ViewFriendAdapter.ViewHolder> {

    private List<Friends> usersList;
    private Context context;

    public ViewFriendAdapter(List<Friends> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_list_added,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.name.setText(usersList.get(position).getName());

        RequestOptions placeholderOprions=new RequestOptions();
        placeholderOprions.placeholder(context.getResources().getDrawable(R.mipmap.profile_black));

        holder.email.setText(usersList.get(position).getEmail());

        Glide.with(context)
                .setDefaultRequestOptions(placeholderOprions)
                .load(usersList.get(position).getImage())
                .into(holder.image);

        final String userid=usersList.get(position).userId;

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.remove.performClick();
            }
        });

        holder.add_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.remove.performClick();
            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(context)
                        .title("Unfriend "+usersList.get(position).getName())
                        .content("Are you sure do you want to remove "+usersList.get(position).getName()+" from your friend list?")
                        .positiveText("Yes")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                removeUser(position);
                                dialog.dismiss();
                            }
                        }).negativeText("No")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

    }

    private void removeUser(final int position) {
        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends").document(usersList.get(position).getEmail()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, usersList.get(position).getName()+" has been removed from your friend list", Toast.LENGTH_SHORT).show();
                usersList.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private CircleImageView image;
        private TextView name,email,add_;
        private Button remove;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image=(CircleImageView)mView.findViewById(R.id.image);
            name=(TextView)mView.findViewById(R.id.name);
            email=(TextView)mView.findViewById(R.id.email);
            remove=(Button) mView.findViewById(R.id.add);
            add_=(TextView)mView.findViewById(R.id.add_);


        }
    }
}
