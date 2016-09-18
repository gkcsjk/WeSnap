package com.unimelb.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Post {
    private static final int TIME_TO_LIVE = 24; //24 hours? TODO

    private String uid;
    private String author;
    private String text;
    private Object timestamp;

    private String full_url;
    private String full_storage_uri;

    private ArrayList<Comment> comments; // TODO: 18/09/2016

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String text) {
        this.uid = uid;
        this.author = author;
        this.text = text;

        // TODO: current timestamp
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("text", text);

        return result;
    }
}
