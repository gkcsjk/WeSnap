package com.unimelb.gof.wesnap.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.MessagesActivity;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.Chat;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatsFragment
 * This fragment provides UI for current user's active chats, and
 * directs user to the messages when selecting one of the chat.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class ChatsFragment extends Fragment {
    private static final String TAG = "ChatsFragment";

    /* UI Variables */
    private RecyclerView mChatsRecyclerView;
    private ChatsAdapter mRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database variables */
    private DatabaseReference refMyChatIds;

    public ChatsFragment() {
    }

//    /* Returns a singleton instance of this fragment */
//    private static ChatsFragment mChatsFragment = null;
//    public static ChatsFragment getInstance() {
//        if (mChatsFragment == null) {
//            mChatsFragment = new ChatsFragment();
//        }
//        return mChatsFragment;
//    }

    // ======================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        mChatsRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_chats);
        mChatsRecyclerView.setTag(TAG);
        return rootView;
    }

    // ========================================================
    /** onActivityCreated() */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        // Database Refs
        refMyChatIds = FirebaseUtil.getCurrentChatsRef();

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);
        mChatsRecyclerView.setLayoutManager(mLinearLayoutManager);

        // UI: RecyclerAdapter
        mRecyclerAdapter = new ChatsAdapter(getActivity(), refMyChatIds);
        mChatsRecyclerView.setAdapter(mRecyclerAdapter);
    }

    // ========================================================
    /* onStop(): Remove database value event listener */
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        //mRecyclerAdapter.cleanupListener();
    }

    // ======================================================
    /* ChatsListViewHolder */
    public static class ChatsListViewHolder extends RecyclerView.ViewHolder {
        public TextView lastmsgView;
        public ImageView avatarView;
        public TextView titleView;

        public ChatsListViewHolder(View v) {
            super(v);

            lastmsgView = (TextView) itemView.findViewById(R.id.text_last_msg_chat);
            avatarView = (ImageView) itemView.findViewById(R.id.avatar_chat);
            titleView = (TextView) itemView.findViewById(R.id.text_title_chat);
        }
    }

    // ======================================================
    /* ChatsAdapter */
    private class ChatsAdapter extends RecyclerView.Adapter<ChatsListViewHolder> {
        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mChatIds = new ArrayList<>();
        private List<Chat> mChats = new ArrayList<>();

        public ChatsAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getChatIds:onChildAdded:" + dataSnapshot.getKey());
                    // get chatId
                    final String newChatId = dataSnapshot.getKey();
                    // get "chats/chatId/"
                    FirebaseUtil.getChatsRef().child(newChatId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d(TAG, "getChat:onDataChange:" + dataSnapshot.getKey());
                                    if (!dataSnapshot.exists()) {
                                        Log.w(TAG, "refMyChatIds:unexpected non-existing chat id=" + newChatId);
                                        refMyChatIds.child(newChatId).removeValue();
                                        return;
                                    }
                                    // load friend's user data
                                    Chat chat = dataSnapshot.getValue(Chat.class);
                                    // update RecyclerView
                                    mChats.add(chat);
                                    mChatIds.add(newChatId);
                                    notifyItemInserted(mChats.size() - 1);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "getChat:onCancelled", databaseError.toException());
                                }
                            });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getChatIds:onChildChanged:" + dataSnapshot.getKey());
                    Toast.makeText(mContext, "Changed:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "getChatIds:onChildRemoved:" + dataSnapshot.getKey());
                    // get chat id and index
                    String removedChatId = dataSnapshot.getKey();
                    int chatIndex = mChatIds.indexOf(removedChatId);
                    if (chatIndex > -1) {
                        // Remove data from the list
                        mChatIds.remove(chatIndex);
                        mChats.remove(chatIndex);
                        // Update the RecyclerView
                        notifyItemRemoved(chatIndex);
                    } else {
                        Log.w(TAG, "getChatIds:onChildRemoved:unknown_child:" + removedChatId);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getChatIds:onChildMoved:" + dataSnapshot.getKey());
                    // This method is triggered when a child location's priority changes.
                    Toast.makeText(mContext, "Moved:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getChatIds:onCancelled", databaseError.toException());
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
        public ChatsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_chat, parent, false);
            return new ChatsListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ChatsListViewHolder viewHolder, final int position) {
            Log.d(TAG, "populateViewHolder:" + position);

            // Load the item view with friend user info
            final Chat chat = mChats.get(position);
            viewHolder.lastmsgView.setText(chat.getLastMessageBody());
            viewHolder.titleView.setText(chat.getChatTitle());
            String avatarUrl = chat.getChatAvatarUrl();
            if (avatarUrl != null && avatarUrl.length() != 0) {
                GlideUtil.loadProfileIcon(avatarUrl, viewHolder.avatarView);
            } else {
                viewHolder.avatarView.setImageResource(R.drawable.ic_default_avatar);
            }
        }

        @Override
        public int getItemCount() {
            return mChats.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
}
