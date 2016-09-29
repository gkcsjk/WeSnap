package com.unimelb.gof.wesnap.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import com.unimelb.gof.wesnap.friend.AddFriendChooserActivity;
import com.unimelb.gof.wesnap.friend.FriendRequest;
import com.unimelb.gof.wesnap.friend.ViewRequestsActivity;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * FriendsFragment
 * This fragment monitors the list of friends of the current user
 * from Firebase Database, and displays the friend info.
 * It also directs the user to do "Add Friends" and "View Friend Requests".
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class FriendsFragment extends Fragment {
    private static final String TAG = "FriendsFragment";

    /* UI Variables */
    private Button mAddFriendButton;
    private Button mViewRequestButton;
    private RecyclerView mFriendsRecyclerView;
    private FriendsAdapter mRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database variables */
    private DatabaseReference refMyFriendIds;
    private ChildEventListener mListenerMyFriendIds;

    public FriendsFragment() {
    }

    /* Returns a singleton instance of this fragment */
    private static FriendsFragment mFriendsFragment = null;
    public static FriendsFragment getInstance() {
        if (mFriendsFragment == null) {
            mFriendsFragment = new FriendsFragment();
        }
        return mFriendsFragment;
    }

    // ========================================================
    /* onCreateView() */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        mAddFriendButton = (Button) rootView.findViewById(R.id.button_add_friend);
        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddFriendChooserActivity.class);
                startActivity(intent);
            }
        });

        mViewRequestButton = (Button) rootView.findViewById(R.id.button_view_request);
        mViewRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewRequestsActivity.class);
                startActivity(intent);
            }
        });

        mFriendsRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_friends);
        mFriendsRecyclerView.setTag(TAG);

        return rootView;
    }

    // ========================================================
    /** onActivityCreated() */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Database Refs
        refMyFriendIds = FirebaseUtil.getCurrentFriendsRef();

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);
        mFriendsRecyclerView.setLayoutManager(mLinearLayoutManager);

        // UI: RecyclerAdapter
        mRecyclerAdapter = new FriendsAdapter(getActivity(), refMyFriendIds);
        mFriendsRecyclerView.setAdapter(mRecyclerAdapter);
    }

    // ========================================================
    /* onStop() */
    @Override
    public void onStop() {
        super.onStop();
        // Remove database value event listener
        mRecyclerAdapter.cleanupListener();
    }

    // ======================================================
    /** FriendsListViewHolder */
    public static class FriendsListViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarView;
        public TextView nameView;

        public FriendsListViewHolder(View itemView) {
            super(itemView);
            avatarView = (ImageView) itemView.findViewById(R.id.avatar_friend);
            nameView = (TextView) itemView.findViewById(R.id.text_name_friend);
        }
    }

    // ======================================================
    /** FriendsListViewHolder */
    private static class FriendsAdapter extends RecyclerView.Adapter<FriendsListViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mFriendIds = new ArrayList<>();
        private List<User> mFriends = new ArrayList<>();

        public FriendsAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getFriends:onChildAdded:" + dataSnapshot.getKey());
                    // get friend id and ref
                    final String newFriendId = (String) dataSnapshot.getKey();
                    FirebaseUtil.getUsersRef().child(newFriendId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "getUser:onDataChange:" + dataSnapshot.getKey());
                            if (dataSnapshot.exists()) {
                                // load friend's user data
                                User friend = dataSnapshot.getValue(User.class);
                                // update RecyclerView
                                mFriends.add(friend);
                                mFriendIds.add(newFriendId);
                                notifyItemInserted(mFriends.size() - 1);
                            } else {
                                Log.d(TAG, "refMyFriendIds:unexpected null user id=" + newFriendId);
                                FriendRequest.removeFriendAfromB(
                                        newFriendId, FirebaseUtil.getCurrentUserId());
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getFriends:onChildChanged:" + dataSnapshot.getKey());
                    Toast.makeText(mContext, "Changed:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "getFriends:onChildRemoved:" + dataSnapshot.getKey());
                    // get friend id and index
                    String removedFriendId = (String) dataSnapshot.getKey();
                    int friendIndex = mFriendIds.indexOf(removedFriendId);
                    if (friendIndex > -1) {
                        // Remove data from the list
                        mFriendIds.remove(friendIndex);
                        mFriends.remove(friendIndex);
                        // Update the RecyclerView
                        notifyItemRemoved(friendIndex);
                    } else {
                        Log.w(TAG, "getFriends:onChildRemoved:unknown_child:" + removedFriendId);
                    }
                    // TODO how does the active "Chat" knows when a friendship ends?
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getFriends:onChildMoved:" + dataSnapshot.getKey());
                    // This method is triggered when a child location's priority changes.
                    Toast.makeText(mContext, "Moved:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getFriends:onCancelled", databaseError.toException());
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
        public FriendsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_friend, parent, false);
            return new FriendsListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FriendsListViewHolder viewHolder, int position) {
            Log.w(TAG, "populateViewHolder:" + position);

            // Load the item view with friend user info
            User friend = mFriends.get(position);
            viewHolder.nameView.setText(friend.getDisplayedName());
            String avatarUrl = friend.getAvatarUrl();
            if (avatarUrl != null && avatarUrl.length() != 0) {
                GlideUtil.loadProfileIcon(avatarUrl, viewHolder.avatarView);
            } else {
                viewHolder.avatarView.setImageResource(R.drawable.ic_default_avatar);
            }
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
