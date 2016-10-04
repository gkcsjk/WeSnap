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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unimelb.gof.wesnap.BaseActivity;

/**
 * Created by qideng on 18/09/2016.
 */
public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";

    public static final String[] NOT_ALLOWED_CHAR = new String[]{".","$","#","[","]","/"};

    // TODO check network???

    public static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static StorageReference getBaseStorage() {
        return FirebaseStorage.getInstance().getReferenceFromUrl("gs://gof-wesnap.appspot.com/");
    }

    // =============================================
    /* Current user */

    // current user's  uid
    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    // current user's ref
    public static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getUsersRef().child(uid);
        } else {
            // null value, error out
            handleNullValue("getCurrentUserId");
            return null;
        }
    }

    // current user's List of Chat-IDs
    public static DatabaseReference getCurrentChatsRef() {
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef != null) {
            return currentUserRef.child("chats");
        }
        return null;
    }

    // current user's List of Friends' uid
    public static DatabaseReference getCurrentFriendsRef() {
        DatabaseReference currentUserRef = getCurrentUserRef();
        if (currentUserRef != null) {
            return currentUserRef.child("friends");
        }
        return null;
    }

    // current user's List of Pending Requests
    public static DatabaseReference getCurrentRequestsRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getRequestsRef().child(uid);
        } else {
            // null value, error out
            handleNullValue("getCurrentUserId");
            return null;
        }
    }

    // =============================================
    /* Users */
    public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
    }

    // =============================================
    /* Usernames */
    public static DatabaseReference getUsernamesRef() {
        return getBaseRef().child("usernames");
    }

    // =============================================
    /* Friend Requests */
    public static DatabaseReference getRequestsRef() {
        return getBaseRef().child("friendRequests");
    }

    // =============================================
    /* Chats */

    public static DatabaseReference getChatsRef() {
        return getBaseRef().child("chats");
    }

    // =============================================
    /* Messages */
    public static DatabaseReference getMessagesRef() {
        return getBaseRef().child("messages");
    }

    // =============================================

    private static void handleNullValue(String field) {
        Log.e(TAG, field + " unexpectedly null; goToLogin()");
        BaseActivity a = new BaseActivity();
        a.goToLogin("null value");
    }

    // =============================================
    /* Memories */
    public static DatabaseReference getCurrentMemoriesDatabase() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("memories").child(uid);
        } else {
            // null value, error out
            handleNullValue("getCurrentUserId");
            return null;
        }
    }

    public static StorageReference getCurrentMemoriesStorage() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseStorage().child("memories").child(uid);
        } else {
            // null value, error out
            handleNullValue("getCurrentUserId");
            return null;
        }
    }

    public static DatabaseReference getMemoriesDatabase() {
        return getBaseRef().child("memories");
    }

    public static StorageReference getMemoriesStorage() {
        return getBaseStorage().child("memories");
    }

    // =============================================
}
