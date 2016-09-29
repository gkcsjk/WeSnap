package com.unimelb.gof.wesnap.fragment;

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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import com.unimelb.gof.wesnap.MessagesActivity;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.Chat;

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

    /* Firebase Database variables */
    private String idCurrentUser;
    private DatabaseReference refCurrentChats;

    /* UI variables */
    private RecyclerView mChatRecyclerView;
    private FirebaseRecyclerAdapter<Chat, ChatListViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    public ChatsFragment() {
    }

    /* Returns a singleton instance of this fragment */
    private static ChatsFragment mChatsFragment = null;
    public static ChatsFragment getInstance() {
        if (mChatsFragment == null) {
            mChatsFragment = new ChatsFragment();
        }
        return mChatsFragment;
    }

    // ======================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mChatRecyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        mChatRecyclerView.setTag(TAG);
        return mChatRecyclerView;
    }

    // ======================================================
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Database Refs
        idCurrentUser = FirebaseUtil.getCurrentUserId();
        //refCurrentChats = FirebaseUtil.getCurrentChatsRef();
        refCurrentChats = FirebaseUtil.getChatsRef();//TODO change

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);

        // UI: RecyclerAdapter
        // [START create the recycler adapter for chat]
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Chat, ChatListViewHolder>(
                Chat.class,
                R.layout.item_chat,
                ChatListViewHolder.class,
                refCurrentChats) {

            @Override
            protected void populateViewHolder(final ChatListViewHolder viewHolder,
                                              final Chat chat, final int position) {
                Log.w(TAG, "populateViewHolder:" + position);

                // Set click listener for the chat item
                final DatabaseReference refChat = getRef(position);
                final String keyChat = refChat.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar.make(v, "Chat-Message detail view: TBD",
                                Snackbar.LENGTH_LONG).show();
                        // Launch MessagesActivity
                        Intent intent = new Intent(getActivity(), MessagesActivity.class);
                        intent.putExtra(MessagesActivity.EXTRA_CHAT_KEY, keyChat);
                        startActivity(intent);
                    }
                });

                // Load data to the chat item
                viewHolder.lastmsgView.setText(chat.getLastMessageBody());
                viewHolder.nameView.setText(chat.getChatTitle()); // dummy
                viewHolder.avatarView.setImageResource(R.drawable.ic_default_avatar); // dummy

//                for (String p : chat.getParticipants()) {
//                    if (!p.equals(idCurrentUser)) {
//                        // use the first non-myself user as the "receiver"
//                        // TODO reading "user.class" with "getUsersRef().child(p)" ??
//                        FirebaseUtil.getUsersRef().child(p)
//                                .addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                Log.w(TAG, "getUser:onDataChange");
//                                // retrieve the user
//                                User receiver = dataSnapshot.getValue(User.class);
//                                // load the user data to the chat item view
//                                viewHolder.nameView.setText(receiver.getDisplayedName());
//                                String avatarUrl = receiver.getAvatarUrl();
//                                if (avatarUrl != null && avatarUrl.length() != 0) {
//                                    GlideUtil.loadProfileIcon(
//                                            avatarUrl, viewHolder.avatarView);
//                                } else {
//                                    viewHolder.avatarView.setImageResource(
//                                            R.drawable.ic_default_avatar);
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//                                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
//                            }
//                        });
//                        break;
//                    }
//                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(
                new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
//                int chatCount = mFirebaseAdapter.getItemCount();
//                int lastVisiblePosition = mLinearLayoutManager
//                        .findLastCompletelyVisibleItemPosition();
//
//                // If the recycler view is initially being loaded or
//                // the user is at the bottom of the list,
//                // scroll to the bottom of the list to show the newly added message.
//                if (lastVisiblePosition == -1 ||
//                        (positionStart >= (chatCount - 1) &&
//                                lastVisiblePosition == (positionStart - 1))) {
//                    mChatRecyclerView.scrollToPosition(positionStart);
//                }
            }
        });
        // [END create the recycler adapter for chat]

        // UI: link them to RecyclerView
        mChatRecyclerView.setLayoutManager(mLinearLayoutManager);
        mChatRecyclerView.setAdapter(mFirebaseAdapter);
    }

    // ======================================================
    /** ChatListViewHolder */
    public static class ChatListViewHolder extends RecyclerView.ViewHolder {
        public TextView lastmsgView;
        public ImageView avatarView;
        public TextView nameView;

        public ChatListViewHolder(View v) {
            super(v);

            lastmsgView = (TextView) itemView.findViewById(R.id.text_last_msg_chat);
            avatarView = (ImageView) itemView.findViewById(R.id.avatar_chat);
            nameView = (TextView) itemView.findViewById(R.id.text_name_chat);
        }
    }

    // ======================================================
}
