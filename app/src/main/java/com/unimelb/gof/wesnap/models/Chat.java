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

    private String chatTitle = "Another Chat"; // TODO
    private String lastMessageBody;
    private List<String> participants = new ArrayList<String>();

    public Chat() {
    }

    public Chat(String[] participantsIds) {
        int i;
        for (i = 0; i < participantsIds.length; i++) {
            this.participants.add(participantsIds[i]);
        }

        this.lastMessageBody = null;
    }

    public Chat(String[] participantsIds, String lastMessageBody) {
        int i;
        for (i = 0; i < participantsIds.length; i++) {
            this.participants.add(participantsIds[i]);
        }

        this.lastMessageBody = lastMessageBody;
    }

    public String getChatTitle() {
        return chatTitle;
    }

    public String getLastMessageBody() {
        return lastMessageBody;
    }

    public List<String> getParticipants() {
        return participants;
    }
}
