package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * TODO comments
 */
@IgnoreExtraProperties
public class User {
    private String username;
    private String displayedName;
    private String email;
    private String avatarUrl; // "null" for a newly registered user
    // TODO user-supplied avatar?

    private Map<String, Object> chats;
    private Map<String, Object> friends;
    private Map<String, Object> posts;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String displayName, String email,
                String avatarUrl) {
        this.username = username;
        this.displayedName = displayName;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    public String getDisplayedName() {
        return displayedName;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getEmail() {
        return email;
    }
}
