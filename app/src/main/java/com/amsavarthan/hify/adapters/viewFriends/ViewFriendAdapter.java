package com.amsavarthan.hify.adapters.viewFriends;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.ViewFriends;
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

public class ViewFriendAdapter extends RecyclerView.Adapter<ViewFriendAdapter.ViewHolder> {

    private List<ViewFriends> usersList;
    private Context context;
    private ViewHolder holderr;
    private String userId;
    private HashMap<String, Object> userMap;

    public ViewFriendAdapter(List<ViewFriends> usersList, Context context) {
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

        holderr = holder;
        holder.name.setText(usersList.get(position).getName());

        holder.email.setText(usersList.get(position).getEmail());
        userId = usersList.get(position).getId();

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(context.getResources().getDrawable(R.mipmap.profile_black)))
                .load(usersList.get(position).getImage())
                .into(holder.image);

    }


    private void restoreItem(ViewFriends deletedItem, int deletedIndex) {
        addUserandRestoreItem(deletedIndex, deletedItem);
    }


    private void addUserandRestoreItem(final int deletedIndex, final ViewFriends deletedItem) {

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

                FirebaseFirestore.getInstance().collection("Users/" + userId + "/Friend_Requests/")
                        .document(documentSnapshot.getString("email"))
                        .set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(context, "Friend request sent to " + usersList.get(deletedIndex).getName(), Toast.LENGTH_SHORT).show();

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


    public void removeItem(final int position, final Snackbar snackbar, final int deletedIndex, final ViewFriends deletedItem) {

        new BottomDialog.Builder(context)
                .setTitle("Unfriend " + usersList.get(position).getName())
                .setContent("Are you sure do you want to remove " + usersList.get(position).getName() + " from your friend list?")
                .setPositiveText("Yes")
                .setPositiveBackgroundColorResource(R.color.colorAccent)
                .setNegativeText("No")
                .setCancelable(false)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        removeUser(position, snackbar, deletedIndex, deletedItem);
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

    private void removeUser(final int position, final Snackbar snackbar, final int deletedIndex, final ViewFriends deletedItem) {

        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends").document(usersList.get(position).getEmail()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(userId)
                        .collection("Friends")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        /*snackbar.setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // undo is selected, restore the deleted item
                                restoreItem(deletedItem, deletedIndex);
                            }
                        });
                        snackbar.setActionTextColor(context.getResources().getColor(R.color.colorAccent));
                        snackbar.show();*/
                        usersList.remove(position);
                        notifyItemRemoved(position);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                restoreItem(deletedItem, deletedIndex);
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, email, listenerText;
        RelativeLayout viewBackground, viewForeground;
        private View mView;
        private CircleImageView image;

        public ViewHolder(View itemView) {
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
