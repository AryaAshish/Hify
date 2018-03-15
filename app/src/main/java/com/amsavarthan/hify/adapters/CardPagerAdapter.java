package com.amsavarthan.hify.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.transition.TransitionManager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.CardItem;
import com.amsavarthan.hify.ui.activities.AddFriends;
import com.amsavarthan.hify.ui.activities.Extras.ExtrasActivity;
import com.amsavarthan.hify.ui.activities.FriendsView;
import com.amsavarthan.hify.ui.activities.FriendsViewForMessage;
import com.amsavarthan.hify.ui.activities.ProfileEdit;
import com.amsavarthan.hify.ui.activities.ProfileView;
import com.amsavarthan.hify.utils.CardAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amsavarthan on 1/3/18.
 */

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    boolean visible;
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
        final CardView cardViewButtons = (CardView) view.findViewById(R.id.card_options);
        final Button button1=(Button)view.findViewById(R.id.button1);
        Button button2=(Button)view.findViewById(R.id.button2);
        button1.setText(item.getmButtonText1());
        button2.setText(item.getmButtonText2());
        final ImageView imageView=(ImageView)view.findViewById(R.id.imageView);
        imageView.setImageDrawable(view.getResources().getDrawable(item.getmImageResource()));
        cardView.setCardBackgroundColor(Color.parseColor(item.getmColorResource()));
        titleTextView.setText(item.getmTextResource());
        contentTextView.setText(item.getmTitleResource());

        final ViewGroup transitionsContainer = (ViewGroup) view.findViewById(R.id.layout);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TransitionManager.beginDelayedTransition(transitionsContainer);
                visible = !visible;
                cardViewButtons.setVisibility(visible ? View.VISIBLE : View.GONE);

            }
        });

        if(item.getmButtonText1().equals("View Profile")){
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProfileView.startActivity(view.getContext());
                }
            });
        }else if(item.getmButtonText1().equals("My Friends")){
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FriendsView.startActivity(view.getContext());
                }
            });
        }else if(item.getmButtonText1().equals("Send a message")){
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FriendsViewForMessage.startActivity(view.getContext());
                }
            });
        } else if (item.getmButtonText1().equals("Explore")) {
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ExtrasActivity.startActivity(view.getContext());
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
                    AddFriends.startActivity(view.getContext());
                }
            });
        }else if(item.getmButtonText2().equals("View Messages")){
            button2.setVisibility(View.GONE);
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    //MessagesView.startActivity(view.getContext());
                }
            });
        }

    }

}
