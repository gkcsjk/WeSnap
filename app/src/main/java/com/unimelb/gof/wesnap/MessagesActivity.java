package com.unimelb.gof.wesnap;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.unimelb.gof.wesnap.models.Message;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

import org.w3c.dom.Text;


/**
 * MessagesActivity
 * This activity shows the messages in a chat conversation and
 * provides the action of view/replay/send messages. TODO
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class MessagesActivity extends BaseActivity {
    private static final String TAG = "MessagesActivity";

    private static final int COLOR_SELF = R.color.colorSenderRed;
    private static final int COLOR_OTHER = R.color.colorSenderGreen;
    public static final String EXTRA_CHAT_KEY = "chat_key";
    public static final String EXTRA_CHAT_TITLE = "chat_title";

    private String idCurrentUser;
    private String mChatKey;
    private DatabaseReference refChatMessages;

    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;

    private TextView mChatTitle;
    private EditText mMessageEditText;
    private Button mMessageSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        /* Get chat id from intent */
        mChatKey = getIntent().getStringExtra(EXTRA_CHAT_KEY);
        if (mChatKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CHAT_KEY");
        }

        /* Get chat title from intent */
        String chatTitle = getIntent().getStringExtra(EXTRA_CHAT_TITLE);
        if (chatTitle == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CHAT_TITLE");
        }
        mChatTitle = (TextView) findViewById(R.id.text_title_messages);
        mChatTitle.setText(chatTitle);

        /* Firebase Database */
        idCurrentUser = FirebaseUtil.getCurrentUserId();
        if (idCurrentUser == null) {
            // null value; error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            goToLogin("current user uid: null");
            return;
        }
        refChatMessages = FirebaseUtil.getMessagesRef().child(mChatKey);

        /* UI : RecyclerView */
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.recycler_messages);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        /* FirebaseRecyclerAdapter */
        // [START setting up mFirebaseAdapter]
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.item_message,
                MessageViewHolder.class,
                refChatMessages) {

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              final Message message,
                                              final int position) {
                Log.d(TAG, "populateViewHolder:" + position);
                final DatabaseReference refMessage = getRef(position);

                /* Sender color bar */
                if (message.getSenderUid().equals(idCurrentUser)) {
                    viewHolder.messageSenderBarView.setBackgroundResource(COLOR_SELF);
                } else {
                    viewHolder.messageSenderBarView.setBackgroundResource(COLOR_OTHER);
                }

                /* Sender name text */
                viewHolder.messageSenderNameView.setText(message.getSenderDisplayedName());

                if (!message.isPhoto()) {
                    /* message in text */
                    viewHolder.messengeImageButtonView.setVisibility(View.GONE);
                    viewHolder.messengeTextView.setVisibility(View.VISIBLE);
                    viewHolder.messengeTextView.setText(message.getMessageBody());
                } else {
                    /* photo message */
                    viewHolder.messengeImageButtonView.setVisibility(View.VISIBLE);
                    viewHolder.messengeTextView.setVisibility(View.GONE);

                    final String photoUrl = message.getMessageBody();
                    if (photoUrl == null || photoUrl.length() == 0) {
                        // null photoUrl value; remove message
                        Log.w(TAG, "photo url: unexpected null value");
                        refMessage.removeValue();
                        return;
                    }

                    if (message.getPhotoTimeToLive() <= 0) {
                        // photo uploaded from local
                        GlideUtil.loadImage(photoUrl, viewHolder.messengeImageButtonView);
                        viewHolder.messengeImageButtonView.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // TODO show full-screen photo
                                    }
                                });
                    } else {// message.getPhotoTimeToLive() > 0
                        // photo taken in app (timeout rule!!)
                        if (!message.isPhotoIsViewed()) {
                            // for first-time view
                            viewHolder.messengeImageButtonView.setImageResource(R.drawable.ic_action_photo_view);
                            viewHolder.messengeImageButtonView.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // TODO show full-screen photo (timeout !!!)

                                            // after view:
                                            refMessage.child("photoIsViewed").setValue(true);
                                        }
                                    });
                        } else {
                            // for replay
                            viewHolder.messengeImageButtonView.setImageResource(R.drawable.ic_action_photo_replay);
                            viewHolder.messengeImageButtonView.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // TODO show full-screen photo (timeout !!!)

                                            // after view:
                                            viewHolder.messengeImageButtonView.setImageResource(R.drawable.ic_action_disabled_photo);
                                            refMessage.child("isPhoto").setValue(false);
                                            refMessage.child("messageBody").setValue("Photo viewed and replayed!");
                                            // TODO delete photo from Firebase storage???
                                        }
                                    });
                        }
                    }
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(
                new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                        int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                        // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                        // to the bottom of the list to show the newly added message.
                        if (lastVisiblePosition == -1 ||
                                (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                            mMessageRecyclerView.scrollToPosition(positionStart);
                        }
                    }
                });
        // [END setting up mFirebaseAdapter]

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView messageSenderBarView;
        public TextView messageSenderNameView;

        public ImageButton messengeImageButtonView;
        public TextView messengeTextView;

        public MessageViewHolder(View v) {
            super(v);
            messageSenderBarView = (ImageView) itemView.findViewById(R.id.image_message_sender);
            messageSenderNameView = (TextView) itemView.findViewById(R.id.text_message_sender);

            messengeImageButtonView = (ImageButton) itemView.findViewById(R.id.button_message_view_photo);
            messengeTextView = (TextView) itemView.findViewById(R.id.text_message);
        }
    }
}
