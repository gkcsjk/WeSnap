package com.unimelb.gof.wesnap.util;

import com.google.firebase.database.FirebaseDatabase;
import com.unimelb.gof.wesnap.models.*;
import com.google.firebase.database.DatabaseReference;

/**
 * WeSnap Dev Team
 */
public class AppParams {
    private static final String TAG = "AppParams";

    // TODO: AppParams

    /* WeSnap Dev Team */
    public static final String ID_DEV_TEAM = "ZqiNmgsuE1hJlHOFZNnTSot8l882";
    public static final String NAME_DEV_TEAM = "WeSnap Dev Team";
    public static final String URL_DEV_TEAM = null;
    public static final String TEXT_WELCOME = "Hello World! Happy Snapping, WeSnap Dev Team";

    public static final Message MESSAGE_WELCOME = new Message(
            ID_DEV_TEAM, NAME_DEV_TEAM, URL_DEV_TEAM,
            TEXT_WELCOME, false);

}
