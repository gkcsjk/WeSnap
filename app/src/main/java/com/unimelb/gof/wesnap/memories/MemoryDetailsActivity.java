package com.unimelb.gof.wesnap.memories;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.camera.EditPhotoActivity;
import com.unimelb.gof.wesnap.chat.ChooseFriendActivity;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;
import com.unimelb.gof.wesnap.util.PhotoUploader;

import java.io.File;
import java.io.IOException;

/**
 * MemoryDetailsActivity
 * Provides fullscreen view of photo in My Memories;
 * Provides options to delete, share, lock, create story, and edit photo
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class MemoryDetailsActivity extends BaseActivity
        implements View.OnClickListener {
    private static final String TAG = "MemoryDetailsActivity";

    public static final String EXTRA_PHOTO_FILENAME = "photo_filename";
    public static final String EXTRA_PHOTO_URI = "photo_uri";

    private String mFirebaseFilename;   // Firebase filename
    private String mFirebaseUri;        // Firebase downloadable uri
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;

    /* UI */
    private ImageView mPhotoFullscreenView;
    private ImageButton mButtonDelete;
    private ImageButton mButtonShare;
    private ImageButton mButtonLock;
    private ImageButton mButtonStory;
    private ImageButton mButtonEdit;
    private ImageButton mButtonSend;
    private boolean mIsVisible = true;

    // ======================================================
    /* onCreate() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_details);

        /* Photo Filename and Uri on Firebase */
        mFirebaseFilename = getIntent().getStringExtra(EXTRA_PHOTO_FILENAME);
        if (mFirebaseFilename == null) {
            throw new IllegalArgumentException("Must pass EXTRA_PHOTO_FILENAME");
        }
        mFirebaseUri = getIntent().getStringExtra(EXTRA_PHOTO_URI);
        if (mFirebaseUri == null) {
            throw new IllegalArgumentException("Must pass EXTRA_PHOTO_URI");
        }

        /* Firebase Database & Storage */
        DatabaseReference dbRef = FirebaseUtil.getMyMemoriesDatabase();
        StorageReference stRef = FirebaseUtil.getMyMemoriesStorage();
        if (dbRef == null || stRef == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            goToLogin("unexpected null value");
            return;
        }
        mDatabaseRef = dbRef.child(mFirebaseFilename);
        mStorageRef = stRef.child(mFirebaseFilename);

        // [START setting up UI]
        /* Hide action bar if any */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        showProgressDialog();

        /* UI: fullscreen photo view */
        mPhotoFullscreenView = (ImageView) findViewById(R.id.image_fullscreen_memory);
        Log.d(TAG, "showPhoto:src=" + mFirebaseUri);
        GlideUtil.loadImage(mFirebaseUri, mPhotoFullscreenView);
        mPhotoFullscreenView.setVisibility(View.VISIBLE);
        mPhotoFullscreenView.setOnClickListener(this);

        /* UI: control buttons */
        mButtonDelete = (ImageButton) findViewById(R.id.bt_delete_memory);
        mButtonDelete.setOnClickListener(this);
        mButtonShare = (ImageButton) findViewById(R.id.bt_share_memory);
        mButtonShare.setOnClickListener(this);

        mButtonLock = (ImageButton) findViewById(R.id.bt_lock_memory);
        mButtonLock.setOnClickListener(this); // TODO
        mButtonStory = (ImageButton) findViewById(R.id.bt_create_story);
        mButtonStory.setOnClickListener(this);

        mButtonEdit = (ImageButton) findViewById(R.id.bt_edit_memory);
        mButtonEdit.setOnClickListener(this);
        mButtonSend = (ImageButton) findViewById(R.id.bt_send_memory);
        mButtonSend.setOnClickListener(this);

        hideProgressDialog();
        // [END setting up UI]
    }

    // ========================================================
    /* onClick(): perform the requested actions by the clicked buttons */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
            case R.id.image_fullscreen_memory:
                updateControls();
                break;
            case R.id.bt_delete_memory:
                deleteMemory();
                break;
            case R.id.bt_share_memory:
                shareMemory();
                break;
            case R.id.bt_lock_memory:
                lockMemory();
                break;
            case R.id.bt_create_story:
                createStory();
                break;
            case R.id.bt_edit_memory:
                editMemory();
                break;
            case R.id.bt_send_memory:
                sendMemory();
                break;
        }
    }

    // ========================================================
    /* Show/Hide the control buttons */
    private void updateControls() {
        if (mIsVisible) {
            findViewById(R.id.ui_group_memory_controls).setVisibility(View.GONE);
            findViewById(R.id.ui_group_memory_send_controls).setVisibility(View.GONE);
            findViewById(R.id.bt_send).setVisibility(View.GONE);
            mIsVisible = false;
        } else {
            findViewById(R.id.ui_group_memory_controls).setVisibility(View.VISIBLE);
            findViewById(R.id.ui_group_memory_send_controls).setVisibility(View.VISIBLE);
            findViewById(R.id.bt_send).setVisibility(View.VISIBLE);
            mIsVisible = true;
        }
    }

    // ========================================================
    /* deleteMemory(): delete from Firebase Storage & Database */
    private void deleteMemory() {
        Log.d(TAG, "deleteMemory:filename=" + mFirebaseFilename);
        mStorageRef.delete();
        mDatabaseRef.removeValue();
        Toast.makeText(MemoryDetailsActivity.this,
                "Deleted",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    // ========================================================
    /* shareMemory(): share to external apps */
    private void shareMemory() {
        Log.d(TAG, "shareMemory:filename=" + mFirebaseFilename);

        showProgressDialog();

        // create a local file
        final File localFile = getLocalFileInstance();
        if (localFile == null) {
            Log.e(TAG, "shareMemory:local file error");
            Toast.makeText(MemoryDetailsActivity.this,
                    "Unable to send due to local file error",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // download photo to the local file
        mStorageRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.e(TAG, "downloadFile:filename=" + mFirebaseFilename);
                        hideProgressDialog();
                        // create a local uri to share
                        Uri photoUri = FileProvider.getUriForFile(
                                MemoryDetailsActivity.this,
                                AppParams.APP_FILE_PROVIDER, localFile);
                        // Start the system share action
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
                        shareIntent.setType("image/jpeg");
                        startActivity(Intent.createChooser(shareIntent,
                                getResources().getText(R.string.action_share_memory_to)));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "downloadFile:failure:", exception);
                        hideProgressDialog();
                        Toast.makeText(MemoryDetailsActivity.this,
                                "Unable to send due to download failure",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ========================================================
    /* lockMemory() */
    private void lockMemory() {
        // TODO lockMemory....
        Toast.makeText(MemoryDetailsActivity.this,
                "lockMemory() TBD",
                Toast.LENGTH_SHORT).show();
    }

    // ========================================================
    /* createStory() */
    private void createStory() {
        // create a local file
        File localFile = getLocalFileInstance();
        if (localFile == null) {
            Log.e(TAG, "editMemory:local file error");
            Toast.makeText(MemoryDetailsActivity.this,
                    "Unable to send due to local file error",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Get uri
        final Uri photoUri = FileProvider.getUriForFile(
                MemoryDetailsActivity.this, AppParams.APP_FILE_PROVIDER, localFile);

        // download photo to the local file
        mStorageRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.e(TAG, "downloadFile:filename=" + mFirebaseFilename);
                        // Downloaded to local file
                        // Send as new Story
                        PhotoUploader.uploadToStories(photoUri, MemoryDetailsActivity.this);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "downloadFile:failure:", exception);
                        Toast.makeText(MemoryDetailsActivity.this,
                                "Unable to send due to download failure",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ========================================================
    /* editMemory(): edit & send to a friend as timed photo message */
    private void editMemory() {
        Log.d(TAG, "editMemory:filename=" + mFirebaseFilename);

        // create a local file
        final File localFile = getLocalFileInstance();
        if (localFile == null) {
            Log.e(TAG, "editMemory:local file error");
            Toast.makeText(MemoryDetailsActivity.this,
                    "Unable to send due to local file error",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // download photo to the local file
        mStorageRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.e(TAG, "downloadFile:filename=" + mFirebaseFilename);
                        // Downloaded to local file
                        Intent editPhotoIntent = new Intent(
                                MemoryDetailsActivity.this, EditPhotoActivity.class);
                        editPhotoIntent.putExtra(
                                EditPhotoActivity.EXTRA_PHOTO_PATH,
                                localFile.getAbsolutePath());
                        startActivity(editPhotoIntent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "downloadFile:failure:", exception);
                        Toast.makeText(MemoryDetailsActivity.this,
                                "Unable to send due to download failure",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ========================================================
    /* sendMemory(): send to a friend as non-timed message */
    private void sendMemory() {
        Log.d(TAG, "sendMemory:filename=" + mFirebaseFilename);

        // create a local file
        final File localFile = getLocalFileInstance();
        if (localFile == null) {
            Log.e(TAG, "sendMemory:local file error");
            Toast.makeText(MemoryDetailsActivity.this,
                    "Unable to send due to local file error",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // download photo to the local file
        mStorageRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.e(TAG, "downloadFile:filename=" + mFirebaseFilename);
                        // Downloaded to local file
                        // choose a friend as receiver
                        Intent sendPhotoIntent = new Intent(
                                MemoryDetailsActivity.this, ChooseFriendActivity.class);
                        sendPhotoIntent.putExtra(
                                ChooseFriendActivity.EXTRA_PHOTO_PATH, localFile.getAbsolutePath());
                        startActivity(sendPhotoIntent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "downloadFile:failure:", exception);
                        Toast.makeText(MemoryDetailsActivity.this,
                                "Unable to send due to download failure",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ========================================================
    /* getLocalFileInstance(): create a local file */
    private File getLocalFileInstance() {
        File localFile;
        try {
            File storageDir = MemoryDetailsActivity.this.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES);
            localFile = File.createTempFile(
                    AppParams.getImageFilename(), ".jpg", storageDir);
        } catch (IOException e) {
            Log.e(TAG, "create file error");
            return null;
        }
        return localFile;
    }

    // ========================================================
}
