package com.amsavarthan.hify.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Extras;

import java.util.List;

/**
 * Created by amsavarthan on 15/3/18.
 */

public class ExtrasAdapter extends RecyclerView.Adapter<ExtrasAdapter.ViewHolder> {

    private List<Extras> extrasList;
    private Context mContext;

    public ExtrasAdapter(List<Extras> extrasList, Context mContext) {
        this.extrasList = extrasList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ExtrasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_extra, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExtrasAdapter.ViewHolder holder, int position) {

        final Extras item = extrasList.get(position);

        holder.imageView.setImageDrawable(mContext.getResources().getDrawable(item.getImage()));
        holder.title.setText(item.getTitle());
        holder.sub.setText(item.getSubtitle());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TO-DO
                mContext.startActivity(item.getIntent());
            }
        });


    }

    @Override
    public int getItemCount() {
        return extrasList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public ImageView imageView;
        public TextView title, sub;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            imageView = mView.findViewById(R.id.image);
            title = mView.findViewById(R.id.title);
            sub = mView.findViewById(R.id.sub);

        }
    }
}
