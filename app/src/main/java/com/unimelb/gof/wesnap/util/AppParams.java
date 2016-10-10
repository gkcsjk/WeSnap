package com.unimelb.gof.wesnap.util;

import com.google.firebase.database.FirebaseDatabase;
import com.unimelb.gof.wesnap.models.*;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/**
 * WeSnap Dev Team info
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class AppParams {
    private static final String TAG = "AppParams";

    public static int NO_TTL = -1;
    public static int DEFAULT_TTL = 3;
    public static int MIN_TTL = 1;
    public static int MAX_TTL = 10;

    public static String FILEPROVIDER = "com.unimelb.gof.wesnap.fileprovider";
    public static final String MY_UUID = "7c3fdc44-33a7-463e-9a9b-b039844bd410";
    public static final String APP_NAME = "WeSnap";

    // ======================================================
    /* WeSnap Dev Team */
    public static final String ID_DEV_TEAM = "ZqiNmgsuE1hJlHOFZNnTSot8l882";
    public static final String EMAIL_DEV_TEAM = "wesnap@example.com";
    public static final String USERNAME_DEV_TEAM = "wesnap-dev";
    public static final String NAME_DEV_TEAM = "Dev Team WeSnap";
    public static final String URL_DEV_TEAM = "https://firebasestorage.googleapis.com/v0/b/gof-wesnap.appspot.com/o/AppParams%2Favatar-default.jpg?alt=media&token=ad8a66d1-f17b-40df-9942-c5863632cc34";

    private static final String TEXT_WELCOME = "Hello World! Happy Snapping, WeSnap Dev Team";

    public static Message getWelcomeMessage() {
        return new Message(ID_DEV_TEAM, NAME_DEV_TEAM, TEXT_WELCOME, false);
    }

    public static Chat getWelcomeChat (String newUserId, String newUserName) {
        HashMap<String, String> participants = new HashMap<>();
        participants.put(ID_DEV_TEAM, NAME_DEV_TEAM);
        participants.put(newUserId, newUserName);
        return new Chat(participants, TEXT_WELCOME, URL_DEV_TEAM, null);
    }

    // ======================================================
    /* Current User */
    public static User currentUser = null;

    public static String getMyEmail() {
        if (currentUser != null) {
            return currentUser.getEmail();
        }
        return null;
    }
    public static String getMyUsername() {
        if (currentUser != null) {
            return currentUser.getUsername();
        }
        return null;
    }
    public static String getMyDisplayedName() {
        if (currentUser != null) {
            return currentUser.getDisplayedName();
        }
        return null;
    }
    public static String getMyAvatarUrl() {
        if (currentUser != null) {
            return currentUser.getAvatarUrl();
        }
        return null;
    }
}
