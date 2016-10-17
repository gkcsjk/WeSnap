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
    public static final String APP_FILE_PROVIDER =
            "com.unimelb.gof.wesnap.fileprovider";
    public static final String MY_UUID = "7c3fdc44-33a7-463e-9a9b-b039844bd410";

    // ======================================================
    /* Time to Live */
    public static final int NO_TTL = -1;
    public static final int DEFAULT_TTL = 3;
    public static final int MIN_TTL = 1;
    public static final int MAX_TTL = 10;

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
    static final String USERNAME_DEV_TEAM = "wesnap-dev";
    static final String ID_DEV_TEAM = "ZqiNmgsuE1hJlHOFZNnTSot8l882";

    public static final String TEXT_WELCOME =
            "Hello World! Happy Snapping, WeSnap Dev Team";

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
