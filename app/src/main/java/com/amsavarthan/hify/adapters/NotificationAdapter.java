package com.amsavarthan.hify.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Notification;
import com.amsavarthan.hify.ui.notification.NotificationActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private Context context;

    public NotificationAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item_list,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationAdapter.ViewHolder holder, int position) {

        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

        holder.message.setText(notificationList.get(position).getMessage());

        mFirestore.collection("Users").document(notificationList.get(position).getFrom()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                final String name,image;
                name=documentSnapshot.getString("name");
                image=documentSnapshot.getString("image");

                Glide.with(context)
                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                        .load(image)
                        .into(holder.image);

                holder.from.setText(name);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(context, NotificationActivity.class);
                        intent.putExtra("from_id", notificationList.get(holder.getAdapterPosition()).getFrom());
                        intent.putExtra("message", notificationList.get(holder.getAdapterPosition()).getMessage());
                        intent.putExtra("image",image);
                        intent.putExtra("name",name);
                        context.startActivity(intent);
                    }
                });

            }
        });



    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView image;
        private TextView from,message;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image = mView.findViewById(R.id.image);
            from = mView.findViewById(R.id.name);
            message = mView.findViewById(R.id.message);

        }
    }
}
