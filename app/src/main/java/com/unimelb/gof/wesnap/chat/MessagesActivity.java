package com.unimelb.gof.wesnap.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.camera.EditPhotoActivity;
import com.unimelb.gof.wesnap.models.Message;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;

import java.io.File;
import java.io.IOException;


/**
 * MessagesActivity
 * This activity shows the messages in a chat conversation and
 * provides the action of view/replay/send messages.
 * (message: upload photo from local / text input) TODO take photo ???
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class MessagesActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MessagesActivity";

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_CHAT_TITLE = "chat_title";
    private static final int COLOR_SELF = R.color.colorSenderRed;
    private static final int COLOR_OTHER = R.color.colorSenderGreen;

    private static int REQUEST_LOAD_LOCAL_IMAGE = 1;
    private static int REQUEST_IMAGE_CAPTURE = 2;
    private static int REQUEST_IMAGE_EDIT = 3;
    private static int REQUEST_IMAGE_VIEWED = 4;
    private static int REQUEST_IMAGE_REPLAYED = 5;

    /* For local photo path and uri */
    public static final String EXTRA_PHOTO_PATH = "photo_path";
    public static final String EXTRA_TIME_TO_LIVE = "time_to_live";
    private String mLocalPhotoPath;

    /* Firebase Database */
    private String mChatId;
    private DatabaseReference mThisChatDatabaseRef;
    private DatabaseReference mThisMessagesDatabaseRef;
    private String mCurrentUserId;
    private DatabaseReference mCurrentUserDatabseRef;

    /* Firebase Storage */
    private StorageReference mThisChatStorageRef;

    /* UI components */
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private TextView mChatTitle;

    private ImageButton mMessageUploadPhoto;
    private ImageButton mMessageTakePhoto;
    private EditText mMessageEditText;
    private ImageButton mMessageSendButton;

    // ======================================================
    /* onCreate() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        /* Get the required chat info from intent;
         * error out if info absent */
        mChatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
        if (mChatId == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CHAT_ID");
        }
        String chatTitle = getIntent().getStringExtra(EXTRA_CHAT_TITLE);
        if (chatTitle == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CHAT_TITLE");
        }

        /* Firebase Database */
        // get current user id & ref
        mCurrentUserId = FirebaseUtil.getCurrentUserId();
        mCurrentUserDatabseRef = FirebaseUtil.getCurrentUserRef();
        if (mCurrentUserId == null || mCurrentUserDatabseRef == null) {
            // null value; error out
            Log.e(TAG, "current user uid/ref unexpectedly null; goToLogin()");
            goToLogin("current user uid/ref: null");
            return;
        }
        // get current database ref
        mThisChatDatabaseRef = FirebaseUtil.getChatsRef().child(mChatId);
        mThisMessagesDatabaseRef = FirebaseUtil.getMessagesRef().child(mChatId);

        /* Firebase Storage */
        mThisChatStorageRef = FirebaseUtil.getChatsStorage().child(mChatId);

        /* Send out initial photo if local file path is passed */
        String filePath = getIntent().getStringExtra(EXTRA_PHOTO_PATH);
        int timeToLive = getIntent().getIntExtra(EXTRA_TIME_TO_LIVE, AppParams.DEFAULT_TTL);
        if (filePath != null && timeToLive > 0) {
            sendPhotoFromPath(filePath, timeToLive);
        }

        /* UI */
        // top: title text
        mChatTitle = (TextView) findViewById(R.id.text_title_messages);
        mChatTitle.setText(chatTitle);

        // bottom: input field & buttons
        mMessageUploadPhoto = (ImageButton) findViewById(R.id.button_upload_photo);
        mMessageUploadPhoto.setOnClickListener(this);
        mMessageTakePhoto = (ImageButton) findViewById(R.id.button_take_photo);
        mMessageTakePhoto.setOnClickListener(this);
        mMessageSendButton = (ImageButton) findViewById(R.id.button_send_message);
        mMessageSendButton.setOnClickListener(this);
        mMessageEditText = (EditText) findViewById(R.id.field_text_message);

        // middle: RecyclerView
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.recycler_messages);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        setupRecyclerAdapter();
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    // ======================================================
    /* UI: FirebaseRecyclerAdapter */
    private void setupRecyclerAdapter() {
        // [START setting up mFirebaseAdapter]
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.item_message,
                MessageViewHolder.class,
                mThisMessagesDatabaseRef) {

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              final Message message, final int position) {
                Log.d(TAG, "populateViewHolder:" + position);
                final DatabaseReference refMessage = getRef(position);

                /* Sender color bar */
                if (message.getSenderUid().equals(mCurrentUserId)) { // self
                    viewHolder.messageSenderBarView.setBackgroundResource(COLOR_SELF);
                } else {
                    viewHolder.messageSenderBarView.setBackgroundResource(COLOR_OTHER);
                }

                /* Sender name text */
                viewHolder.messageSenderNameView.setText(message.getSenderDisplayedName());

                /* Text Message */
                if (!message.isPhoto()) {
                    Log.e(TAG, "msgType=text");
                    /* message in text */
                    viewHolder.messengeImageButtonView.setVisibility(View.GONE);
                    viewHolder.messengeTextView.setVisibility(View.VISIBLE);
                    viewHolder.messengeTextView.setText(message.getMessageBody());
                    return;
                } // else ...

                /* Photo Message */
                Log.e(TAG, "msgType=photo");
                viewHolder.messengeImageButtonView.setVisibility(View.VISIBLE);
                viewHolder.messengeTextView.setVisibility(View.GONE);

                final String photoFilename = message.getMessageBody();
                if (photoFilename == null || photoFilename.length() == 0) {
                    // null photoFilename value; remove message
                    Log.e(TAG, "photo url: unexpected null value");
                    refMessage.removeValue();
                    return;
                }

                final int timeToLive = message.getTimeToLive();

                /* show-photo button */
                // button image
                if (timeToLive <= 0 || !message.isViewed()) {
                    // for local photo OR for first-time view
                    viewHolder.messengeImageButtonView.setImageResource(R.drawable.ic_action_photo_view);
                } else {
                    // for replay
                    viewHolder.messengeImageButtonView.setImageResource(R.drawable.ic_action_photo_replay);
                }

                // button listener: only for receiver, not sender
                if (!message.getSenderUid().equals(mCurrentUserId)) {
                    // [START show-photo button listener]
                    viewHolder.messengeImageButtonView.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // create intent
                                    Intent showPhotoIntent = new Intent(MessagesActivity.this, PhotoFullscreenActivity.class);
                                    showPhotoIntent.putExtra(PhotoFullscreenActivity.EXTRA_CHAT_ID, mChatId);
                                    showPhotoIntent.putExtra(PhotoFullscreenActivity.EXTRA_MESSAGE_ID, refMessage.getKey());
                                    showPhotoIntent.putExtra(PhotoFullscreenActivity.EXTRA_PHOTO_FILENAME, photoFilename);
                                    showPhotoIntent.putExtra(PhotoFullscreenActivity.EXTRA_TIME_TO_LIVE, timeToLive);

                                    // show full-screen photo
                                    if (timeToLive <= 0 || !message.isViewed()) {
                                        // for local photo OR for first-time view
                                        startActivityForResult(showPhotoIntent, REQUEST_IMAGE_VIEWED);
                                    } else {
                                        // for replay
                                        startActivityForResult(showPhotoIntent, REQUEST_IMAGE_REPLAYED);
                                    }
                                }
                            });
                    // [END show-photo button listener]
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

    // ========================================================
    /* onClick(): perform the requested actions by the clicked buttons */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
            case R.id.button_upload_photo:
                pickLocalPhoto();
                break;
            case R.id.button_take_photo:
                takePhoto();
                break;
            case R.id.button_send_message:
                sendTextMessage();
                break;
        }
    }

    // ======================================================
    /* Choose a photo from local */
    private void pickLocalPhoto() {
        Log.d(TAG, "pickLocalPhoto");
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_LOAD_LOCAL_IMAGE);
    }

    // ======================================================
    /* Start camera to take a photo */
    private void takePhoto() {
        Intent startCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (startCameraIntent.resolveActivity(
                MessagesActivity.this.getPackageManager()) != null) {

            // create image file
            File photoFile = null;
            try {
                File storageDir = MessagesActivity.this.getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES);
                photoFile = File.createTempFile(
                        AppParams.getImageFilename(), ".jpg", storageDir);
            } catch (IOException ex) {
                Log.e(TAG, "create file error");
            }

            if (photoFile != null) {
                // save a copy of the photo path
                mLocalPhotoPath = photoFile.getAbsolutePath();
                // start image capture from message screen
                Uri photoURI = FileProvider.getUriForFile(
                        MessagesActivity.this,
                        "com.unimelb.gof.wesnap.fileprovider",
                        photoFile);
                startCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(startCameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // ======================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOAD_LOCAL_IMAGE && resultCode == RESULT_OK && null != data) {
            Log.d(TAG, "onActivityResult:REQUEST_LOAD_LOCAL_IMAGE");
            Uri selectedImageUri = data.getData();
            sendPhotoFromUri(selectedImageUri, AppParams.NO_TTL);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult:REQUEST_IMAGE_CAPTURE");

            if (mLocalPhotoPath != null) {
                Intent editPhotoIntent = new Intent(MessagesActivity.this, EditPhotoActivity.class);
                editPhotoIntent.putExtra(EditPhotoActivity.EXTRA_PHOTO_PATH, mLocalPhotoPath);
                editPhotoIntent.putExtra(EditPhotoActivity.EXTRA_CHAT_ID, mChatId);
                startActivityForResult(editPhotoIntent, REQUEST_IMAGE_EDIT);
            } else {
                throw new IllegalArgumentException("photo path is null for REQUEST_IMAGE_CAPTURE");
            }

        } else if (requestCode == REQUEST_IMAGE_EDIT && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "onActivityResult:REQUEST_IMAGE_EDIT");

            String localFilePath = data.getStringExtra(EXTRA_PHOTO_PATH);
            int timeToLive = data.getIntExtra(EXTRA_TIME_TO_LIVE, AppParams.DEFAULT_TTL);
            if (localFilePath != null && timeToLive > 0) {
                sendPhotoFromPath(localFilePath, timeToLive);
            } else {
                throw new IllegalArgumentException("Must pass photo data from REQUEST_IMAGE_EDIT");
            }

        } else if (requestCode == REQUEST_IMAGE_VIEWED) {
            Log.d(TAG, "onActivityResult:REQUEST_IMAGE_VIEWED");
            if (resultCode == RESULT_OK && data != null) {
                // update Firebase Database
                String msgId = data.getStringExtra(PhotoFullscreenActivity.EXTRA_MESSAGE_ID);
                mThisMessagesDatabaseRef.child(msgId).child("viewed").setValue(true);

            } else if (resultCode == RESULT_CANCELED) {
                Log.e(TAG, "onActivityResult:REQUEST_IMAGE_VIEWED:RESULT_CANCELED");
                Toast.makeText(MessagesActivity.this,
                        "Failed to load the photo",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == REQUEST_IMAGE_REPLAYED) {
            Log.d(TAG, "onActivityResult:REQUEST_IMAGE_REPLAYED");
            if (resultCode == RESULT_OK && data != null) {
                // update Firebase Database
                final String msgId = data.getStringExtra(PhotoFullscreenActivity.EXTRA_MESSAGE_ID);
                mThisMessagesDatabaseRef.child(msgId).child("photo").setValue(false);
                mThisMessagesDatabaseRef.child(msgId).child("messageBody").setValue("Photo viewed and replayed!");
                // delete photo from Firebase storage
                final String photoFilename = data.getStringExtra(PhotoFullscreenActivity.EXTRA_PHOTO_FILENAME);
                mThisChatStorageRef.child(photoFilename).delete();

            } else if (resultCode == RESULT_CANCELED) {
                Log.e(TAG, "onActivityResult:REQUEST_IMAGE_REPLAYED:RESULT_CANCELED");
                Toast.makeText(MessagesActivity.this,
                        "Failed to load the photo",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ======================================================
    /** sendPhotoFromPath()
     * @params String localFilePath
     * @params int timeToLive
     *
     * Upload local photo to Firebase Storage, and then send photo message
     * by creating a new message instance and saving it to Firebase Database
     * */
    private void sendPhotoFromPath(final String localFilePath,
                                   final int timeToLive) {
        Log.d(TAG, "sendPhotoFromPath:src=" + localFilePath);
        Log.d(TAG, "sendPhotoFromPath:ttl=" + timeToLive);

        File photoFile = new File(localFilePath);
        Uri photoUri = FileProvider.getUriForFile(MessagesActivity.this,
                "com.unimelb.gof.wesnap.fileprovider",
                photoFile);
        sendPhotoFromUri(photoUri, timeToLive);
    }

    // ======================================================
    /** sendPhotoFromUri()
     * @params Uri localFileUri
     * @params int timeToLive
     *
     * Upload local photo to Firebase Storage, and then send photo message
     * by creating a new message instance and saving it to Firebase Database
     * */
    private void sendPhotoFromUri(final Uri localFileUri,
                                  final int timeToLive) {
        Log.d(TAG, "sendPhotoFromUri:src=" + localFileUri.toString());
        Log.d(TAG, "sendPhotoFromUri:ttl=" + timeToLive);

        // get unique filename
        //final String filename = localFileUri.getLastPathSegment();
        final String filename = AppParams.getMyUsername() +"_"+ AppParams.getImageFilename();

        // Upload file to Firebase Storage
        showProgressDialog();
        final StorageReference photoRef = mThisChatStorageRef.child(filename);
        Log.d(TAG, "sendPhotoFromUri:dst=" + photoRef.getPath());
        photoRef.putFile(localFileUri)
                .addOnSuccessListener(MessagesActivity.this,
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Upload succeeded
                                Log.d(TAG, "sendPhotoFromUri:onSuccess");
                                // Get the public download URL (not used!!!)
                                Uri downloadUrl = taskSnapshot.getMetadata()
                                        .getDownloadUrl();
                                // Create & send new photo message instance
                                Message message = new Message(
                                        mCurrentUserId,
                                        AppParams.getMyDisplayedName(),
                                        filename,
                                        true,
                                        timeToLive);
                                mThisMessagesDatabaseRef.push().setValue(message);
                                mThisChatDatabaseRef.child("lastMessageBody")
                                        .setValue("Tap to view new photo!");
                                // update UI
                                hideProgressDialog();
                                Toast.makeText(MessagesActivity.this, "Photo sent",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(MessagesActivity.this,
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Upload failed
                                Log.w(TAG, "sendPhotoFromUri:onFailure", exception);
                                // Get the public download URL (not used!!!)
                                Uri downloadUrl = null;
                                // update UI
                                hideProgressDialog();
                                Toast.makeText(MessagesActivity.this, "Error: upload failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    // ======================================================
    /* Send text message */
    private void sendTextMessage() {
        Log.d(TAG, "sendTextMessage");
        // check text input field
        String text = mMessageEditText.getText().toString();
        Message m;
        if (text.length() > 0) {
            // create & send new message instance
            m = new Message(mCurrentUserId, AppParams.getMyDisplayedName(), text, false);
            FirebaseUtil.getChatsRef().child(mChatId).child("lastMessageBody").setValue(text);
            mThisMessagesDatabaseRef.push().setValue(m);
            // clear the text input field
            mMessageEditText.setText("");
        } else {
            // empty input field
            Toast.makeText(MessagesActivity.this, "Empty text input",
                    Toast.LENGTH_SHORT).show();
        }
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
