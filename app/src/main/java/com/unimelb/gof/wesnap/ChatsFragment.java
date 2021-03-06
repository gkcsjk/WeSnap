package com.unimelb.gof.wesnap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.unimelb.gof.wesnap.chat.MessagesActivity;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.models.Chat;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private DatabaseReference mMyChatIdsRef;
    private String mMyUid;

    public ChatsFragment() {
    }

    // ======================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_chats, container, false);

        mChatsRecyclerView = (RecyclerView) rootView.findViewById(
                R.id.recycler_chats);
        mChatsRecyclerView.setTag(TAG);

        return rootView;
    }

    // ========================================================
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        // Database Refs
        mMyChatIdsRef = FirebaseUtil.getMyChatIdsRef();
        mMyUid = FirebaseUtil.getMyUidNonNull();
        if (mMyChatIdsRef == null || mMyUid == null) {
            Log.e(TAG, "unexpected null; goToLogin()");
            (new BaseActivity()).goToLogin("null");
            return;
        }

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);
        mChatsRecyclerView.setLayoutManager(mLinearLayoutManager);

        // UI: RecyclerAdapter
        mRecyclerAdapter = new ChatsAdapter(getActivity(), mMyChatIdsRef);
        mChatsRecyclerView.setAdapter(mRecyclerAdapter);
    }

    // ======================================================
    /* MemoryViewHolder */
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView lastmsgView;
        public ImageView avatarView;
        public TextView titleView;

        public ChatViewHolder(View v) {
            super(v);
            lastmsgView = (TextView) itemView.findViewById(
                    R.id.text_last_msg_chat);
            avatarView = (ImageView) itemView.findViewById(
                    R.id.avatar_chat);
            titleView = (TextView) itemView.findViewById(
                    R.id.text_title_chat);
            itemView.setLongClickable(true);
        }
    }

    // ======================================================
    /* MemoriesAdapter */
    private class ChatsAdapter extends RecyclerView.Adapter<ChatViewHolder> {
        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mChatIds = new ArrayList<>();
        private List<Chat> mChats = new ArrayList<>();
        private List<ValueEventListener> mChatListeners = new ArrayList<>();

        public ChatsAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot,
                                         String previousChildName) {
                    Log.d(TAG, "getChatIds:onChildAdded:" +
                            dataSnapshot.getKey());

                    // get chatId
                    String newChatId = dataSnapshot.getKey();

                    // [START create the chat listener]
                    ValueEventListener chatListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String chatId = dataSnapshot.getKey();
                            Log.d(TAG, "getChat:onDataChange:" + chatId);

                            if (!dataSnapshot.exists()) {
                                Log.w(TAG, "getChat:non-existing chat id");
                                mMyChatIdsRef.child(chatId).removeValue();
                                return;
                            }

                            // load friend's user data
                            Chat chat = dataSnapshot.getValue(Chat.class);
                            // update RecyclerView
                            int chatIndex = mChatIds.indexOf(chatId);
                            if (chatIndex > -1) { // existing one
                                mChats.set(chatIndex, chat);
                                mChatIds.set(chatIndex, chatId);
                                notifyItemChanged(mChats.size() - 1);
                            } else { // new one
                                mChats.add(chat);
                                mChatIds.add(chatId);
                                mChatListeners.add(this);
                                notifyItemInserted(mChats.size() - 1);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getChat:onCancelled", databaseError.toException());
                        }
                    };
                    // [END create the chat listener]

                    // monitor on "chats/chatId/"
                    FirebaseUtil.getChatsRef().child(newChatId)
                            .addValueEventListener(chatListener);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot,
                                           String previousChildName) {
                    Log.d(TAG, "getChatIds:onChildChanged:" +
                            dataSnapshot.getKey());
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "getChatIds:onChildRemoved:" +
                            dataSnapshot.getKey());

                    // get chat id and index
                    String removedChatId = dataSnapshot.getKey();
                    int chatIndex = mChatIds.indexOf(removedChatId);
                    if (chatIndex > -1) {
                        // Remove data from the list
                        mChatIds.remove(chatIndex);
                        mChats.remove(chatIndex);
                        ValueEventListener l = mChatListeners.get(chatIndex);
                        if (l != null) {
                            FirebaseUtil.getChatsRef()
                                    .child(removedChatId)
                                    .removeEventListener(l);
                        }
                        // Update the RecyclerView
                        notifyItemRemoved(chatIndex);
                    } else {
                        Log.w(TAG, "getChatIds:onChildRemoved:unknown_child:" +
                                removedChatId);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot,
                                         String previousChildName) {
                    Log.d(TAG, "getChatIds:onChildMoved:" +
                            dataSnapshot.getKey());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getChatIds:onCancelled",
                            databaseError.toException());
                    Toast.makeText(mContext, "Failed to load chats.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_chat, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ChatViewHolder viewHolder,
                                     final int position) {
            Log.d(TAG, "populateViewHolder:" + position);

            /* Load the item view with chat info */
            final Chat chat = mChats.get(position);
            // ----- last message
            String msg = chat.getAddFriendMessage();
            if (msg != null) {
                viewHolder.lastmsgView.setText(msg);
            } else {
                viewHolder.lastmsgView.setText(chat.getLastMessageBody());
            }
            // ----- chat title / participants
            Map<String, String> participants = chat.getParticipants();
            String uid = null;
            for (String id : participants.keySet()) {
                if (!id.equals(mMyUid)) {
                    uid = id;
                    break;
                }
            }
            String chatTitle = chat.getChatTitle();
            if (chatTitle == null || participants.size() <= 2) {
                // use participants names
                chatTitle = participants.get(uid);
            } // else: group chat with title
            viewHolder.titleView.setText(chatTitle);
            // ----- avatarUrl
            String avatarUrl = chat.getChatAvatarUrl();
            if (avatarUrl != null && avatarUrl.length() != 0) {
                GlideUtil.loadProfileIcon(avatarUrl, viewHolder.avatarView);
            }

            /* on click: directs to message list */
            viewHolder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context context = v.getContext();
                            Intent intent = new Intent(
                                    context, MessagesActivity.class);
                            intent.putExtra(
                                    MessagesActivity.EXTRA_CHAT_ID,
                                    mChatIds.get(position));
                            intent.putExtra(
                                    MessagesActivity.EXTRA_CHAT_TITLE,
                                    viewHolder.titleView.getText().toString());
                            context.startActivity(intent);
                        }
                    }
            );

            /* ValueEventListener for friend's info */
            final ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.w(TAG, "getFriendUser:onDataChange");

                    if (!dataSnapshot.exists()) {
                        // null value; remove node
                        FirebaseUtil.getChatsRef()
                                .child(mChatIds.get(position))
                                .removeValue();
                        return;
                    }

                    // get metadata
                    User myFriend = dataSnapshot.getValue(User.class);
                    String name = myFriend.getDisplayedName();
                    String username = myFriend.getUsername();
                    String email = myFriend.getEmail();

                    // update UI: dialog to show friend's info
                    AlertDialog myFriendMetadataDialog =
                            new AlertDialog.Builder(getActivity()).create();
                    myFriendMetadataDialog.setTitle(name);
                    myFriendMetadataDialog.setMessage(
                            " USERNAME: " + username + "\n EMAIL: " + email);
                    myFriendMetadataDialog.show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getFriendUser:onCancelled",
                            databaseError.toException());
                }
            };

            /* on long click: show friend data */
            final String receiverUid = uid;
            if (receiverUid != null) {
                viewHolder.itemView.setOnLongClickListener(
                        new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                FirebaseUtil.getUsersRef().child(receiverUid)
                                        .addListenerForSingleValueEvent(listener);
                                return true;
                            }
                        }
                );
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
