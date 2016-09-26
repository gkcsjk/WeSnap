package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Chat {

    private String chatId;
    private String receiverName; // TODO: messageImage???
    private String receiverPhotoUrl;

    public Chat() {
    }

    public Chat(String receiverName, String receiverPhotoUrl) {
        this.receiverName = receiverName;
        this.receiverPhotoUrl = receiverPhotoUrl;
    }

    public String getId() {
        return chatId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhotoUrl() {
        return receiverPhotoUrl;
    }

    public void setReceiverPhotoUrl(String receiverPhotoUrl) {
        this.receiverPhotoUrl = receiverPhotoUrl;
    }
}
