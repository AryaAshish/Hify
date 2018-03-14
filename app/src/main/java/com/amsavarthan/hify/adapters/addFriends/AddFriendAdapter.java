package com.amsavarthan.hify.adapters.addFriends;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Friends;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {

    private List<Friends> usersList;
    private Context context;
    private ViewHolder holderr;
    private String userId;
    private View view;
    private HashMap<String, Object> userMap;

    public AddFriendAdapter(List<Friends> usersList, Context context, View view) {
        this.usersList = usersList;
        this.context = context;
        this.view = view;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_list,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Log.i("Adapter", "Setting items...");


        holder.name.setText(usersList.get(position).getName());
        holderr = holder;

        holder.email.setText(usersList.get(position).getEmail());
        holder.listenerText.setText("Add as friend");
        userId = usersList.get(position).getId();

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(context.getResources().getDrawable(R.mipmap.profile_black)))
                .load(usersList.get(position).getImage())
                .into(holder.image);

        //checkUserAlreadyFriend(holder,userid,position);

    }


    public void checkUserAlreadyFriend(final ViewHolder holder, final String userId, final int pos) {
        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends").document(usersList.get(pos).getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //holder.add.setText("REMOVE");
                    //holder.add_.setText("Click to remove friend");
                } else {
                    //holder.add.setText("ADD");
                    //holder.add_.setText("Click to add friend");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void restoreItem(Friends deletedItem, int deletedIndex) {
        removeUserandRestoreItem(deletedIndex, deletedItem);
    }

    private void removeUserandRestoreItem(final int position, final Friends deletedItem) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .collection("Friend_Requests").document(deletedItem.getEmail()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                usersList.add(position, deletedItem);
                notifyItemInserted(position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeItem(final int position, final Snackbar snackbar, final int deletedIndex, final Friends deletedItem) {


        new BottomDialog.Builder(context)
                .setTitle("Add Friend")
                .setContent("Are you sure do you want to add " + usersList.get(position).getName() + " to your friend list?")
                .setPositiveText("Yes")
                .setNegativeText("No")
                .setPositiveBackgroundColorResource(R.color.colorAccent)
                .setCancelable(false)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        addUser(position, snackbar, deletedIndex, deletedItem);
                        dialog.dismiss();
                    }
                }).onNegative(new BottomDialog.ButtonCallback() {
            @Override
            public void onClick(@NonNull BottomDialog dialog) {
                dialog.dismiss();
                notifyDataSetChanged();
            }
        }).show();


    }

    private void addUser(final int position, final Snackbar snackbar, final int deletedIndex, final Friends deletedItem) {

        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .collection("Friend_Requests").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    executeFriendReq(position, snackbar, deletedIndex, deletedItem);
                } else {
                    Snackbar.make(view, "Friend request has been sent already", Snackbar.LENGTH_LONG).show();
                    notifyDataSetChanged();
                }
            }
        });

    }

    private void executeFriendReq(final int position, final Snackbar snackbar, final int deletedIndex, final Friends deletedItem) {

        userMap = new HashMap<>();

        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                userMap.put("name", documentSnapshot.getString("name"));
                userMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                userMap.put("accepted", false);
                userMap.put("image", documentSnapshot.getString("image"));
                userMap.put("token", documentSnapshot.getString("token_id"));
                userMap.put("notification_id", String.valueOf(System.currentTimeMillis()));
                userMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

                FirebaseFirestore.getInstance().collection("Users").document(deletedItem.getId())
                        .collection("Friend_Requests")
                        .document(documentSnapshot.getString("email"))
                        .set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Snackbar.make(view, "Friend request sent to " + deletedItem.getName(), Snackbar.LENGTH_LONG).show();
                        usersList.remove(position);
                        notifyItemRemoved(position);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        restoreItem(deletedItem, deletedIndex);
                    }
                });

            }
        });

    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView image;
        View mView;
        TextView name, email, listenerText;
        RelativeLayout viewBackground, viewForeground;

        ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image=(CircleImageView)mView.findViewById(R.id.image);
            name=(TextView)mView.findViewById(R.id.name);
            email=(TextView)mView.findViewById(R.id.email);
            viewBackground = (RelativeLayout) mView.findViewById(R.id.view_background);
            viewForeground = (RelativeLayout) mView.findViewById(R.id.view_foreground);
            listenerText = (TextView) mView.findViewById(R.id.view_foreground_text);

        }
    }
}
