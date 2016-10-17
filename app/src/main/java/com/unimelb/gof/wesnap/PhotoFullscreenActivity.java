package com.unimelb.gof.wesnap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.memories.MemoryDetailsActivity;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.io.File;
import java.io.IOException;

/**
 * PhotoFullscreenActivity
 * Show photo, possibly with timeout rules
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class PhotoFullscreenActivity extends BaseActivity {
    private static final String TAG = "PhotoFullscreenActivity";

    public static final String EXTRA_PHOTO_URI_STRING = "photo_uri";
    public static final String EXTRA_PHOTO_FILENAME = "photo_filename";
    public static final String EXTRA_TIME_TO_LIVE = "time_to_live";
    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_MESSAGE_ID = "msg_id";

    private String mPhotoUriString;
    private String mFilename;
    private StorageReference mStorageRef;
    private static final int MILLIS_IN_ONE_SECOND = 1000;
    private int mTimeToLiveMillis;
    private CountDownTimer mTimer;

    /* UI */
    private ImageView mPhotoFullscreenView;
    private TextView mCountDownTextView;

    // ======================================================
    /* onCreate() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_fullscreen);

        /* photo path OR download uri in string */
        mFilename = getIntent().getStringExtra(EXTRA_PHOTO_FILENAME);
        mPhotoUriString = getIntent().getStringExtra(EXTRA_PHOTO_URI_STRING);
        if (mFilename == null && mPhotoUriString == null) {
            throw new IllegalArgumentException(
                    "Must pass either EXTRA_PHOTO_FILENAME or EXTRA_PHOTO_URI_STRING");
        }

        /* chat ID if any */
        String chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
        if (chatId != null) {
            /* Firebase Storage */
            mStorageRef = FirebaseUtil.getChatsStorage().child(chatId).child(mFilename);
        }

        /* time to live */
        mTimeToLiveMillis = getIntent().getIntExtra(EXTRA_TIME_TO_LIVE,
                AppParams.DEFAULT_TTL);
        mTimeToLiveMillis = mTimeToLiveMillis * MILLIS_IN_ONE_SECOND;

        /* hide action bar if any */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        showProgressDialog();

        /* get UI elements */
        mPhotoFullscreenView = (ImageView) findViewById(R.id.image_fullscreen_show_photo);
        mPhotoFullscreenView.setVisibility(View.GONE);
        mCountDownTextView = (TextView) findViewById(R.id.text_countdown);
        mCountDownTextView.setVisibility(View.GONE);

        /* set up countdown */
        if (mTimeToLiveMillis > 0) {
            // needs timeout rule
            mCountDownTextView.setVisibility(View.VISIBLE);
            mTimer = new CountDownTimer(mTimeToLiveMillis, MILLIS_IN_ONE_SECOND) {
                public void onTick(long millisUntilFinished) {
                    long secondUntilFinished = millisUntilFinished / MILLIS_IN_ONE_SECOND;
                    mCountDownTextView.setText(
                            getString(R.string.text_photo_countdown, secondUntilFinished));
                }

                public void onFinish() {
                    // hide photo & quit
                    mPhotoFullscreenView.setVisibility(View.GONE);
                    quitOk();
                }
            };
        }

        /* try uri first */
        if (mPhotoUriString != null) {
            GlideUtil.loadPhoto(mPhotoUriString, mPhotoFullscreenView);
            mPhotoFullscreenView.setVisibility(View.VISIBLE);
            hideProgressDialog();
            if (mTimer != null) {
                mTimer.start();
            }
            return;
        }

        /* get photo from Firebase & show photo */
        Log.d(TAG, "showPhoto:src=" + mStorageRef.getPath());

        // create a local file
        final File localFile = getLocalFileInstance();
        if (localFile == null) {
            Log.e(TAG, "showPhoto:local file error");
            Toast.makeText(PhotoFullscreenActivity.this,
                    "Unable to show photo due to local file error",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // download photo to local file
        mStorageRef.getFile(localFile).addOnSuccessListener(
                new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(
                            FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.e(TAG, "downloadFile:onSuccess");
                        // Downloaded to local file
                        hideProgressDialog();
                        GlideUtil.loadImage(localFile.getAbsolutePath(),
                                mPhotoFullscreenView);
                        mPhotoFullscreenView.setVisibility(View.VISIBLE);
                        if (mTimer != null) {
                            mTimer.start();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "downloadFile:onFailure:", exception);
                        hideProgressDialog();
                        quitError();
                    }
                });
    }

    // ========================================================
    /* getLocalFileInstance(): create a local file */
    private File getLocalFileInstance() {
        File localFile;
        try {
            File storageDir = PhotoFullscreenActivity.this.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES);
            localFile = File.createTempFile(
                    AppParams.getImageFilename(), ".jpg", storageDir);
        } catch (IOException e) {
            Log.e(TAG, "create file error");
            return null;
        }
        return localFile;
    }

    // ======================================================
    /* Let back key triggers exit app dialog */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            quitOk();
            return true;
        }
        return false;
    }

    // ======================================================
    private void quitOk() {
        Log.d(TAG, "showPhoto:quitOk");
        Intent data = new Intent();
        data.putExtra(EXTRA_PHOTO_FILENAME, mFilename);
        String msgId = getIntent().getStringExtra(EXTRA_MESSAGE_ID);
        if (msgId != null) { /* for use of Firebase Storage */
            data.putExtra(EXTRA_MESSAGE_ID, msgId);
        }
        setResult(RESULT_OK, data);
        finish();
    }

    // ======================================================
    private void quitError() {
        Log.d(TAG, "showPhoto:quitError");
        Intent data = new Intent();
        setResult(RESULT_CANCELED, data);
        finish();
    }

    // ======================================================
}
