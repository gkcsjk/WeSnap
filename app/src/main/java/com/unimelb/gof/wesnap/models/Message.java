package com.unimelb.gof.wesnap.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by qideng on 18/09/2016.
 */
@IgnoreExtraProperties
public class Message {
    // private String messageId;

    private String sendUid;
    private String senderDisplayedName;
    private String senderAvatarUrl;

    private String messageBody; // message text = {text, photoUrl}
    private boolean isPhoto; // message type = {plain_text, photo}
    private boolean photoIsViewed;
    private int photoTimeToLive; // positive int for photo; -1 for photo uploaded from local or text

    public Message() {
    }

    // without photoTimeToLive
    public Message(String sendUid, String senderDisplayedName, String senderAvatarUrl,
                   String messageBody, boolean isPhoto) {
        this(sendUid, senderDisplayedName, senderAvatarUrl,
                messageBody, isPhoto, -1);
    }

    // with photoTimeToLive
    public Message(String sendUid, String senderDisplayedName, String senderAvatarUrl,
                   String messageBody, boolean isPhoto, int photoTimeToLive) {
        this.sendUid = sendUid;
        this.senderDisplayedName = senderDisplayedName;
        this.senderAvatarUrl = senderAvatarUrl;

        this.messageBody = messageBody;
        this.isPhoto = isPhoto;
        this.photoTimeToLive = photoTimeToLive;

        this.photoIsViewed = false;
    }

    // ======================================================

//    public String getSendUid() {
//        return sendUid;
//    }

    public String getSenderDisplayedName() {
        return senderDisplayedName;
    }

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
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
}
