package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Chat class for Firebase Database
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
@IgnoreExtraProperties
public class Chat {
    @Exclude
    public static final String ADDED_AS_FRIEND = "Added as friends ";
    @Exclude
    private static final int MILLISECONDS_IN_ONE_HOUR = 60 * 60 * 1000;

    private Map<String, String> participants = new HashMap<>();
                                                // {uid: displayedName}
    private String lastMessageBody;
    private Long chatCreatedAt;
    private String chatTitle = null; // null for now; possible group chats
    private String chatAvatarUrl = null; // if null: use default icon

    public Chat() {
    }

    public Chat(Map<String, String> participants, String lastMessageBody) {
        this(participants, lastMessageBody, null, null);
    }

    public Chat(Map<String, String> participants, String lastMessageBody,
                String chatAvatarUrl, String chatTitle) {
        this.participants.putAll(participants);
        this.lastMessageBody = lastMessageBody;
        this.chatCreatedAt = System.currentTimeMillis();

        if (chatTitle != null) {
            this.chatTitle = chatTitle;
        }
        if (chatAvatarUrl != null) {
            this.chatAvatarUrl = chatAvatarUrl;
        }
    }

    // ======================================================

    public Map<String, String> getParticipants() {
        return participants;
    }

    public String getLastMessageBody() {
        return lastMessageBody;
    }

    public long getChatCreatedAt() {
        return this.chatCreatedAt;
    }

    @Exclude
    public String getAddFriendMessage() {
        if (this.chatCreatedAt != null && this.lastMessageBody != null &&
                this.lastMessageBody.equals(ADDED_AS_FRIEND)) {
            int hours = (int) (System.currentTimeMillis() - this.chatCreatedAt)
                    / MILLISECONDS_IN_ONE_HOUR;
            if (hours >= 0) {
                switch (hours) {
                    case 0:
                        return (ADDED_AS_FRIEND + "just now");
                    case 1:
                        return (ADDED_AS_FRIEND + "1 hour ago");
                    default:
                        return (ADDED_AS_FRIEND + hours + " hours ago");
                }
            }
        }
        return null;
    }

    public String getChatTitle() {
        return chatTitle;
    }

    public String getChatAvatarUrl() {
        return chatAvatarUrl;
    }

    // ======================================================
    @Exclude
    public Map<String, Object> toMap() {
        // used "@Exclude" to mark a field as excluded from the Database
        HashMap<String, Object> result = new HashMap<>();
        result.put("chatTitle", this.chatTitle);
        result.put("chatAvatarUrl", this.chatAvatarUrl);
        result.put("lastMessageBody", this.lastMessageBody);
        result.put("participants", this.participants);
        return result;
    }
}
