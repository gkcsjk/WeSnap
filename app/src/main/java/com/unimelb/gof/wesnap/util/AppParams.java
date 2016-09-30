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
    public static final String URL_DEV_TEAM = "https://firebasestorage.googleapis.com/v0/b/gof-wesnap.appspot.com/o/AppParams%2Favatar-default.jpg?alt=media&token=ad8a66d1-f17b-40df-9942-c5863632cc34";

    private static final String TEXT_WELCOME = "Hello World! Happy Snapping, WeSnap Dev Team";

    public static Message getWelcomeMessage() {
        return new Message(ID_DEV_TEAM, NAME_DEV_TEAM, URL_DEV_TEAM, TEXT_WELCOME, false);
    }
    public static Chat getWelcomeChat (String newUserId) {
        return new Chat(new String[]{AppParams.ID_DEV_TEAM, newUserId},
                TEXT_WELCOME, URL_DEV_TEAM, NAME_DEV_TEAM);
    }

}
