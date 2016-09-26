package com.unimelb.gof.wesnap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by qideng on 18/09/2016.
 */
public class FirebaseUtil {

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

    // =============================================
    /* Users */

    public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
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

}
