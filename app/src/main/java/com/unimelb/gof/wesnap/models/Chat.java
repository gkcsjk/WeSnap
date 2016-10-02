package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Chat {
    private String chatTitle = "Just Another Chat"; // TODO
    private String chatAvatarUrl = null;

    private String lastMessageBody;
    private Map<String, Boolean> participants = new HashMap<>();

    public Chat() {
    }

    public Chat(String[] participantsIds, String lastMessageBody,
                String chatAvatarUrl, String chatTitle) {
        int i;
        for (i = 0; i < participantsIds.length; i++) {
            this.participants.put(participantsIds[i], true);
        }

        this.lastMessageBody = lastMessageBody;
        //this.chatAvatarUrl = chatAvatarUrl; TODO use default icon for now
        if (chatTitle != null) {
            this.chatTitle = chatTitle;
        }
    }

    public String getChatTitle() {
        return chatTitle;
    }

    public String getChatAvatarUrl() {
        return chatAvatarUrl;
    }

    public String getLastMessageBody() {
        return lastMessageBody;
    }

    public Map<String, Boolean> getParticipants() {
        return participants;
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
