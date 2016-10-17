package com.unimelb.gof.wesnap.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.friend.FriendHandler;
import com.unimelb.gof.wesnap.friend.FriendItemViewHolder;
import com.unimelb.gof.wesnap.models.Chat;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ChooseFriendActivity
 * This activity allows user to choose from his/her friend list.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class ChooseFriendActivity extends BaseActivity {
    private static final String TAG = "ChooseFriendActivity";

    public static final String EXTRA_TIME_TO_LIVE = "time_to_live";
    public static final String EXTRA_PHOTO_PATH = "photo_path";

    /* UI Variables */
    public RecyclerView mFriendsRecyclerView;
    public FriendChooserAdapter mRecyclerAdapter;
    public LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database variables */
    public DatabaseReference refMyFriendIds;
    private DatabaseReference refMyChatIds;

    // ========================================================
    /* onCreate() */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friends);

        /* Firebase Database variables */
        refMyFriendIds = FirebaseUtil.getMyFriendIdsRef();
        refMyChatIds = FirebaseUtil.getMyChatIdsRef();
        if (refMyFriendIds == null || refMyChatIds == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            goToLogin("unexpected null value");
            return;
        }

        /* UI components */
        // UI: toolbar with title
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_friends);
        setSupportActionBar(mToolbar);

        // UI: RecyclerView
        mFriendsRecyclerView = (RecyclerView) findViewById(R.id.recycler_friends);

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(ChooseFriendActivity.this);
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);
        mFriendsRecyclerView.setLayoutManager(mLinearLayoutManager);

        // UI: RecyclerAdapter
        mRecyclerAdapter = new FriendChooserAdapter(
                ChooseFriendActivity.this, refMyFriendIds);
        mFriendsRecyclerView.setAdapter(mRecyclerAdapter);
    }

    // ========================================================
    /* onStop(): remove listener */
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        mRecyclerAdapter.cleanupListener();
    }

    // ======================================================
    /* FriendChooserAdapter */
    private class FriendChooserAdapter extends RecyclerView.Adapter<FriendItemViewHolder> {

        Context mContext;
        DatabaseReference mDatabaseReference;
        ChildEventListener mChildEventListener;

        List<String> mFriendIds = new ArrayList<>();
        List<User> mFriends = new ArrayList<>();

        FriendChooserAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getFriendIds:onChildAdded:" + dataSnapshot.getKey());
                    // get friendId
                    final String newFriendId = dataSnapshot.getKey();
                    // get "users/friendId/"
                    FirebaseUtil.getUsersRef().child(newFriendId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d(TAG, "getUser:onDataChange:" + dataSnapshot.getKey());
                                    if (!dataSnapshot.exists()) {
                                        Log.w(TAG, "refMyFriendIds:unexpected non-existing user id=" + newFriendId);
                                        FriendHandler.removeFriendAfromB(
                                                newFriendId, FirebaseUtil.getMyUid());
                                        return;
                                    }
                                    // load friend's user data
                                    User friend = dataSnapshot.getValue(User.class);
                                    // update RecyclerView
                                    mFriends.add(friend);
                                    mFriendIds.add(newFriendId);
                                    notifyItemInserted(mFriends.size() - 1);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                }
                            });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getFriendIds:onChildChanged:" + dataSnapshot.getKey());
                    Toast.makeText(mContext, "Changed:" + dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "getFriendIds:onChildRemoved:" + dataSnapshot.getKey());
                    // get friend id and index
                    String removedFriendId = dataSnapshot.getKey();
                    int friendIndex = mFriendIds.indexOf(removedFriendId);
                    if (friendIndex > -1) {
                        // Remove data from the list
                        mFriendIds.remove(friendIndex);
                        mFriends.remove(friendIndex);
                        // Update the RecyclerView
                        notifyItemRemoved(friendIndex);
                    } else {
                        Log.w(TAG, "getFriendIds:onChildRemoved:unknown_child:" + removedFriendId);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getFriendIds:onChildMoved:" + dataSnapshot.getKey());
                    // This method is triggered when a child location's priority changes.
                    Toast.makeText(mContext, "Moved:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getFriendIds:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load friends.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public FriendItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_friend, parent, false);
            return new FriendItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final FriendItemViewHolder viewHolder,
                                     int position) {
            Log.d(TAG, "populateViewHolder:" + position);

            // Load the item view with friend user info
            final int index = position;
            User friend = mFriends.get(index);
            final String name = friend.getDisplayedName();
            viewHolder.nameView.setText(name);
            final String avatarUrl = friend.getAvatarUrl();
            if (avatarUrl != null && avatarUrl.length() != 0) {
                GlideUtil.loadProfileIcon(avatarUrl, viewHolder.avatarView);
            } else {
                viewHolder.avatarView.setImageResource(R.drawable.ic_default_avatar);
            }
            viewHolder.emailView.setText(friend.getEmail());
            viewHolder.doButton.setVisibility(View.GONE);

            // Set up item click listener
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check if exists an active "chat" for the selected friend
                    // and act accordingly
                    ChatStarter.checkExistingChats(
                            ChooseFriendActivity.this,
                            mFriendIds.get(index), name);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFriends.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
    }
}
