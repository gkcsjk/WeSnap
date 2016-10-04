package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Message class for Firebase Database
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
@IgnoreExtraProperties
public class Message {
    // sender data
    private String senderUid;
    private String senderDisplayedName;

    // messageBody: message text or photoUrl
    private String messageBody;

    // photo: false for text messages
    private boolean photo;

    // timeToLive:
    // positive int for photo taken in app (timeout rule);
    // -1 for photo uploaded from local or text
    private int timeToLive;

    // for photo viewing rules: view -> replay -> not accessible
    private boolean isViewed;

    public Message() {
    }

    // without timeToLive
    public Message(String senderUid, String senderDisplayedName,
                   String messageBody, boolean photo) {
        this(senderUid, senderDisplayedName, messageBody, photo, -1);
    }

    // with timeToLive
    public Message(String senderUid, String senderDisplayedName,
                   String messageBody, boolean photo, int timeToLive) {
        this.senderUid = senderUid;
        this.senderDisplayedName = senderDisplayedName;

        this.messageBody = messageBody;
        this.photo = photo;
        this.timeToLive = timeToLive;

        this.isViewed = false;
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
        return photo;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    // ======================================================
    @Exclude
    public Map<String, Object> toMap() {
        // used "@Exclude" to mark a field as excluded from the Database
        HashMap<String, Object> result = new HashMap<>();
        result.put("senderUid", this.senderUid);
        result.put("senderDisplayedName", this.senderDisplayedName);
        result.put("messageBody", this.messageBody);
        result.put("photo", this.photo);
        result.put("isViewed", this.isViewed);
        result.put("timeToLive", this.timeToLive);
        return result;
    }
}
