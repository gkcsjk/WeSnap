package com.unimelb.gof.wesnap.friend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.R;

/**
 * AddFriendChooserActivity
 * This activity provides the different options for users to add friends.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class AddFriendChooserActivity extends BaseActivity
        implements View.OnClickListener {
    private static final String TAG = "AddFriendChooser";
    private static final String SHARE_MESSAGE = "Add me on WeSnap!!! Username: ";

    /* UI components */
    private Button mSearchButton;
    private Button mShareButton;
    private Button mOtherButton;

    /* Firebase Database variables */
    private DatabaseReference mCurrentUserRef;
    private DatabaseReference mCurrentFriendsRef;

    // ========================================================
    /* onCreate() */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_chooser);

        // Add Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_add_friend);
        setSupportActionBar(mToolbar);

        // UI components
        mSearchButton = (Button) findViewById(R.id.option_search_username);
        mSearchButton.setOnClickListener(this);
        mShareButton = (Button) findViewById(R.id.option_share_username);
        mShareButton.setOnClickListener(this);
        mOtherButton = (Button) findViewById(R.id.option_other_options);
        mOtherButton.setOnClickListener(this);
    }

    // ========================================================
    /* onClick(): perform the requested actions by the clicked buttons */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
            case R.id.option_search_username:
                doSearch();
                break;
            case R.id.option_share_username:
                doShare();
                break;
            case R.id.option_nearby_options:
//                Toast.makeText(AddFriendChooserActivity.this,
//                        R.string.action_other_options,
//                        Toast.LENGTH_SHORT).show();
                doNearby();
                break;
        }
    }

    // ========================================================
    // direct to the relevant activity or start relevant actions
    // ========================================================
    /* Search for other users by usernames */
    private void doSearch() {
        Log.d(TAG, "search:username");
        Intent intent = new Intent(AddFriendChooserActivity.this, SearchUsernameActivity.class);
        startActivity(intent);
    }

    // ========================================================
    /* Share username (plain text) by invoking the system share action */
    // TODO share link that trigger the app?
    private void doShare() {
        Log.d(TAG, "share:username");
        if (AppParams.currentUser != null && AppParams.getMyUsername() != null) {
            // share the username via system action
            shareMyUsername(AppParams.getMyUsername());
            return;
        }
        FirebaseUtil.getCurrentUserRef().child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "getUsername:onDataChange");
                        String username = (String) dataSnapshot.getValue();

                        if (username == null) {
                            // null value, error out
                            Log.e(TAG, "Username unexpectedly null");
                            Toast.makeText(AddFriendChooserActivity.this,
                                    "Error: could not fetch username.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // share the username via system action
                            shareMyUsername(username);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                    }
                });
    }

    private void shareMyUsername(String username) {
        // share the username via system action
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                SHARE_MESSAGE + username);
        sendIntent.setType("text/plain");
        startActivity(
                Intent.createChooser(
                        sendIntent,
                        getResources().getText(R.string.action_share_username_to))
        );
    }

    // ========================================================
    private void doNearby() {
        Log.d(TAG, "nearby:username");
        if (AppParams.currentUser != null && AppParams.getMyUsername() != null) {
            // share the username via system action
            searchNearby(AppParams.getMyUsername());
            return;
        }
        FirebaseUtil.getCurrentUserRef().child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "getUsername:onDataChange");
                        String username = (String) dataSnapshot.getValue();

                        if (username == null) {
                            // null value, error out
                            Log.e(TAG, "Username unexpectedly null");
                            Toast.makeText(AddFriendChooserActivity.this,
                                    "Error: could not fetch username.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // share the username via system action
                            searchNearby(username);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                    }
                });

    }

    private void searchNearby(String username){
        Log.d(TAG, "nearby users");
        Intent intent = new Intent(this, SearchNearbyActivity.class);
        intent.putExtra(SearchNearbyActivity.EXTRA_USERNAME, username);
        startActivity(intent);
    }

    // ========================================================
    /* onKeyDown() */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // finish the current activity if it is not finished already
            if (!isFinishing()) {
                finish();
            }
            return true;
        }
        return false;
    }

}
