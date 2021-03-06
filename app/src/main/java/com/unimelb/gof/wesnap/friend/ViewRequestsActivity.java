package com.unimelb.gof.wesnap.friend;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.User;

import java.util.HashMap;

/**
 * ViewRequestsActivity
 * This activity shows the pending friend requests received by current user and
 * allows the user accept or deny the requests.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class ViewRequestsActivity extends BaseActivity {
    private static final String TAG = "ViewRequestsActivity";

    /* UI components */
    private TextView mNotFoundText;
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<
            User, FriendItemViewHolder> mRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database variables */
    private DatabaseReference refCurrentRequests;
    private DatabaseReference refCurrentFriends;
    private ValueEventListener mListenerCurrentFriends;
    private HashMap<String, Boolean> mFriendIds;

    // ========================================================
    /* onCreate() */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        /* Firebase Database variables */
        refCurrentRequests = FirebaseUtil.getMyFriendRequestsRef();

        /* Get friend list */
        if (AppParams.currentUser != null) {
            mFriendIds = (HashMap) AppParams.currentUser.getFriends();
        } else {
            mFriendIds = null;
        }

        /* UI components */
        // toolbar with title
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_request);
        setSupportActionBar(mToolbar);

        // TextView for "not found"
        mNotFoundText = (TextView) findViewById(R.id.text_request_404);
        mNotFoundText.setVisibility(View.GONE);

        // RecyclerView for "found"
        mRecyclerView = (RecyclerView) findViewById(
                R.id.recycler_friend_requests);
        mRecyclerView.setTag(TAG);
        mRecyclerView.setVisibility(View.GONE);
        mLinearLayoutManager = new LinearLayoutManager(ViewRequestsActivity.this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        // create the recycler adapter
        mRecyclerAdapter = new FirebaseRecyclerAdapter<User, FriendItemViewHolder>(
                User.class,
                R.layout.item_friend,
                FriendItemViewHolder.class,
                refCurrentRequests) {

            @Override
            protected void populateViewHolder(final FriendItemViewHolder viewHolder,
                                              final User requestSender,
                                              final int position) {
                Log.d(TAG, "populateViewHolder:" + position);

                final DatabaseReference refRequest = getRef(position);

                // Load item view with user info
                viewHolder.nameView.setText(requestSender.getDisplayedName());
                viewHolder.emailView.setText(requestSender.getEmail());
                String avatarUrl = requestSender.getAvatarUrl();
                if (avatarUrl != null && avatarUrl.length() != 0) {
                    GlideUtil.loadProfileIcon(avatarUrl, viewHolder.avatarView);
                } else {
                    viewHolder.avatarView.setImageResource(
                            R.drawable.ic_default_avatar);
                }

                // Use friend list from AppParam to check if is friend already
                // Set up button action and UI accordingly
                final String fromUid = refRequest.getKey();
                if (mFriendIds != null && mFriendIds.containsKey(fromUid)) {
                    // isFriend == true
                    viewHolder.useDoneButton();
                    // remove request node from database
                    refRequest.removeValue();
                } else {
                    // otherwise, enable the button to accept friend request
                    viewHolder.useAcceptButton(refRequest);
                }
            }
        };
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // ========================================================
    /* onStop(): Remove database value event listener */
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mListenerCurrentFriends != null) {
            refCurrentFriends.removeEventListener(mListenerCurrentFriends);
        }
    }

    // ========================================================
}


