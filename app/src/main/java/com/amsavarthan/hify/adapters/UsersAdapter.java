package com.amsavarthan.hify.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Users;
import com.amsavarthan.hify.ui.activities.SendActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<Users> usersList;
    private Context context;

    public UsersAdapter(List<Users> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_list,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.name.setText(usersList.get(position).getName());

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(usersList.get(position).getImage())
                .into(holder.image);

        final String userid=usersList.get(position).userId;

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendActivity.startActivityExtra(context,userid);
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
        private TextView name;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image = mView.findViewById(R.id.image);
            name = mView.findViewById(R.id.name);

        }
    }
}
