package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User class for Firebase Database
 */
@IgnoreExtraProperties
public class User {
    private String uid;
    private String email;
    private String username;
    private String displayedName;
    private String avatarUrl; // TODO user-supplied avatar? snapcode?

    private Map<String, Boolean> friends; // "uid: true"
    private Map<String, Boolean> chats; // "chat-id: true"

    // private Map<String, Object> stories; //TODO

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String username, String displayedName, String email) {
        this(uid, username, displayedName, email, null, null);
    }

    public User(String uid, String username, String displayName, String email,
                String initialFriendId, String initialChatId) {
        this.uid = uid;
        this.username = username;
        this.displayedName = displayName;
        this.email = email;

        this.avatarUrl = null; // "null" for a newly registered user

        this.friends = new HashMap<>();
        if (initialFriendId != null) {
            this.friends.put(initialFriendId, true);
        }

        this.chats = new HashMap<>();
        if (initialChatId != null) {
            this.chats.put(initialChatId, true);
        }
    }

    // ======================================================

    public String getUid() {
        return uid;
    }
    public String getEmail() {
        return email;
    }
    public String getUsername() {
        return username;
    }
    public String getDisplayedName() {
        return displayedName;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Map<String, Boolean> getFriends() {
        return this.friends;
    }

    public Map<String, Boolean> getChats() {
        return this.chats;
    }

    // ======================================================
    @Exclude
    public Map<String, String> toChatParticipant() {
        // used "@Exclude" to mark a field as excluded from the Database
        HashMap<String, String> result = new HashMap<>();
        result.put(this.uid, this.displayedName);
        return result;
    }

    // ======================================================
    @Exclude
    public Map<String, Object> toFriendRequest() {
        // used "@Exclude" to mark a field as excluded from the Database
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", this.uid);
        result.put("email", this.email);
        result.put("displayedName", this.displayedName);
        result.put("avatarUrl", this.avatarUrl);
        return result;
    }
}
