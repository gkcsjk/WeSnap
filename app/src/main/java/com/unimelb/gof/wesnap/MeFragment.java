package com.unimelb.gof.wesnap;

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
import com.unimelb.gof.wesnap.friend.AddFriendChooserActivity;
import com.unimelb.gof.wesnap.friend.ViewFriendsActivity;
import com.unimelb.gof.wesnap.friend.ViewRequestsActivity;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

/**
 * MeFragment
 * Shows current user information and provides button for friendship features.
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
    private DatabaseReference mMyUserRef;

    public MeFragment() {
    }

    // ======================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_me, container, false);

        mAvatar = (ImageView) rootView.findViewById(R.id.my_avatar);
        mDisplayedName = (TextView) rootView.findViewById(R.id.my_name);
        mUsername = (TextView) rootView.findViewById(R.id.my_username);
        setCurrentUserInfo();

        //--------------
        mAddFriendButton = (Button) rootView.findViewById(
                R.id.button_add_friends);
        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        getActivity(), AddFriendChooserActivity.class);
                startActivity(intent);
            }
        });

        //--------------
        mViewRequestButton = (Button) rootView.findViewById(
                R.id.button_view_requests);
        mViewRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        getActivity(), ViewRequestsActivity.class);
                startActivity(intent);
            }
        });

        //--------------
        mViewFriendsButton = (Button) rootView.findViewById(
                R.id.button_view_friends);
        mViewFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        getActivity(), ViewFriendsActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    // ======================================================
    private void setCurrentUserInfo() {
        /* Check AppParams */
        if (AppParams.currentUser != null) {
            showInfo(AppParams.currentUser);
            return;
        }

        /* Confirm Current User */
        mMyUserRef = FirebaseUtil.getMyUserRef();
        if (mMyUserRef == null) {
            Log.e(TAG, "unexpected null; goToLogin()");
            (new BaseActivity()).goToLogin("null");
            return;
        }

        /* ValueEventListener for Current User */
        mMyUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.w(TAG, "getCurrentUser:onDataChange");
                User currentUser = dataSnapshot.getValue(User.class);
                showInfo(currentUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getCurrentUser:onCancelled",
                        databaseError.toException());
            }
        });
    }

    // ======================================================
    private void showInfo(User currentUser) {
        String avatarUrl = currentUser.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.length() == 0) {
            mAvatar.setImageResource(R.drawable.avatar_default_2);
        } else {
            GlideUtil.loadProfileIcon(avatarUrl, mAvatar);
        }
        mDisplayedName.setText(currentUser.getDisplayedName());
        mUsername.setText(currentUser.getUsername());
    }

    // ======================================================
}
