package com.unimelb.wesnap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import com.unimelb.wesnap.models.Person;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A placeholder fragment containing a simple view.
 */
public class FriendFragment extends Fragment {

    private static final String TAG = "MyProfileFragment";

    private DatabaseReference mCurrentUserRef;

    private Button mBottonAddFriend; //button_add_friend
    private View.OnClickListener listenerAddFriend;

    /* Fragment singleton??? */
    private static FriendFragment mFriendFragment = null;

    public FriendFragment() {
    }

    /* Returns a singleton instance of this fragment */
    public static FriendFragment getInstance() {
        if (mFriendFragment == null) {
            mFriendFragment = new FriendFragment();
        }
        return mFriendFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentUserRef = FirebaseUtil.getCurrentUserRef();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mBottonAddFriend = (Button) view.findViewById(R.id.button_add_friend);

        // [START settings_click_listener]
        listenerAddFriend = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddFriendActivity.class);
                startActivity(intent);
            }
        };
        mBottonAddFriend.setOnClickListener(listenerAddFriend);
        // [END settings_click_listener]
    }
}