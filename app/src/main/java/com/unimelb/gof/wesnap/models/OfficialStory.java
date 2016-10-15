package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class OfficialStory {
    @Exclude
    public static final int HOURS_TO_LIVE = 24 * 7; // one week
    @Exclude
    public static final int MILLISECONDS_IN_ONE_HOUR = 60 * 60 * 1000;

    public String source;
    public String keyword;
    public String title;
    public String webpageUrl;
    public String photoUrl;
    public String publicationDate;

    public OfficialStory() {
        // Default constructor required for calls to DataSnapshot.getValue(OfficialStory.class)
    }

    public OfficialStory(String source, String keyword, String title,
                         String webpageUrl, String photoUrl, String publicationDate) {
        this.source = source;
        this.keyword = keyword;
        this.title = title;
        this.webpageUrl = webpageUrl;
        this.photoUrl = photoUrl;
        this.publicationDate = publicationDate;
    }

    // ======================================================
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("source", source);
        result.put("keyword", keyword);
        result.put("title", title);
        result.put("webpageUrl", webpageUrl);
        result.put("photoUrl", photoUrl);
        result.put("publicationDate", publicationDate);
        return result;
    }

    // ======================================================
}
