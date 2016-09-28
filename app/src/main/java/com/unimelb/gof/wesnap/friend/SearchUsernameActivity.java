package com.unimelb.gof.wesnap.friend;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.User;

import java.util.List;

/**
 * SearchUsernameActivity
 * This activity takes in a username string from user input,
 * returns the search results, and provides the option to add friends.
 *
 * @author Qi Deng (dengq@student.unimelb.edu.au)
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class SearchUsernameActivity extends BaseActivity {
    private static final String TAG = "SearchUsernameActivity";

    /* UI components */
    private EditText mSearchKeywordField;
    private ImageButton mSearchSubmitButton;
    private TextView mNotFoundText;
    private RecyclerView mResultsRecyclerView;
    private FirebaseRecyclerAdapter<
            User, RequestsListViewHolder> mRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database variables */
    private String mSearchKeyword;
    private String mResultUid;

    // ========================================================
    /* onCreate() */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_username);

        /* UI components */
        // toolbar with title
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(mToolbar);

        // search field and button
        mSearchKeywordField = (EditText) findViewById(R.id.field_search_username);
        mSearchSubmitButton = (ImageButton) findViewById(R.id.button_submit_search_username);
        mSearchSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchKeyword = mSearchKeywordField.getText().toString();
                mResultUid = null;
                // update ui
                showProgressDialog();
                mNotFoundText.setVisibility(View.GONE);
                mResultsRecyclerView.setVisibility(View.GONE);
                // access database
                retrieveSearchResults();
            }
        });

        // TextView for "not found"
        mNotFoundText = (TextView) findViewById(R.id.text_search_404);
        mNotFoundText.setVisibility(View.GONE);

        // RecyclerView for "found"
        mResultsRecyclerView = (RecyclerView) findViewById(R.id.recycler_search_results);
        mResultsRecyclerView.setTag(TAG);
        mResultsRecyclerView.setVisibility(View.GONE);
        mLinearLayoutManager = new LinearLayoutManager(SearchUsernameActivity.this);
        mResultsRecyclerView.setLayoutManager(mLinearLayoutManager);
    }

    // ========================================================
    /* retrieveSearchResults(): Access the Firebase Database to retrieve user data */
    private void retrieveSearchResults() {
        Log.w(TAG, "retrieveSearchResults:username=" + mSearchKeyword);

        // retrieve the uid corresponding to the given username
        FirebaseUtil.getUsernamesRef().child(mSearchKeyword)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w(TAG, "getUsername:onDataChange:username=" + mSearchKeyword);
                        if (dataSnapshot.exists()) {
                            // username found
                            mResultUid = (String) dataSnapshot.getValue();
                            showSearchResults(FirebaseUtil.getUser(mResultUid));
                        } else {
                            // username not found
                            mNotFoundText.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                    }
                });
    }

    // ========================================================
    /* showSearchResults(): Update recycler view with the retrieved user data */
    private void showSearchResults(Query queryUser) {
        Log.w(TAG, "showSearchResults:username=" + queryUser.toString());

        // create the recycler adapter for search result
        mRecyclerAdapter = new FirebaseRecyclerAdapter<User, RequestsListViewHolder>(
                User.class,
                R.layout.item_friend_request,
                RequestsListViewHolder.class,
                queryUser) {

            @Override
            protected void populateViewHolder(final RequestsListViewHolder viewHolder,
                                              final User resultUser, final int position) {
                Log.w(TAG, "populateViewHolder:" + position);

                // Load item view with user info
                viewHolder.nameView.setText(resultUser.getDisplayedName());
                viewHolder.emailView.setText(resultUser.getEmail());
                String avatarUrl = resultUser.getAvatarUrl();
                if (avatarUrl != null && avatarUrl.length() != 0) {
                    GlideUtil.loadProfileIcon(avatarUrl, viewHolder.avatarView);
                } else {
                    viewHolder.avatarView.setImageResource(R.drawable.ic_default_avatar);
                }

                // Check if is friend already, and set up button action and UI accordingly
                FirebaseUtil.getCurrentFriendsRef()
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w(TAG, "getMyFriends:onDataChange");
                        if (dataSnapshot.exists()) {
                            List<String> myFriends = (List<String>) dataSnapshot.getValue();
                            if (myFriends != null && myFriends.contains(mResultUid)) {
                                // isFriend = true;
                                viewHolder.changeToDoneButton();
                            } else {
                                // enable the button to send friend request
                                viewHolder.doButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // send friend requests TODO: if false?
                                        FriendRequest.sendFriendRequest(mResultUid);
                                        Snackbar.make(v, "Friend request sent", Snackbar.LENGTH_LONG).show();
                                        // update UI
                                        viewHolder.changeToDoneButton();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getMyFriends:onCancelled", databaseError.toException());
                    }
                });
            }
        };

        // link adapter to RecyclerView
        mResultsRecyclerView.setAdapter(mRecyclerAdapter);

        // show the view
        mResultsRecyclerView.setVisibility(View.VISIBLE);
        hideProgressDialog();
    }

    // ======================================================
}
