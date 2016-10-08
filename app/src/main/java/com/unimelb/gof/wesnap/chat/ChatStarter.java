package com.unimelb.gof.wesnap.chat;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.models.Chat;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.FirebaseUtil;

import java.util.HashMap;

/**
 * ChatStarter
 * This class contains methods for starting an existing chat or a new chat.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class ChatStarter {
    private static final String TAG = "ChatStarter";
    private static DatabaseReference refMyChatIds = FirebaseUtil.getCurrentChatsRef();

    // ======================================================
    /* check if exists an active "chat" for the selected friend */
    public static void checkExistingChats(final Context context, final String uid, final String name) {
        // get current user's chat ids
        refMyChatIds.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getChatIds:onDataChange");

                HashMap<String, Boolean> mChatIds =
                        (HashMap<String, Boolean>) dataSnapshot.getValue();
                if (mChatIds == null) {
                    // no chat! start a new one
                    startNewChat(context, uid, name);
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
                                        goToChat(context, chatId, name);
                                    } else if (chatId.equals(mChatIdArray[mChatIdArray.length - 1].toString())) {
                                        // the last one! start a new chat
                                        startNewChat(context, uid, name);
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
    private static void goToChat(final Context context, String chatId, String chatTitle) {
        Log.d(TAG, "goToChat:id=" + chatId);
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra(MessagesActivity.EXTRA_CHAT_ID, chatId);
        intent.putExtra(MessagesActivity.EXTRA_CHAT_TITLE, chatTitle);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    // ======================================================
    /* Create a new chat for the selected friend */
    private static void startNewChat(final Context context, final String uid, final String name) {
        Log.d(TAG, "startNewChat:uid=" + uid);

        final DatabaseReference refCurrentUser = FirebaseUtil.getCurrentUserRef();
        if (refCurrentUser == null) { // error out
            Log.e(TAG, "current user ref unexpectedly null; goToLogin()");
            (new BaseActivity()).goToLogin("current user ref: null");
            return;
        }

        // get current user info
        refCurrentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getCurrentUser:onDataChange:" + dataSnapshot.getKey());

                if (!dataSnapshot.exists()) {
                    Log.e(TAG, "unexpected non-existing user; goToLogin()");
                    (new BaseActivity()).goToLogin("current user: null");
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

                // add this chat to both user
                String newChatId = newChatRef.getKey();
                refCurrentUser.child("chats").child(newChatId).setValue(true);
                FirebaseUtil.getUsersRef().child(uid).child("chats").child(newChatId).setValue(true);

                // now go to the new chat
                goToChat(context, newChatId, name);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getCurrentUser:onCancelled", databaseError.toException());
            }
        });
    }
}
