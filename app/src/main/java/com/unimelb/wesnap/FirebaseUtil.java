package com.unimelb.wesnap;

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
            return getBaseRef().child("people").child(getCurrentUserId());
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
    public static DatabaseReference getPeopleRef() {
        return getBaseRef().child("people");
    }

    public static String getPeoplePath() {
        return "people/";
    }

    public static DatabaseReference getFollowersRef() {
        return getBaseRef().child("followers");
    }

    public static String getFollowersPath() {
        return "followers/";
    }

    // =============================================
    public static DatabaseReference getPostsRef() {
        return getBaseRef().child("posts");
    }

    public static String getPostsPath() {
        return "posts/";
    }

    public static DatabaseReference getCommentsRef() {
        return getBaseRef().child("comments");
    }

}
