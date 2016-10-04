package com.unimelb.gof.wesnap.friend;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.chat.StartChat;

/**
 * FriendItemViewHolder
 * ViewHolder for item_friend.
 * It provides a method to change the "Add" ImageButton to "Done".
 * Access to the variables and method is package local.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class FriendItemViewHolder extends RecyclerView.ViewHolder {
    public ImageView avatarView;
    public TextView nameView;
    public TextView emailView;
    public ImageButton doButton;

    public FriendItemViewHolder(View v) {
        super(v);

        avatarView = (ImageView) itemView.findViewById(R.id.avatar_request);
        nameView = (TextView) itemView.findViewById(R.id.text_name_request);
        emailView = (TextView) itemView.findViewById(R.id.text_email_request);
        doButton = (ImageButton) itemView.findViewById(R.id.button_request);
    }

    /* Default as "add friend" button */
    void useAddButton(final String uid) {
        doButton.setImageResource(R.drawable.ic_action_add_friend);
        doButton.setColorFilter(R.color.colorPrimary);
        doButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send friend requests to "mResultUid"
                FriendRequest.sendFriendRequest(uid, FriendItemViewHolder.this, v);
            }
        });
    }

    /* Update the button UI after friend request sent or if already isFriend */
    void useDoneButton() {
        doButton.setImageResource(R.drawable.ic_action_done);
        doButton.setColorFilter(R.color.colorAccent);
        doButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Friend request sent!",
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /* Accept rquest */
    void useAcceptButton(final DatabaseReference refRequest) {
        doButton.setImageResource(R.drawable.ic_action_add_friend);
        doButton.setColorFilter(R.color.colorPrimary);
        doButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // accept friend requests
                FriendRequest.acceptFriendRequest(refRequest, FriendItemViewHolder.this, v);
            }
        });
    }

    /* Update the button UI if trying to chat */
    void useChatButton(final Context context, final String uid, final String name) {
        doButton.setImageResource(R.drawable.ic_action_chat);
        doButton.setColorFilter(R.color.colorPrimary);
        doButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartChat.checkExistingChats(context, uid, name);
            }
        });
    }
}

