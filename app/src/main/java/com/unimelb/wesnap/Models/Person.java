package com.unimelb.wesnap.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Person {
    private String uid;
    private String username;
    private String email;
    private String profile_picture;

    private Map<String, Boolean> posts; // TODO: 18/09/2016
    private Map<String, Object> friends; // TODO: 18/09/2016
    private Map<String, Object> friend_requests; // TODO: 18/09/2016

    public Person() {
        // Default constructor required for calls to DataSnapshot.getValue(Person.class)
    }

    public Person(String uid, String username, String email, String profile_picture) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profile_picture = profile_picture;
    }


}
