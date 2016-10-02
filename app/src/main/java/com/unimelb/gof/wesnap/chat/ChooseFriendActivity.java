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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.friend.FriendRequest;
import com.unimelb.gof.wesnap.models.Chat;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ChooseFriendActivity
 * This activity allows user to choose from his/her friend list.
 *
 * @author Qi Deng (dengq@student.unimelb.edu.au)
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class ChooseFriendActivity extends BaseActivity {
    private static final String TAG = "ChooseFriendActivity";

    /* UI Variables */
    public RecyclerView mFriendsRecyclerView;
    public RecyclerView.Adapter<FriendViewHolder> mRecyclerAdapter;
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
        refMyFriendIds = FirebaseUtil.getCurrentFriendsRef();
        refMyChatIds = FirebaseUtil.getCurrentChatsRef();
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
        mRecyclerAdapter = new FriendChooserAdapter(ChooseFriendActivity.this, refMyFriendIds);
        mFriendsRecyclerView.setAdapter(mRecyclerAdapter);
    }

    // ======================================================
    /* FriendViewHolder */
    public class FriendViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarView;
        public TextView nameView;

        public FriendViewHolder(View itemView) {
            super(itemView);
            avatarView = (ImageView) itemView.findViewById(R.id.avatar_friend);
            nameView = (TextView) itemView.findViewById(R.id.text_name_friend);
        }
    }

    // ======================================================
    /* FriendsAdapter */
    public class FriendChooserAdapter extends RecyclerView.Adapter<FriendViewHolder> {

        public Context mContext;
        public DatabaseReference mDatabaseReference;
        public ChildEventListener mChildEventListener;

        public List<String> mFriendIds = new ArrayList<>();
        public List<User> mFriends = new ArrayList<>();

        public FriendChooserAdapter(final Context context, DatabaseReference ref) {
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
                                        FriendRequest.removeFriendAfromB(
                                                newFriendId, FirebaseUtil.getCurrentUserId());
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
        public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_friend, parent, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final FriendViewHolder viewHolder, int position) {
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

            // Set up item click listener
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check if exists an active "chat" for the selected friend
                    // and act accordingly
                    checkExistingChats(mFriendIds.get(index), name);
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

    // ======================================================
    /* check if exists an active "chat" for the selected friend */
    private void checkExistingChats(final String uid, final String name) {
        // get current user's chat ids
        refMyChatIds.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getChatIds:onDataChange");

                HashMap<String, Boolean> mChatIds =
                        (HashMap<String, Boolean>) dataSnapshot.getValue();
                if (mChatIds == null) {
                    // no chat!
                    return;
                }

                final Object[] mChatIdArray = mChatIds.keySet().toArray();
                for (Object c : mChatIdArray) {
                    // get the chat record with id = c
                    FirebaseUtil.getChatsRef().child(c.toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String chatId = dataSnapshot.getKey();
                                    Log.d(TAG, "getChat:onDataChange:" + chatId);

                                    if (!dataSnapshot.exists()) {
                                        Log.w(TAG, "refMyChatIds:unexpected non-existing chat id=" + chatId);
                                        refMyChatIds.child(chatId).removeValue();
                                        return;
                                    }

                                    // check if contains selectedFriendId
                                    Chat chat = dataSnapshot.getValue(Chat.class);
                                    if (chat.getParticipants().containsKey(uid)) {
                                        // found one! enter that chat
                                        goToChat(chatId, name);
                                    } else if (chatId.equals(mChatIdArray[mChatIdArray.length - 1].toString())) {
                                        // the last one! start a new chat
                                        startNewChat(uid, name);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "getChat:onCancelled", databaseError.toException());
                                }
                            });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getChatIds:onCancelled", databaseError.toException());
            }
        });
    }

    // ======================================================
    /* Direct User to an existing chat */
    private void goToChat(String chatId, String chatTitle) {
        Log.d(TAG, "goToChat:id=" + chatId);
        Intent intent = new Intent(ChooseFriendActivity.this, MessagesActivity.class);
        intent.putExtra(MessagesActivity.EXTRA_CHAT_ID, chatId);
        intent.putExtra(MessagesActivity.EXTRA_CHAT_TITLE, chatTitle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // TODO proper flag?
        startActivity(intent);
        finish();
    }

    // ======================================================
    /* Create a new chat for the selected friend */
    private void startNewChat(final String uid, final String name) {
        Log.d(TAG, "startNewChat:uid=" + uid);

        DatabaseReference refCurrentUser = FirebaseUtil.getCurrentUserRef();
        if (refCurrentUser == null) { // error out
            Log.e(TAG, "current user ref unexpectedly null; goToLogin()");
            goToLogin("current user ref: null");
            return;
        }

        // get current user info
        refCurrentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getCurrentUser:onDataChange:" + dataSnapshot.getKey());

                if (!dataSnapshot.exists()) {
                    Log.e(TAG, "unexpected non-existing user; goToLogin()");
                    goToLogin("current user: null");
                    return;
                }

                // create a new chat item
                User me = dataSnapshot.getValue(User.class);
                HashMap<String, String> participants = new HashMap<>();
                participants.put(uid, name);
                participants.put(me.getUid(), me.getDisplayedName());
                Chat newChat = new Chat(participants, null, null, null);
                DatabaseReference newChatRef = FirebaseUtil.getChatsRef().push();
                newChatRef.setValue(newChat);

                // now go to the new chat
                goToChat(newChatRef.getKey(), name);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getCurrentUser:onCancelled", databaseError.toException());
            }
        });
    }
}
