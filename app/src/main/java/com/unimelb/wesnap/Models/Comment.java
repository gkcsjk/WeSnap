package com.unimelb.wesnap.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Comment {
    private String uid;
    private String author;
    private String text;

    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String uid, String author, String text) {
        this.uid = uid;
        this.author = author;
        this.text = text;
    }
}
