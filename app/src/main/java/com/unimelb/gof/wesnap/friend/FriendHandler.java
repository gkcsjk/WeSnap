package com.unimelb.gof.wesnap.friend;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.chat.ChatStarter;
import com.unimelb.gof.wesnap.models.Chat;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;

import java.util.Map;

/**
 * FriendHandler
 * This class contains methods for sending/accepting friend requests.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class FriendHandler {
    private static final String TAG = "FriendHandler";
    private static final String SENT = "Friend request sent";
    private static final String NOT_SENT = "Failed to send the request. Try again.";
    private static final String ACCEPTED = "Friend request accepted";

    // ========================================================
    /* sendFriendRequest()
     * send request to Firebase Database and update UI accordingly */
    public static void sendFriendRequest(final String toUserId,
                                         final FriendItemViewHolder viewHolder,
                                         final View v) {
        Log.d(TAG, "sendFriendRequest");

        final String fromUserId = FirebaseUtil.getMyUid();
        if (fromUserId == null) { // null value; error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            BaseActivity error = new BaseActivity();
            error.goToLogin("current user uid: null");
            return;
        }

        if (AppParams.currentUser != null) {
            // get current user info
            Map<String, Object> requestValues = AppParams.currentUser.toFriendRequest();
            // add request to destination user
            FirebaseUtil.getRequestsRef()
                    .child(toUserId).child(fromUserId).setValue(requestValues);
            // update UI
            Snackbar.make(v, SENT, Snackbar.LENGTH_LONG).show();
            viewHolder.useDoneButton();
        } else {
            // go to Firebase Database
            FirebaseUtil.getMyUserRef()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.w(TAG, "getCurrentUser:onDataChange");
                            // fetch current user info
                            User currentUser = dataSnapshot.getValue(User.class);
                            AppParams.currentUser = currentUser;
                            Map<String, Object> requestValues = currentUser.toFriendRequest();
                            // add request to destination user
                            FirebaseUtil.getRequestsRef()
                                    .child(toUserId).child(fromUserId).setValue(requestValues);
                            // update UI
                            Snackbar.make(v, SENT, Snackbar.LENGTH_LONG).show();
                            viewHolder.useDoneButton();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                            // update UI
                            Snackbar.make(v, NOT_SENT, Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }

    // ========================================================
    public static void acceptFriendRequest(final DatabaseReference refRequest,
                                           final FriendItemViewHolder viewHolder,
                                           final View v) {
        Log.w(TAG, "acceptFriendRequest");

        final String fromUserId = refRequest.getKey();
        final String toUserId = FirebaseUtil.getMyUid();
        if (toUserId == null) { // null value; error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            BaseActivity error = new BaseActivity();
            error.goToLogin("null value");
            return;
        }

        // update Firebase Database
        insertFriendAtoB(fromUserId, toUserId);
        insertFriendAtoB(toUserId, fromUserId);
        refRequest.removeValue();

        // start a new chat
        ChatStarter.startNewChat(v.getContext(),
                fromUserId, viewHolder.nameView.getText().toString(),
                Chat.ADDED_AS_FRIEND);

        // update UI
        Snackbar.make(v, ACCEPTED, Snackbar.LENGTH_LONG).show();
        viewHolder.useDoneButton();
    }

    // ========================================================
    /* Insert "someFriendId" to "toUserId"'s friend list */
    public static void insertFriendAtoB(final String someFriendId,
                                        final String toUserId) {
        Log.d(TAG, "insertFriend:" + someFriendId + " to " + toUserId);

        if (someFriendId != null && toUserId != null) {
            FirebaseUtil.getUsersRef().child(toUserId).child("friends")
                    .child(someFriendId).setValue(true);
        }
        // TODO null?
    }

    // ========================================================
    /* Remove "someFriendId" to "toUserId"'s friend list */
    public static void removeFriendAfromB(final String someFriendId,
                                          final String fromUserId) {
        Log.d(TAG, "insertFriend:" + someFriendId + " to " + fromUserId);
        if (someFriendId != null && fromUserId != null) {
            FirebaseUtil.getUsersRef().child(fromUserId).child("friends")
                    .child(someFriendId).setValue(true);
            return;
        }
        // TODO null?
    }
}
