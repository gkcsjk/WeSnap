package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Message {
    // sender data
    private String senderUid;
    private String senderDisplayedName;

    // messageBody: message text or photoUrl
    private String messageBody;

    // isPhoto: false for text messages
    private boolean isPhoto;

    // photoTimeToLive:
    // positive int for photo taken in app (timeout rule);
    // -1 for photo uploaded from local or text
    private int photoTimeToLive;

    // for photo viewing rules: view -> replay -> not accessible
    private boolean photoIsViewed;

    public Message() {
    }

    // without photoTimeToLive
    public Message(String senderUid, String senderDisplayedName,
                   String messageBody, boolean isPhoto) {
        this(senderUid, senderDisplayedName, messageBody, isPhoto, -1);
    }

    // with photoTimeToLive
    public Message(String senderUid, String senderDisplayedName,
                   String messageBody, boolean isPhoto, int photoTimeToLive) {
        this.senderUid = senderUid;
        this.senderDisplayedName = senderDisplayedName;

        this.messageBody = messageBody;
        this.isPhoto = isPhoto;
        this.photoTimeToLive = photoTimeToLive;

        this.photoIsViewed = false;
    }

    // ======================================================

    public String getSenderUid() {
        return senderUid;
    }

    public String getSenderDisplayedName() {
        return senderDisplayedName;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public boolean isPhoto() {
        return isPhoto;
    }

    public boolean isPhotoIsViewed() {
        return photoIsViewed;
    }

    public int getPhotoTimeToLive() {
        return photoTimeToLive;
    }

    // ======================================================
    @Exclude
    public Map<String, Object> toMap() {
        // used "@Exclude" to mark a field as excluded from the Database
        HashMap<String, Object> result = new HashMap<>();
        result.put("senderUid", this.senderUid);
        result.put("senderDisplayedName", this.senderDisplayedName);
        result.put("messageBody", this.messageBody);
        result.put("isPhoto", this.isPhoto);
        result.put("photoIsViewed", this.photoIsViewed);
        result.put("photoTimeToLive", this.photoTimeToLive);
        return result;
    }
}
