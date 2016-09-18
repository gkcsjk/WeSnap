package com.unimelb.wesnap.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Person {
    private String username;    // TODO
    private String displayName; // TODO

    private String email;
    private String profilePhoto;

    private Map<String, Object> chats;
    private Map<String, Object> friends;
    private Map<String, Object> posts; // TODO

    public Person() {
        // Default constructor required for calls to DataSnapshot.getValue(Person.class)
    }

    public Person(String email, String profilePhoto) {
        this.email = email;
        this.profilePhoto = profilePhoto;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public String getEmail() {
        return email;
    }
}
