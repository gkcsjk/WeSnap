package com.unimelb.gof.wesnap.chat;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.Message;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;


/**
 * MessagesActivity
 * This activity shows the messages in a chat conversation and
 * provides the action of view/replay/send messages.
 * (message: upload photo from local / text input) TODO take photo ???
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class MessagesActivity extends BaseActivity {
    private static final String TAG = "MessagesActivity";

    private static final int COLOR_SELF = R.color.colorSenderRed;
    private static final int COLOR_OTHER = R.color.colorSenderGreen;
    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_CHAT_TITLE = "chat_title";

    /* Firebase Database */
    private String idChat;
    private DatabaseReference refChatMessages;
    private String idCurrentUser;
    private DatabaseReference refCurrentUser;

    /* UI components */
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private TextView mChatTitle;
    private ImageButton mMessageUploadPhoto;
    private EditText mMessageEditText;
    private ImageButton mMessageSendButton;

    // ======================================================
    /* onCreate() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        /* Get chat info from intent */
        idChat = getIntent().getStringExtra(EXTRA_CHAT_ID);
        if (idChat == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CHAT_ID");
        }
        String chatTitle = getIntent().getStringExtra(EXTRA_CHAT_TITLE);
        if (chatTitle == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CHAT_TITLE");
        }

        /* Firebase Database */
        // get current user id & ref
        idCurrentUser = FirebaseUtil.getCurrentUserId();
        refCurrentUser = FirebaseUtil.getCurrentUserRef();
        if (idCurrentUser == null || refCurrentUser == null) {
            // null value; error out
            Log.e(TAG, "current user uid/ref unexpectedly null; goToLogin()");
            goToLogin("current user uid/ref: null");
            return;
        }
        // get current chat ref
        refChatMessages = FirebaseUtil.getMessagesRef().child(idChat);

        /* UI */
        // top: title text
        mChatTitle = (TextView) findViewById(R.id.text_title_messages);
        mChatTitle.setText(chatTitle);

        // bottom: input field & buttons
        mMessageUploadPhoto = (ImageButton) findViewById(R.id.button_upload_photo);
        mMessageEditText = (EditText) findViewById(R.id.field_text_message);
        mMessageSendButton = (ImageButton) findViewById(R.id.button_send_message);

        // middle: RecyclerView
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.recycler_messages);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        setupRecyclerAdapter();
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        setupButtonListener();
    }

    // ======================================================
    /* UI: FirebaseRecyclerAdapter */
    private void setupRecyclerAdapter() {
        // [START setting up mFirebaseAdapter]
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.item_message,
                MessageViewHolder.class,
                refChatMessages) {
            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              final Message message, final int position) {
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

                    if (message.getTimeToLive() <= 0) {
                        // photo uploaded from local
                        GlideUtil.loadImage(photoUrl, viewHolder.messengeImageButtonView);
                        viewHolder.messengeImageButtonView.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // TODO show full-screen photo
                                    }
                                });
                    } else {// message.getTimeToLive() > 0
                        // photo taken in app (timeout rule!!)
                        if (!message.isViewed()) {
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
    }

    // ======================================================
    private void setupButtonListener() {
        /* upload photo */
        mMessageUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO choose photo from local memory
            }
        });

        /* send message */
        mMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO check for message data
                String text = mMessageEditText.getText().toString();
                Message m;
                if (text.length() > 0) { /* text message */
                    // send out a text message
                    m = new Message(idCurrentUser, AppParams.getMyDisplayedName(), text, false);
                    FirebaseUtil.getChatsRef().child(idChat).child("lastMessageBody").setValue(text);
                    // clear the text input field
                    mMessageEditText.setText("");
                } else { /* photo message */
                    // TODO save photo to firebase storage & get url
                    m = new Message(idCurrentUser, AppParams.getMyDisplayedName(), "dummy", false);
                }

                // TODO create & send new message instance
                refChatMessages.push().setValue(m);
            }
        });
    }

    // ======================================================
    /* MessageViewHolder */
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
