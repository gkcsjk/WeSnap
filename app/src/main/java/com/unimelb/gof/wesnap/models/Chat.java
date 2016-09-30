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
    private List<String> participants = new ArrayList<String>();

    public Chat() {
    }

    public Chat(String[] participantsIds, String lastMessageBody,
                String chatAvatarUrl, String chatTitle) {
        int i;
        for (i = 0; i < participantsIds.length; i++) {
            this.participants.add(participantsIds[i]);
        }

        this.lastMessageBody = lastMessageBody;
        this.chatAvatarUrl = chatAvatarUrl;
        this.chatTitle = chatTitle;
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

    public List<String> getParticipants() {
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
