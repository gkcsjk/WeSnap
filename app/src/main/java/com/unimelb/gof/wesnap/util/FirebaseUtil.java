package com.unimelb.gof.wesnap.util;

import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by qideng on 18/09/2016.
 */
public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";

    // TODO check network???

    public static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    // =============================================
    /* Current user */

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("users").child(getCurrentUserId());
        }
        return null;
    }

    public static DatabaseReference getCurrentUserUsername() {
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef != null) {
            return currentUserRef.child("username");
        }
        return null;
    }

    /* List of Chat-IDs of the current user */
    public static DatabaseReference getCurrentChatsRef() {
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef != null) {
            return currentUserRef.child("chats");
        }
        return null;
    }

    /* List of User-IDs of the current user's friends */
    public static DatabaseReference getCurrentFriendsRef() {
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef != null) {
            return currentUserRef.child("friends");
        }
        return null;
    }

    /* List of pending requests for the current user */
    public static DatabaseReference getCurrentRequestsRef() {
        if (getCurrentUserId() != null) {
            return getRequestsRef().child(getCurrentUserId());
        }
        return null;
    }

    // =============================================
    /* Users */

    public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
    }

    public static Query getUser(String uid) {
        return getUsersRef().orderByKey().equalTo(uid);
    }

    public static String getUsersPath() {
        return "users/";
    }

    // =============================================
    /* Usernames */

    public static DatabaseReference getUsernamesRef() {
        return getBaseRef().child("usernames");
    }

    public static String getUsernamesPath() {
        return "usernames/";
    }

    // =============================================
    /* Friend Requests */

    public static DatabaseReference getRequestsRef() {
        return getBaseRef().child("friendRequests");
    }

    public static String getRequestsPath() {
        return "friendRequests/";
    }

    // =============================================
    /* Chats */

    public static DatabaseReference getChatsRef() {
        return getBaseRef().child("chats");
    }

    public static String getChatsPath() {
        return "chats/";
    }

    // =============================================
    /* Messages */

    public static DatabaseReference getMessagesRef() {
        return getBaseRef().child("messages");
    }

    public static String getMessagesPath() {
        return "messages/";
    }
}
