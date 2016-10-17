package com.unimelb.gof.wesnap.util;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.models.Story;

import java.util.HashMap;

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

    private static final String SAVED_TO_MEMORIES = "Saved to My Memories";
    private static final String FAILED_MEMORIES = "Failed to save your photo";
    private static final String SENT_TO_STORIES = "Sent to My Stories";
    private static final String FAILED_STORIES = "Failed to post your story";

    // ======================================================
    /**
     * Upload local file to Firebase Storage "/memories/"
     * and save the unique filename to database
     * @param localFileUri the local file uri of the photo to be uploaded
     * @param activity the caller activity (for UI feedback)
     * */
    public static void uploadToMemories(final Uri localFileUri,
                                        final BaseActivity activity) {
        Log.d(TAG, "uploadToMemories:src:" + localFileUri.toString());

        /* Firebase Database / Storage variables */
        final DatabaseReference fMemoriesDatabase =
                FirebaseUtil.getMyMemoriesDatabase();
        final StorageReference fMemoriesStorage =
                FirebaseUtil.getMyMemoriesStorage();
        final String fCurrentUserId = FirebaseUtil.getMyUid();
        if (fMemoriesDatabase == null || fMemoriesStorage == null
                || fCurrentUserId == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            activity.goToLogin("unexpected null value");
            return;
        }

        /* Photo filename TODO */
        // final String filename = localFileUri.getLastPathSegment().replaceAll("\\.jpg","");
        final String filename = AppParams.getMyUsername() +"_"+ AppParams.getImageFilename();

        /* Upload file to Firebase Storage & Database */
        final StorageReference photoRef = fMemoriesStorage.child(filename);
        Log.d(TAG, "uploadToMemories:dst:" + photoRef.getPath());
        activity.showProgressDialog();
        photoRef.putFile(localFileUri)
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
                                Toast.makeText(activity, SAVED_TO_MEMORIES,
                                        Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(activity, FAILED_MEMORIES,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    // ======================================================
    /**
     * Get a unique story ID from Firebase Database,
     * upload local file to Firebase Storage "/stories/"
     * and send to friends to inform them about the new story
     * @param localFileUri the local file uri of the photo to be uploaded
     * @param activity the caller activity (for UI feedback)
     * */
    public static void uploadToStories(final Uri localFileUri,
                                       final BaseActivity activity) {
        Log.d(TAG, "uploadToStories:src:" + localFileUri.toString());

        /* Firebase Database / Storage variables */
        final DatabaseReference fStoriesDatabase =
                FirebaseUtil.getStoriesDatabase();
        final StorageReference fStoriesStorage =
                FirebaseUtil.getStoriesStorage();
        final String fCurrentUserId = FirebaseUtil.getMyUid();
        if (fStoriesDatabase == null || fStoriesStorage == null
                || fCurrentUserId == null || AppParams.currentUser == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            activity.goToLogin("unexpected null value");
            return;
        }
        final String fCurrentDisplayedName = AppParams.getMyDisplayedName();

        /* Unique Story ID */
        final String uniqueStoryId = fStoriesDatabase.push().getKey();

        /* Upload file to Firebase Storage & Database */
        final StorageReference photoRef = fStoriesStorage.child(uniqueStoryId);
        Log.d(TAG, "uploadToStories:dst:" + photoRef.getPath());
        activity.showProgressDialog();
        photoRef.putFile(localFileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        // Get the public download URL (not used!!!)
                        Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        if (downloadUrl == null) {
                            // null value error out
                            Log.e(TAG, "downloadUrl=null");
                            return;
                        }

                        // [START publish the photo as a new personal story]
                        // Create a new story using the link
                        final Story newStory = new Story(fCurrentUserId,
                                fCurrentDisplayedName, downloadUrl.toString());

                        // Save story to Firebase Database
                        fStoriesDatabase.child(uniqueStoryId).setValue(newStory);

                        // save as mine
                        FirebaseUtil.getSelfStoriesDatabase()
                                .child(fCurrentUserId)
                                .child(uniqueStoryId)
                                .setValue(newStory.getTimestamp());

                        // share to friends
                        FirebaseUtil.getMyFriendIdsRef()
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d(TAG, "storiesShareToFriends:onDataChange");
                                HashMap<String, Boolean> myFriendIds =
                                        (HashMap<String, Boolean>) dataSnapshot.getValue();
                                for (String friendId : myFriendIds.keySet()) {
                                    FirebaseUtil.getFriendsStoriesDatabase()
                                            .child(friendId)
                                            .child(uniqueStoryId)
                                            .setValue(newStory.getTimestamp());
                                }
                                // Update UI
                                activity.hideProgressDialog();
                                Toast.makeText(activity, SENT_TO_STORIES,
                                        Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "storiesShareToFriends:onCancelled",
                                        databaseError.toException());
                                // Update UI
                                activity.hideProgressDialog();
                                Toast.makeText(activity, FAILED_STORIES,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        // [END publish the photo as a new personal story]
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.w(TAG, "uploadFromUri:onFailure", exception);
                        // Update UI
                        activity.hideProgressDialog();
                        Toast.makeText(activity, FAILED_STORIES, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
