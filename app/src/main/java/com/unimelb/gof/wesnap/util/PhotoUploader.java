package com.unimelb.gof.wesnap.util;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unimelb.gof.wesnap.BaseActivity;

/**
 * PhotoUploader
 * Provides methods to send photo to friends, save photo as Memories,
 * and create stories via uploading data to Firebase Database and Storage.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class PhotoUploader {
    private static final String TAG = "PhotoUploader";
    private static final String SAVED_TO_MEMORIES = "Saved to memories";
    private static final String FAILED = "Failed to save photo";

    // ======================================================
    /* Upload local file to Firebase Storage "/memories/"*/
    public static void uploadToMemories(final Uri fileUri,
                                        final BaseActivity activity) {
        Log.d(TAG, "uploadToMemories:src:" + fileUri.toString());

        /* Firebase Database / Storage variables */
        final DatabaseReference fMemoriesDatabase =
                FirebaseUtil.getCurrentMemoriesDatabase();
        final StorageReference fMemoriesStorage =
                FirebaseUtil.getCurrentMemoriesStorage();
        final String fCurrentUserId = FirebaseUtil.getCurrentUserId();
        if (fMemoriesDatabase == null || fMemoriesStorage == null
                || fCurrentUserId == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            activity.goToLogin("unexpected null value");
            return;
        }

        /* Photo filename */
        final String filename = fileUri.getLastPathSegment().replaceAll("\\.jpg","");

        /* Upload file to Firebase Storage & Database */
        final StorageReference photoRef = fMemoriesStorage.child(filename);
        Log.d(TAG, "uploadToMemories:dst:" + photoRef.getPath());
        activity.showProgressDialog();
        photoRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Upload succeeded
                                Log.d(TAG, "uploadFromUri:onSuccess");
                                // Get the public download URL (not used!!!)
                                Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                                // Save link to Firebase Database
                                fMemoriesDatabase.child(filename).setValue(true);
                                // Update UI
                                activity.hideProgressDialog();
                                Toast.makeText(activity, SAVED_TO_MEMORIES, Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Upload failed
                                Log.w(TAG, "uploadFromUri:onFailure", exception);
                                // Get the public download URL (not used!!!)
                                Uri downloadUrl = null;
                                // Update UI
                                activity.hideProgressDialog();
                                Toast.makeText(activity, FAILED, Toast.LENGTH_SHORT).show();
                            }
                        });
    }
}
