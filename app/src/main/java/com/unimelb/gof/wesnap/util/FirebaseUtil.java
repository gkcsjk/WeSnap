package com.unimelb.gof.wesnap.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unimelb.gof.wesnap.BaseActivity;

/**
 * FirebaseUtil
 * Returns the relevant Firebase Database/Storage References.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";

    public static final String[] NOT_ALLOWED_CHAR = new String[]{
            ".","$","#","[","]","/"};

    // =============================================

    private static void handleNullValue(String field) {
        String message = field + ":unexpected null value";
        Log.e(TAG, message +":goToLogin");
        (new BaseActivity()).goToLogin(message);
    }

    // =============================================
    /* Root */

    @NonNull
    public static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    public static StorageReference getBaseStorage() {
        return FirebaseStorage.getInstance()
                .getReferenceFromUrl("gs://gof-wesnap.appspot.com/");
    }

    // =============================================
    // =============================================
    // =============================================
    /* Users */
    @NonNull
    public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
    }

    /* Usernames */
    @NonNull
    public static DatabaseReference getUsernamesRef() {
        return getBaseRef().child("usernames");
    }

    /* Friend Requests */
    @NonNull
    public static DatabaseReference getRequestsRef() {
        return getBaseRef().child("friendRequests");
    }

    /* Chats */
    @NonNull
    public static DatabaseReference getChatsRef() {
        return getBaseRef().child("chats");
    }

    @NonNull
    public static StorageReference getChatsStorage() {
        return getBaseStorage().child("chats");
    }

    /* Messages */
    public static DatabaseReference getMessagesRef() {
        return getBaseRef().child("messages");
    }

    /* ----- Current User  ----- */

    // current user's Firebase UID (from Firebase Auth)
    // if null: return null
    @Nullable
    public static String getMyUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    // current user's Firebase UID (from Firebase Auth)
    // if null: error out; direct user to Login
    @Nullable
    public static String getMyUidNonNull() {
        String uid = getMyUid();
        if (uid == null) {
            handleNullValue("getMyUid");
            return null;
        }
        return uid; // non-null uid
    }

    // current user's root reference
    @Nullable
    public static DatabaseReference getMyUserRef() {
        String uid = getMyUidNonNull();
        if (uid == null) {
            return null;
        }
        return getUsersRef().child(uid); // "users/my-uid"
    }

    // current user's List of Chat-IDs
    @Nullable
    public static DatabaseReference getMyChatIdsRef() {
        DatabaseReference currentUserRef = getMyUserRef();
        if (currentUserRef == null) {
            return null;
        }
        return currentUserRef.child("chats"); // "users/my-uid/chats"
    }

    // current user's List of Friend-UIDs
    @Nullable
    public static DatabaseReference getMyFriendIdsRef() {
        DatabaseReference currentUserRef = getMyUserRef();
        if (currentUserRef == null) {
            return null;
        }
        return currentUserRef.child("friends"); // "users/my-uid/friends"
    }

    // current user's List of Pending Requests
    @Nullable
    public static DatabaseReference getMyFriendRequestsRef() {
        String uid = getMyUidNonNull();
        if (uid == null) {
            return null;
        }
        return getRequestsRef().child(uid);
    }

    // =============================================
    // =============================================
    // =============================================

    /* Memories (Only Current User) */

    @Nullable
    public static DatabaseReference getMyMemoriesDatabase() {
        String uid = getMyUidNonNull();
        if (uid == null) {
            return null;
        }
        return getBaseRef().child("memories").child(uid);
    }

    @Nullable
    public static StorageReference getMyMemoriesStorage() {
        String uid = getMyUidNonNull();
        if (uid == null) {
            return null;
        }
        return getBaseStorage().child("memories").child(uid);
    }

    // =============================================
    // =============================================
    // =============================================

    /* My Stories / Friends Stories */

    // all the stories
    @NonNull
    public static StorageReference getStoriesStorage() {
        return getBaseStorage().child("stories");
    }

    // all the stories
    @NonNull
    public static DatabaseReference getStoriesDatabase() {
        return getBaseRef().child("stories");
    }

    // root ref to self stories
    @NonNull
    public static DatabaseReference getSelfStoriesDatabase() {
        return getBaseRef().child("storiesByMe");
    }

    // root ref to friends stories
    @NonNull
    public static DatabaseReference getFriendsStoriesDatabase() {
        return getBaseRef().child("storiesByFriends");
    }

    /* ----- Current User  ----- */

    // List of Ids: stories by me (current user)
    @Nullable
    public static DatabaseReference getMyStoryIdsRef() {
        String uid = getMyUidNonNull();
        if (uid == null) {
            return null;
        }
        return getSelfStoriesDatabase().child(uid);
    }

    // List of Ids: stories posted by friends & visible by me (current user)
    @Nullable
    public static DatabaseReference getFriendsStoryIdsRef() {
        String uid = getMyUidNonNull();
        if (uid == null) {
            return null;
        }
        return getFriendsStoriesDatabase().child(uid);
    }

    // =============================================
    // =============================================
    // =============================================

    /* Official Stories */

    @NonNull
    public static DatabaseReference getOfficialStoriesDatabase() {
        return getBaseRef().child("officialStories");
    }

    @NonNull
    public static DatabaseReference getKeywordsDatabase() {
        return getBaseRef().child("keywords");
    }

    @NonNull
    public static DatabaseReference getLastImportTimeRef() {
        return getBaseRef().child("lastImportTime");
    }

    /* ----- Current User  ----- */

    // List of Keyword Strings: subscribed by me (current user)
    @Nullable
    public static DatabaseReference getMySubscriptionKeywordsRef() {
        DatabaseReference currentUserRef = getMyUserRef();
        if (currentUserRef == null) {
            return null;
        }
        return currentUserRef.child("subscription");
    }

    // List of Keyword Strings: clicked by me (current user)
    @Nullable
    public static DatabaseReference getMyInterestKeywordsRef() {
        DatabaseReference currentUserRef = getMyUserRef();
        if (currentUserRef == null) {
            return null;
        }
        return currentUserRef.child("interests");
    }

    // =============================================

    public static DatabaseReference getDevTeamRef() {
        return getUsersRef().child(AppParams.ID_DEV_TEAM);
    }
}
