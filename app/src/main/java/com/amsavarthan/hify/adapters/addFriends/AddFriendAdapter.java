package com.amsavarthan.hify.adapters.addFriends;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Friends;
import com.amsavarthan.hify.ui.activities.AddUserDetail;
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
    private View view;
    private HashMap<String, Object> userMap;
    private boolean exist;

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
    public void onBindViewHolder(final ViewHolder holder, int position) {

        checkIfReqSent(holder, position);
        holder.listenerText.setText("Add as friend");

        FirebaseFirestore.getInstance().collection("Users").document(usersList.get(position).userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        holder.name.setText(documentSnapshot.getString("name"));

                        holder.email.setText(documentSnapshot.getString("email"));

                        Glide.with(context)
                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                .load(documentSnapshot.getString("image"))
                                .into(holder.image);
                    }
                });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddUserDetail.startActivity(context,
                        usersList.get(holder.getAdapterPosition()).userId,
                        usersList.get(holder.getAdapterPosition()).getName(),
                        usersList.get(holder.getAdapterPosition()).getEmail()
                        , usersList.get(holder.getAdapterPosition()).getImage()
                        , usersList.get(holder.getAdapterPosition()).getToken_id());
            }
        });

        holder.exist_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Information");
                dialog.setMessage("This icons shows to indicate that friend request to this user has been sent already.");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setIcon(R.drawable.ic_call_made_black_24dp).show();
            }
        });

        holder.friend_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Information");
                dialog.setMessage("This icon is shown to indicate that the user is already your friend.");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setIcon(R.drawable.ic_friend).show();
            }
        });

    }


    private void checkIfReqSent(final ViewHolder holder, final int pos) {

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(usersList.get(pos).userId)
                .collection("Friends")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    holder.exist_icon.setVisibility(View.GONE);
                    holder.friend_icon.setVisibility(View.VISIBLE);
                } else {
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(usersList.get(pos).userId)
                            .collection("Friend_Requests")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                holder.friend_icon.setVisibility(View.GONE);
                                holder.exist_icon.setVisibility(View.VISIBLE);
                            } else {
                                holder.exist_icon.setVisibility(View.GONE);
                                holder.friend_icon.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }


    private void removeUserandRestoreItem(final int position, final Friends deletedItem) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(usersList.get(position).userId)
                .collection("Friend_Requests")
                .document(deletedItem.userId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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
                .setPositiveBackgroundColorResource(R.color.colorAccentt)
                .setCancelable(false)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        addUser(position, deletedIndex, deletedItem);
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

    private void addUser(final int position, final int deletedIndex, final Friends deletedItem) {

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(usersList.get(position).userId)
                .collection("Friends")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (!documentSnapshot.exists()) {

                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(usersList.get(position).userId)
                                    .collection("Friend_Requests")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                                            if (!documentSnapshot.exists()) {
                                                executeFriendReq(position, deletedIndex, deletedItem);
                                            } else {
                                                Snackbar.make(view, "Friend request has been sent already", Snackbar.LENGTH_LONG).show();
                                                notifyDataSetChanged();
                                            }

                                        }
                                    });

                        } else {
                            usersList.remove(position);
                            notifyDataSetChanged();
                        }

                    }
                });


    }

    private void executeFriendReq(final int position, final int deletedIndex, final Friends deletedItem) {

        userMap = new HashMap<>();

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                userMap.put("name", documentSnapshot.getString("name"));
                userMap.put("id", documentSnapshot.getString("id"));
                userMap.put("email", documentSnapshot.getString("email"));
                userMap.put("image", documentSnapshot.getString("image"));
                userMap.put("token", documentSnapshot.getString("token_id"));
                userMap.put("notification_id", String.valueOf(System.currentTimeMillis()));
                userMap.put("timestamp", String.valueOf(System.currentTimeMillis()));

                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(deletedItem.userId)
                        .collection("Friend_Requests")
                        .document(documentSnapshot.getString("id"))
                        .set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Snackbar.make(view, "Friend request sent to " + deletedItem.getName(), Snackbar.LENGTH_LONG).show();
                        notifyDataSetChanged();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        removeUserandRestoreItem(deletedIndex, deletedItem);
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
        ImageView exist_icon, friend_icon;

        ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image=(CircleImageView)mView.findViewById(R.id.image);
            name=(TextView)mView.findViewById(R.id.name);
            email=(TextView)mView.findViewById(R.id.email);
            viewBackground = (RelativeLayout) mView.findViewById(R.id.view_background);
            viewForeground = (RelativeLayout) mView.findViewById(R.id.view_foreground);
            listenerText = (TextView) mView.findViewById(R.id.view_foreground_text);
            exist_icon = (ImageView) mView.findViewById(R.id.exist_icon);
            friend_icon = (ImageView) mView.findViewById(R.id.friend_icon);


        }
    }
}
