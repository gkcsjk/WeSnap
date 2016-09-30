package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Chat {
    // private String chatId;

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

    public void setChatTitle(String chatTitle) {
        this.chatTitle = chatTitle;
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
}
