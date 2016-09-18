package com.unimelb.wesnap.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Person {
    public String uid;
//    private String username;
    public String email;
    public String profilePhoto;

    private Map<String, Boolean> posts; // TODO: 18/09/2016
    private Map<String, Object> friends; // TODO: 18/09/2016
    private Map<String, Object> friend_requests; // TODO: 18/09/2016

    public Person() {
        // Default constructor required for calls to DataSnapshot.getValue(Person.class)
    }

    public Person(String uid, String username, String email, String profilePhoto) {
        this.uid = uid;
//        this.username = username;
        this.email = email;
        this.profilePhoto = profilePhoto;
    }


}
