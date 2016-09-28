package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO comments
 */
@IgnoreExtraProperties
public class User {
    private String uid;
    private String email;
    private String username;
    private String displayedName;
    private String avatarUrl; // TODO user-supplied avatar?
    private List<String> friends; // "uid: true"
    private List<String> chats; // "chat-id: true"

    // private Map<String, Object> stories; //TODO
    // private Map<String, Object> memories; //TODO

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String username, String displayName, String email) {
        this(uid, username, displayName, email, null, null);
    }

    public User(String uid, String username, String displayName, String email,
                String initialFriendId, String initialChatId) {
        this.uid = uid;
        this.username = username;
        this.displayedName = displayName;
        this.email = email;

        this.avatarUrl = null; // "null" for a newly registered user

        this.friends = new ArrayList<>();
        if (initialFriendId != null) {
            this.friends.add(initialFriendId);
        }

        this.chats = new ArrayList<>();
        if (initialChatId != null) {
            this.chats.add(initialChatId);
        }
    }

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

    public List<String> getFriends() {
        return this.friends;
    }

    public List<String> getChats() {
        return this.chats;
    }

    @Exclude
    public Map<String, Object> toChatParticipant() {
        // used "@Exclude" to mark a field as excluded from the Database
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", this.uid);
        result.put("email", this.email);
        result.put("displayedName", this.displayedName);
        result.put("avatarUrl", this.avatarUrl);
        return result;
    }

    @Exclude
    public Map<String, Object> toFriendRequest() {
        return toChatParticipant();
    }
}
