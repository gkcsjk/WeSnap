package com.unimelb.gof.wesnap;

import android.support.v4.app.Fragment;

/**
 * Created by qideng on 20/09/2016.
 */

public class FriendsFragment extends Fragment {
    public FriendsFragment() {

    }

    private static FriendsFragment mFriendsFragment = null;
    /* Returns a singleton instance of this fragment */
    public static FriendsFragment getInstance() {
        if (mFriendsFragment == null) {
            mFriendsFragment = new FriendsFragment();
        }
        return mFriendsFragment;
    }
}
