package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chat class for Firebase Database
 */
@IgnoreExtraProperties
public class Chat {
    private Map<String, String> participants = new HashMap<>(); // {uid: displayedName}
    private String lastMessageBody;

    private String chatTitle = null; // TODO for group chats; use null for now
    private String chatAvatarUrl = null; // TODO use default icon for now

    public Chat() {
    }

    public Chat(Map<String, String> participants, String lastMessageBody) {
        this(participants, lastMessageBody, null, null);
    }

    public Chat(Map<String, String> participants, String lastMessageBody,
                String chatAvatarUrl, String chatTitle) {
        this.participants.putAll(participants);
        this.lastMessageBody = lastMessageBody;
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
