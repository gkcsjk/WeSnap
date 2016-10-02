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
        setMyDisplayedName(newUserName);
        return new Chat(participants, TEXT_WELCOME, URL_DEV_TEAM, null);
    }

    /* Current User */
    private static String myDisplayedName;
    public static String getMyDisplayedName() {
        return myDisplayedName;
    }
    public static void setMyDisplayedName(String name) {
        myDisplayedName = name;
    }
}
