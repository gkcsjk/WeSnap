package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Story {
    @Exclude
    public static final int HOURS_TO_LIVE = 24; // hours
    @Exclude
    public static final int MILLISECONDS_IN_ONE_HOUR = 60 * 60 * 1000;

    private String authorUid;
    private String authorName;
    private String photoUrl;
    private Long timestamp;

    public Story() {
        // Default constructor required for calls to DataSnapshot.getValue(Story.class)
    }

    public Story(String authorUid, String authorName,
                 String photoUrl) {
        this.authorUid = authorUid;
        this.authorName = authorName;
        this.photoUrl = photoUrl;
        this.timestamp = System.currentTimeMillis();
    }

    // ======================================================

    public String getAuthorUid() {
        return this.authorUid;
    }

    public String getAuthorName() {
        return this.authorName;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    @Exclude
    public long getDiffHours() {
        return (System.currentTimeMillis() - this.timestamp) / MILLISECONDS_IN_ONE_HOUR;
    }

    @Exclude
    public boolean isExpired() {
        return (getDiffHours() >= HOURS_TO_LIVE);
    }

    // ======================================================
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("authorUid", authorUid);
        result.put("authorName", authorName);
        result.put("photoUrl", photoUrl);
        result.put("timestamp", timestamp);
        return result;
    }

}
