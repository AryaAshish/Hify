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

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {

    private List<Friends> usersList;
    private Context context;

    public AddFriendAdapter(List<Friends> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_list,parent,false);

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
                holder.add.performClick();
            }
        });

        holder.add_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.add.performClick();
            }
        });

        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(context)
                        .title("Add Friend")
                        .content("Are you sure do you want to add "+usersList.get(position).getName()+" to your friend list?")
                        .positiveText("Yes")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                addUser(position);
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

        //checkUserAlreadyFriend(holder,userid,position);

    }

    private void addUser(final int position) {
        Map<String,Object> userMap=new HashMap<>();
        userMap.put("name", usersList.get(position).getName());
        userMap.put("image",usersList.get(position).getImage());
        userMap.put("token",usersList.get(position).getToken());

        FirebaseFirestore.getInstance().collection("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Friends/").document(usersList.get(position).getEmail()).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context,usersList.get(position).getName()+" has been added to your friend list",Toast.LENGTH_SHORT).show();
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

    public void checkUserAlreadyFriend(final ViewHolder holder, final String userId, final int pos){
        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends").document(usersList.get(pos).getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    holder.add.setText("REMOVE");
                    holder.add_.setText("Click to remove friend");
                }else{
                    holder.add.setText("ADD");
                    holder.add_.setText("Click to add friend");
                }
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
        private Button add;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image=(CircleImageView)mView.findViewById(R.id.image);
            name=(TextView)mView.findViewById(R.id.name);
            email=(TextView)mView.findViewById(R.id.email);
            add=(Button) mView.findViewById(R.id.add);
            add_=(TextView)mView.findViewById(R.id.add_);


        }
    }
}
