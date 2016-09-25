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

    public static DatabaseReference getCurrentChatsRef() {
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef != null) {
            return currentUserRef.child("chats");
        }
        return null;
    }

    // =============================================
    public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
    }

    public static String getUsersPath() {
        return "users/";
    }

    // =============================================
    public static DatabaseReference getUsernamesRef() {
        return getBaseRef().child("usernames");
    }

    public static String getUsernamesPath() {
        return "usernames/";
    }

}
