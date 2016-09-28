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
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.R;

/**
 * AddFriendChooserActivity
 * This activity provides the different ways for users to add friends.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class AddFriendChooserActivity extends BaseActivity
        implements View.OnClickListener {
    private static final String TAG = "AddFriendChooser";

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
                Toast.makeText(AddFriendChooserActivity.this,
                        R.string.action_share_username,
                        Toast.LENGTH_SHORT).show();
                doShare();
                break;
            case R.id.option_other_options:
                Toast.makeText(AddFriendChooserActivity.this,
                        R.string.action_other_options,
                        Toast.LENGTH_SHORT).show();
                doOther();
                break;
        }
    }

    // ========================================================
    // direct to the relevant activity or start relevant actions
    // ========================================================
    /* Search for other users by usernames */
    private void doSearch() {
        // TODO doSearch()
        Intent intent = new Intent(AddFriendChooserActivity.this, SearchUsernameActivity.class);
        startActivity(intent);
    }

    // ========================================================
    /* Share username (plain text) by invoking the system share action */
    private void doShare() {
        DatabaseReference refCurrentUsername = FirebaseUtil.getCurrentUserUsername();
        refCurrentUsername.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w(TAG, "getUsername:onDataChange");
                        String username = (String) dataSnapshot.getValue();

                        if (username == null) {
                            // null value, error out
                            Log.e(TAG, "Username unexpectedly null");
                            Toast.makeText(AddFriendChooserActivity.this,
                                    "Error: could not fetch username.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // share the username via system action
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT,
                                    "Add me on WeSnap!!! Username: " + username);
                            sendIntent.setType("text/plain"); // TODO url links?
                            startActivity(Intent.createChooser(
                                    sendIntent,
                                    getResources().getText(R.string.action_share_username_to)));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                    }
                });
    }

    // ========================================================
    private void doOther() {
        // TODO doOther()
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
