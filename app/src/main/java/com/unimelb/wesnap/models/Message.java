package com.unimelb.wesnap.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Message {

    private String messageId;
    private String messageText; // TODO: messageImage???
    private String senderName;

    public Message() {
    }

    public Message(String messageText, String senderName) {
        this.messageText = messageText;
        this.senderName = senderName;
    }

    public String getId() {
        return messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
