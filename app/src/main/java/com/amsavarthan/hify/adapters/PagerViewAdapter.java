package com.amsavarthan.hify.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.CardView;

import com.amsavarthan.hify.ui.fragments.FriendsFragment;
import com.amsavarthan.hify.ui.fragments.HifiFragment;
import com.amsavarthan.hify.ui.fragments.ProfileFragment;

/**
 * Created by amsavarthan on 21/2/18.
 */

public class PagerViewAdapter extends FragmentPagerAdapter{


    public PagerViewAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                ProfileFragment profileFragment=new ProfileFragment();
                return profileFragment;
            case 1:
                FriendsFragment friendsFragment=new FriendsFragment();
                return friendsFragment;
            case 2:
                HifiFragment hifiFragment=new HifiFragment();
                return hifiFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }


}
