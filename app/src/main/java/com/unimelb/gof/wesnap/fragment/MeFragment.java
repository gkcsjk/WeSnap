package com.unimelb.gof.wesnap.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.friend.AddFriendChooserActivity;
import com.unimelb.gof.wesnap.friend.ViewFriendsActivity;
import com.unimelb.gof.wesnap.friend.ViewRequestsActivity;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

/**
 * MeFragment
 * TODO comments
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class MeFragment extends Fragment {
    private static final String TAG = "MeFragment";

    /* UI Variables */
    private ImageView mAvatar;
    private TextView mDisplayedName;
    private TextView mUsername;
    private Button mAddFriendButton;
    private Button mViewRequestButton;
    private Button mViewFriendsButton;

    /* Firebase Database variables */
    private DatabaseReference refCurrentUser;
    private ValueEventListener mListenerCurrentUser;

    public MeFragment() {
    }

    // ======================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        refCurrentUser = FirebaseUtil.getCurrentUserRef();
        if (refCurrentUser == null) {
            // TODO null value; error out
            return;
        }

        // [START person_value_event_listener]
        ValueEventListener profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.w(TAG, "getCurrentUser:onDataChange");
                User currentUser = dataSnapshot.getValue(User.class);

                String avatarUrl = currentUser.getAvatarUrl();
                if (avatarUrl != null) {
                    GlideUtil.loadProfileIcon(avatarUrl, mAvatar);
                } else {
                    mAvatar.setImageResource(R.drawable.ic_default_avatar);
                }
                mDisplayedName.setText(currentUser.getDisplayedName());
                mUsername.setText(currentUser.getUsername());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getCurrentUser:onCancelled", databaseError.toException());
            }
        };
        refCurrentUser.addValueEventListener(profileListener);
        // [END person_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mListenerCurrentUser = profileListener;
    }

    // ======================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_me, container, false);

        mAddFriendButton = (Button) rootView.findViewById(R.id.button_add_friends);
        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddFriendChooserActivity.class);
                startActivity(intent);
            }
        });

        mViewRequestButton = (Button) rootView.findViewById(R.id.button_view_requests);
        mViewRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewRequestsActivity.class);
                startActivity(intent);
            }
        });

        mViewFriendsButton = (Button) rootView.findViewById(R.id.button_view_friends);
        mViewFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewFriendsActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    // ========================================================
    /* onActivityCreated() */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }
}
