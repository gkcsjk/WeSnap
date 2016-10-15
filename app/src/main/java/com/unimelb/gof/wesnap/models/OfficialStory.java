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
    public String summary;

    public OfficialStory() {
        // Default constructor required for calls to DataSnapshot.getValue(OfficialStory.class)
    }

    public OfficialStory(String source, String keyword, String title,
                         String webpageUrl, String photoUrl, String publicationDate,
                         String summary) {
        this.source = source;
        this.keyword = keyword;
        this.title = title;
        this.webpageUrl = webpageUrl;
        this.photoUrl = photoUrl;
        this.publicationDate = publicationDate;
        this.summary = summary;
    }

    public OfficialStory(String[] stringArray) {
        this.source = stringArray[0];
        this.keyword = stringArray[1];
        this.title = stringArray[2];
        this.webpageUrl = stringArray[3];
        this.photoUrl = stringArray[4];
        this.publicationDate = stringArray[5];
        this.summary = stringArray[6];
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
        result.put("summary", summary);
        return result;
    }

    @Exclude
    public String[] toStringArray() {
        return (new String[]{
                source, keyword, title, webpageUrl, photoUrl, publicationDate, summary
        });
    }

    // ======================================================
}
