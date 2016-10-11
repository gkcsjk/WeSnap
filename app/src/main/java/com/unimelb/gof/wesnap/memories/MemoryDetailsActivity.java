package com.unimelb.gof.wesnap.memories;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by qideng on 10/10/16.
 */
public class MemoryDetailsActivity extends BaseActivity
        implements View.OnClickListener {
    private static final String TAG = "MemoryDetailsActivity";

    public static final String EXTRA_PHOTO_FILENAME = "photo_filename";
    public static final String EXTRA_PHOTO_URI = "photo_uri";

    private String mPhotoUri;
    private String mFilename;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;

    /* UI */
    private ImageView mPhotoFullscreenView;
    private ImageButton mButtonDelete;
    private ImageButton mButtonShare;
    private ImageButton mButtonLock;
    private ImageButton mButtonStory;
    private ImageButton mButtonSend;

    // ======================================================
    /* onCreate() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_details);

        /* photo path */
        mFilename = getIntent().getStringExtra(EXTRA_PHOTO_FILENAME);
        if (mFilename == null) {
            throw new IllegalArgumentException("Must pass EXTRA_PHOTO_FILENAME");
        }
        mPhotoUri = getIntent().getStringExtra(EXTRA_PHOTO_URI);
        if (mPhotoUri == null) {
            throw new IllegalArgumentException("Must pass EXTRA_PHOTO_URI");
        }

        /* Firebase Database & Storage */
        DatabaseReference dbRef = FirebaseUtil.getCurrentMemoriesDatabase();
        StorageReference stRef = FirebaseUtil.getCurrentMemoriesStorage();
        if (dbRef == null || stRef == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            goToLogin("unexpected null value");
            return;
        }
        mDatabaseRef = dbRef.child(mFilename);
        mStorageRef = stRef.child(mFilename);

        /* hide action bar if any */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // [START setting up UI]
        showProgressDialog();

        /* UI: fullscreen photo view */
        mPhotoFullscreenView = (ImageView) findViewById(R.id.image_fullscreen_show_photo);
        Log.d(TAG, "showPhoto:src=" + mPhotoUri);
        GlideUtil.loadImage(mPhotoUri, mPhotoFullscreenView);
        mPhotoFullscreenView.setVisibility(View.VISIBLE);

        /* UI: control buttons */
        mButtonDelete = (ImageButton) findViewById(R.id.bt_delete_memory);
        mButtonDelete.setOnClickListener(this);
        mButtonShare = (ImageButton) findViewById(R.id.bt_share_memory);
        mButtonShare.setOnClickListener(this); // TODO
        mButtonLock = (ImageButton) findViewById(R.id.bt_lock_memory);
        mButtonLock.setOnClickListener(this); // TODO
        mButtonStory = (ImageButton) findViewById(R.id.bt_create_story);
        mButtonStory.setOnClickListener(this); // TODO
        mButtonSend = (ImageButton) findViewById(R.id.bt_send_memory);
        mButtonSend.setOnClickListener(this); // TODO should be "edit" instead of send?

        hideProgressDialog();
        // [END setting up UI]
    }

    // ========================================================
    /* onClick(): perform the requested actions by the clicked buttons */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
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
            case R.id.bt_send_memory:
                sendMemory();
                break;
        }
    }

    // ========================================================
    /* deleteMemory(): delete from Firebase Storage & Database */
    private void deleteMemory() {
        Log.d(TAG, "deleteMemory:filename=" + mFilename);
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
        Log.d(TAG, "shareMemory:filename=" + mFilename);

        // create a local file
        final File localFile = getLocalFileInstance();
        if (localFile == null) {
            Log.e(TAG, "shareMemory:local file error");
            Toast.makeText(MemoryDetailsActivity.this,
                    "Unable to send due to local file error",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO shareMemory via system action
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
        // TODO createStory....
        Toast.makeText(MemoryDetailsActivity.this,
                "createStory() TBD",
                Toast.LENGTH_SHORT).show();
    }

    // ========================================================
    /* sendMemory(): edit & send to a friend as message */
    private void sendMemory() {
        Log.d(TAG, "sendMemory:filename=" + mFilename);

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
                        Log.e(TAG, "downloadFile:filename=" + mFilename);
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
    /* getLocalFileInstance(): create a local file */
    private File getLocalFileInstance() {
        File localFile = null;
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
