package com.unimelb.gof.wesnap.stories;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.PhotoFullscreenActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.Story;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * MyStoriesActivity
 * Provides UI to show the list of My Stories
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class MyStoriesActivity extends BaseActivity {
    private static final String TAG = "MyStoriesActivity";

    /* UI Variables */
    private RecyclerView mMyStoriesRecyclerView;
    private MyStoriesAdapter mRecyclerAdapter;
    private TextView mNoResultText;

    /* Firebase Database / Storage variables */
    private DatabaseReference mMyStoriesDatabase;
    private DatabaseReference mAllStoriesDatabase;
    private StorageReference mAllStoriesStorage;

    public MyStoriesActivity() {
    }

    // ========================================================
    /* onCreated() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_stories);

        // Firebase Refs
        mMyStoriesDatabase = FirebaseUtil.getSelfStoriesDatabase();
        mAllStoriesDatabase = FirebaseUtil.getStoriesDatabase();
        mAllStoriesStorage = FirebaseUtil.getStoriesStorage();
        String idCurrentUser = FirebaseUtil.getMyUid();
        if (mMyStoriesDatabase == null || mAllStoriesDatabase== null
                || mAllStoriesStorage == null || idCurrentUser == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            (new BaseActivity()).goToLogin("unexpected null value");
            return;
        }
        mMyStoriesDatabase = mMyStoriesDatabase.child(idCurrentUser);

        // Add Toolbar to main screen
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_stories);
        setSupportActionBar(mToolbar);

        // Add recycler
        mMyStoriesRecyclerView = (RecyclerView) findViewById(R.id.recycler_stories);
        mMyStoriesRecyclerView.setTag(TAG);

        // UI: GridLayoutManager
        int tilePadding = getResources().getDimensionPixelSize(R.dimen.size_edges_small);
        mMyStoriesRecyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
        mMyStoriesRecyclerView.setLayoutManager(new GridLayoutManager(MyStoriesActivity.this, 2));

        // UI: RecyclerAdapter
        mRecyclerAdapter = new MyStoriesAdapter(MyStoriesActivity.this, mMyStoriesDatabase);
        mMyStoriesRecyclerView.setAdapter(mRecyclerAdapter);

        /* No result message */
        mNoResultText = (TextView) findViewById(R.id.text_no_result);
    }

    // ========================================================
    /* Remove listener */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Exit the app after confirming via dialog;
            // only show the Dialog when the activity is not finished
            Log.d(TAG, "onKeyDown");
            if (!isFinishing()) {
                mRecyclerAdapter.cleanupListener();
                finish();
            }
            return true;
        }
        return false;
    }

    // ======================================================
    // ======================================================
    // ======================================================
    /* FriendStoryViewHolder */
    private static class MyStoryViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView timeView;

        public MyStoryViewHolder(View v) {
            super(v);
            imageView = (ImageView) itemView.findViewById(R.id.story_image_my);
            timeView = (TextView) itemView.findViewById(R.id.story_time_my);
        }
    }

    // ======================================================
    /* MyStoriesAdapter */
    private class MyStoriesAdapter
            extends RecyclerView.Adapter<MyStoryViewHolder> {

        private Context mContext; // from the calling activity
        private DatabaseReference mListRef; // list of items we need to monitor
        private ChildEventListener mChildEventListener; // listener for the item list

        private List<String> mStoryIds = new ArrayList<>();
        private List<Story> mStories = new ArrayList<>();

        public MyStoriesAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mListRef = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    // get storyId:storyTimestamp
                    final String newStoryId = dataSnapshot.getKey();
                    Log.d(TAG, "getStoryIds:onChildAdded:" + newStoryId);

                    // get "stories/storyId/"
                    mAllStoriesDatabase.child(newStoryId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // check if exists
                                    if (!dataSnapshot.exists()) {
                                        Log.w(TAG, "getStory:Non-Existing:id=" + newStoryId);
                                        mListRef.child(newStoryId).removeValue();
                                        return;
                                    }
                                    Log.d(TAG, "getStory:onDataChange:" + dataSnapshot.getKey());
                                    // load story data
                                    Story story = dataSnapshot.getValue(Story.class);
                                    // check if expired
                                    if (story.isExpired()) {
                                        dataSnapshot.getRef().removeValue();
                                        return;
                                    }
                                    // update RecyclerView
                                    mStories.add(story);
                                    mStoryIds.add(newStoryId);
                                    notifyItemInserted(mStories.size() - 1);
                                    // hide "no result"
                                    mNoResultText.setVisibility(View.GONE);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "getStory:onCancelled", databaseError.toException());
                                }
                            });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getStoryIds:onChildChanged:" + dataSnapshot.getKey());
                    Toast.makeText(mContext, "Changed:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    // get story id and index
                    String removedStoryId = dataSnapshot.getKey();
                    Log.d(TAG, "getStoryIds:onChildRemoved:" + removedStoryId);
                    // find story in view
                    int storyIndex = mStoryIds.indexOf(removedStoryId);
                    if (storyIndex > -1) {
                        // Remove data from the list
                        mStoryIds.remove(storyIndex);
                        mStories.remove(storyIndex);
                        // Update the RecyclerView
                        notifyItemRemoved(storyIndex);
                        // show "no result" if empty
                        if (mStories.size() <= 0) {
                            mNoResultText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.w(TAG, "getStoryIds:onChildRemoved:unknown_id=" + removedStoryId);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getStoryIds:onChildMoved:" + dataSnapshot.getKey());
                    // This method is triggered when a child location's priority changes.
                    Toast.makeText(mContext, "Moved:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getstoryIds:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load friends.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public MyStoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_story_my, parent, false);
            return new MyStoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyStoryViewHolder viewHolder,
                                     final int position) {
            Log.d(TAG, "onBindViewHolder:" + position);
            final Story story = mStories.get(position);

            if (story.getDiffHours() >= 2) {
                viewHolder.timeView.setText(mContext.getString(
                        R.string.text_hours_ago, story.getDiffHours()));
            } else {
                viewHolder.timeView.setText(mContext.getString(
                        R.string.text_just_now));
            }

            final String photoUrl = story.getPhotoUrl();
            if (photoUrl != null && photoUrl.length() != 0) {
                GlideUtil.loadImage(photoUrl, viewHolder.imageView);
            }

            // on click: directs to message list
            viewHolder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // show story photo fullscreen
                            Intent showPhotoIntent = new Intent(MyStoriesActivity.this, PhotoFullscreenActivity.class);
                            showPhotoIntent.putExtra(PhotoFullscreenActivity.EXTRA_PHOTO_URI_STRING, photoUrl);
                            showPhotoIntent.putExtra(PhotoFullscreenActivity.EXTRA_TIME_TO_LIVE, AppParams.NO_TTL);
                            startActivity(showPhotoIntent);
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return mStories.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mListRef.removeEventListener(mChildEventListener);
            }
        }
    }

    // ======================================================
}
