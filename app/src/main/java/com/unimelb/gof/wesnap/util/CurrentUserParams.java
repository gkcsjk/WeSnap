package com.unimelb.gof.wesnap.util;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.models.User;

import java.util.HashMap;

/**
 * Save a local copy of current user info.
 * Aim to reduce the use of single value listener.
 * Will be started first after register or login.
 * Will be reset when logout or exit app.
 * TODO
 */
public class CurrentUserParams {
    private static final String TAG = "CurrentUserParams";

    private static CurrentUserParams mCurrentUserParams = null;

    /* Firebase Database */
    private static DatabaseReference mCurrentUserRef;
    private static ValueEventListener mCurrentUserListener;
    private static DatabaseReference mFriendsRef;
    private static ValueEventListener mFriendsListener;
    private static DatabaseReference mChatsRef;
    private static ValueEventListener mChatsListener;

    /* User attributes */
    private static String email;
    private static String username;
    private static String displayedName;
    private static String avatarUrl;
    private static HashMap<String, Boolean> friendIds;
    private static HashMap<String, Boolean> chatIds;

    private CurrentUserParams() {
        // create listeners
        mCurrentUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getCurrentUser:onDataChange");
                User user = dataSnapshot.getValue(User.class);
                email = user.getEmail();
                username = user.getUsername();
                displayedName = user.getDisplayedName();
                avatarUrl = user.getAvatarUrl();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "getCurrentUser:onCancelled");
                // TODO
            }
        };

        mFriendsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getCurrentFriends:onDataChange");
                friendIds = (HashMap<String, Boolean>) dataSnapshot.getValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "getCurrentFriends:onCancelled");
                // TODO
            }
        };

        mChatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getCurrentChats:onDataChange");
                chatIds = (HashMap<String, Boolean>) dataSnapshot.getValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "getCurrentChats:onCancelled");
                // TODO
            }
        };

        // set up database ref
        mCurrentUserRef = FirebaseUtil.getCurrentUserRef();
        mFriendsRef = FirebaseUtil.getCurrentFriendsRef();
        mChatsRef = FirebaseUtil.getCurrentChatsRef();

        // get user data for the first time
        mCurrentUserRef.addValueEventListener(mCurrentUserListener);
        mFriendsRef.addValueEventListener(mFriendsListener);
        mChatsRef.addValueEventListener(mChatsListener);
    }

    public static CurrentUserParams getInstance() {
        if (mCurrentUserParams == null) {
            mCurrentUserParams = new CurrentUserParams();
        }
        return mCurrentUserParams;
    }

    public static void resetCurrentUserParams() {
        mCurrentUserParams = null;
        if (mCurrentUserListener != null) {
            mCurrentUserRef.removeEventListener(mCurrentUserListener);
        }
        if (mFriendsListener != null) {
            mFriendsRef.removeEventListener(mFriendsListener);
        }
        if (mChatsListener != null) {
            mChatsRef.removeEventListener(mChatsListener);
        }
        return;
    }

    public static String getEmail() {
        return email;
    }

    public static String getUsername() {
        return username;
    }

    public static String getDisplayedName() {
        return displayedName;
    }

    public static String getAvatarUrl() {
        return avatarUrl;
    }

    public static HashMap<String, Boolean> getFriendIds() {
        return friendIds;
    }

    public static HashMap<String, Boolean> getChatIds() {
        return chatIds;
    }

    /* Construct a User instance using the current user attributes */
    public static User getTmpUserInstance() {
        String uid = FirebaseUtil.getCurrentUserId();
        if (uid != null) {
            User tmpUser = new User(uid, username, displayedName, email, null, null);
            tmpUser.setAvatarUrl(avatarUrl);
            tmpUser.getFriends().putAll(friendIds);
            tmpUser.getChats().putAll(chatIds);
            return tmpUser;
        }
        return null;
    }
}
