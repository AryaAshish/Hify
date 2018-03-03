package com.amsavarthan.hify.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.hify.MessagesView;
import com.amsavarthan.hify.ui.activities.FriendsView;
import com.amsavarthan.hify.utils.CardAdapter;
import com.amsavarthan.hify.ui.activities.ProfileView;
import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.CardItem;
import com.amsavarthan.hify.ui.activities.FragmentHolder;
import com.amsavarthan.hify.ui.activities.ProfileEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amsavarthan on 1/3/18.
 */

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;
    private Activity activity;

    public CardPagerAdapter(Activity activity) {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
        this.activity=activity;
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.adapter, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(final CardItem item, View view) {
        TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        TextView contentTextView = (TextView) view.findViewById(R.id.contentTextView);
        final CardView cardView=(CardView)view.findViewById(R.id.cardView);
        final Button button1=(Button)view.findViewById(R.id.button1);
        Button button2=(Button)view.findViewById(R.id.button2);
        button1.setText(item.getmButtonText1());
        button2.setText(item.getmButtonText2());
        final ImageView imageView=(ImageView)view.findViewById(R.id.imageView);
        imageView.setImageDrawable(view.getResources().getDrawable(item.getmImageResource()));
        cardView.setCardBackgroundColor(Color.parseColor(item.getmColorResource()));
        titleTextView.setText(item.getmTextResource());
        contentTextView.setText(item.getmTitleResource());

        if(item.getmButtonText1().equals("View Profile")){
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProfileView.startActivity(view.getContext());
                   // FragmentHolder.startActivity(view.getContext(),"v_profile",cardView,activity,item.getmImageResource(),item.getmColorResource());
                }
            });
        }else if(item.getmButtonText1().equals("My Friends")){
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FriendsView.startActivity(view.getContext());
                    // FragmentHolder.startActivity(view.getContext(),"v_friends",cardView,activity,item.getmImageResource(),item.getmColorResource());
                }
            });
        }else if(item.getmButtonText1().equals("View Messages")){
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MessagesView.startActivity(view.getContext());
                    //FragmentHolder.startActivity(view.getContext(),"v_messages",cardView,activity,item.getmImageResource(),item.getmColorResource());
                }
            });
        }


        if(item.getmButtonText2().equals("Edit Profile")){
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProfileEdit.startActivity(view.getContext());
                }
            });
        }else if(item.getmButtonText2().equals("Add a Friend")){
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FriendsView.startActivity(view.getContext());
                    //AddFriends.startActivity(view.getContext());
                }
            });
        }else if(item.getmButtonText2().equals("Clear Message History")){
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    new MaterialDialog.Builder(view.getContext())
                            .title("Clear Messages")
                            .content("Are you sure do you want to clear your message history?")
                            .positiveText("Yeah!")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Toast.makeText(view.getContext(), "History cleared", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }).negativeText("Nope")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            });
        }

    }

}
