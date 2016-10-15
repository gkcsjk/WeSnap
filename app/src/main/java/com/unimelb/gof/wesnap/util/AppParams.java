package com.unimelb.gof.wesnap.util;

import com.unimelb.gof.wesnap.models.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * WeSnap App shared settings
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class AppParams {
    private static final String TAG = "AppParams";
    public static final String APP_NAME = "WeSnap";

    public static final int NO_TTL = -1;
    public static final int DEFAULT_TTL = 3;
    public static final int MIN_TTL = 1;
    public static final int MAX_TTL = 10;

    public static final String FILEPROVIDER = "com.unimelb.gof.wesnap.fileprovider";
    public static final String MY_UUID = "7c3fdc44-33a7-463e-9a9b-b039844bd410";


    // ======================================================
    /* Timestamp in certain format */

    public static DateFormat getDateFormatter() {
        return (new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH));
    }

    public static String getCurrentTimeString() {
        return (getDateFormatter().format(new Date()));
    }

    public static final SimpleDateFormat PUBDATE_FORMATTER =
            new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.ENGLISH);

    // ======================================================
    /* Naming of imgae files */
    public static String getImageFilename() {
        return ("IMG_" + getCurrentTimeString());
    }

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
