package com.unimelb.gof.wesnap.friend;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.User;

/**
 * ViewRequestActivity
 * This activity ... TODO comments: ViewRequestActivity
 *
 * @author Qi Deng (dengq@student.unimelb.edu.au)
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class ViewRequestActivity extends BaseActivity {
    private static final String TAG = "ViewRequestActivity";

    /* UI components */
    private TextView mNotFoundText;
    private RecyclerView mResultsRecyclerView;
    private FirebaseRecyclerAdapter<
            User, RequestsListViewHolder> mRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database variables */
    private String idCurrentUser;
    private DatabaseReference refCurrentRequests;

    // ========================================================
    /* onCreate() */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);

        /* Firebase Database variables */
        refCurrentRequests = FirebaseUtil.getCurrentRequestsRef();
        idCurrentUser = FirebaseUtil.getCurrentUserId();
        if (idCurrentUser == null) {
            // something wrong TODO
            goToLogin();
        }

        /* UI components */
        // toolbar with title
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_request);
        setSupportActionBar(mToolbar);

        // TextView for "not found"
        mNotFoundText = (TextView) findViewById(R.id.text_request_404);
        mNotFoundText.setVisibility(View.GONE);

        // RecyclerView for "found"
        mResultsRecyclerView = (RecyclerView) findViewById(R.id.recycler_friend_requests);
        mResultsRecyclerView.setTag(TAG);
        mLinearLayoutManager = new LinearLayoutManager(ViewRequestActivity.this);
        mResultsRecyclerView.setLayoutManager(mLinearLayoutManager);

        // create the recycler adapter
        mRecyclerAdapter = new FirebaseRecyclerAdapter<
                User, RequestsListViewHolder>(
                User.class,
                R.layout.item_friend_request,
                RequestsListViewHolder.class,
                refCurrentRequests) {
            @Override
            protected void populateViewHolder(final RequestsListViewHolder viewHolder,
                                              final User requestSender,
                                              final int position) {
                DatabaseReference refRequest = getRef(position);

                // Load item view with user info
                viewHolder.nameView.setText(requestSender.getDisplayedName());
                viewHolder.emailView.setText(requestSender.getEmail());
                String avatarUrl = requestSender.getAvatarUrl();
                if (avatarUrl != null && avatarUrl.length() != 0) {
                    GlideUtil.loadProfileIcon(avatarUrl, viewHolder.avatarView);
                } else {
                    viewHolder.avatarView.setImageResource(R.drawable.ic_default_avatar);
                }

                // Check if is friend already, and set up button action and UI accordingly
                boolean isFriend = requestSender.getFriends().contains(idCurrentUser);
                if (isFriend) {
                    // update UI
                    viewHolder.changeToDoneButton();
                    // remove request node from database
                    refRequest.removeValue();
                } else {
                    // enable the button to reply to friend request
                    final String fromUserId = refRequest.getKey();
                    viewHolder.doButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // send friend requests
                            FriendRequest.replyFriendRequest(fromUserId, viewHolder);
                            // TODO if false?
                        }
                    });
                }
            }
        };
        mResultsRecyclerView.setAdapter(mRecyclerAdapter);

        mResultsRecyclerView.setVisibility(View.VISIBLE);
    }
}
