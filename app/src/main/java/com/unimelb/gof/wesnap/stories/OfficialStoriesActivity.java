package com.unimelb.gof.wesnap.stories;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.OfficialStory;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

/**
 * OfficialStoriesActivity
 * Provides UI to show the list of Official Stories
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class OfficialStoriesActivity extends BaseActivity {
    private static final String TAG = "OfficialStoriesActivity";
    private static final int NUM_INTERESTS = 1;

    /* UI Variables */
    private RecyclerView mOfficialStoriesRecyclerView;
    private FirebaseRecyclerAdapter<OfficialStory, OfficialStoryViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database / Storage variables */
    private DatabaseReference mOfficialStoriesDatabase;
    private DatabaseReference mInterestsDatabase;
    private Query mTopInterestsQuery;

    public OfficialStoriesActivity() {
    }

    // ========================================================
    /* onCreated() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_stories);

        // Firebase Refs
        mOfficialStoriesDatabase = FirebaseUtil.getOfficialStoriesDatabase();
        mInterestsDatabase = FirebaseUtil.getUserInterestsRef();
        String idCurrentUser = FirebaseUtil.getCurrentUserId();
        if (mOfficialStoriesDatabase == null || mInterestsDatabase == null
                || idCurrentUser == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            (new BaseActivity()).goToLogin("unexpected null value");
            return;
        }
        mTopInterestsQuery = mInterestsDatabase.orderByValue().limitToLast(NUM_INTERESTS);

        // Add Toolbar to main screen
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_stories);
        setSupportActionBar(mToolbar);

        // Add recycler
        mOfficialStoriesRecyclerView = (RecyclerView) findViewById(R.id.recycler_stories);
        mOfficialStoriesRecyclerView.setTag(TAG);

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(this);
        mOfficialStoriesRecyclerView.setLayoutManager(mLinearLayoutManager);

        // UI: RecyclerAdapter
        setupRecyclerAdapter();
        mOfficialStoriesRecyclerView.setAdapter(mFirebaseAdapter);
    }

    // ========================================================
    /* onCreateOptionsMenu()
     * Inflate the menu: add items to the action bar if it is present */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_official_stories, menu);
        return true;
    }

    /* onOptionsItemSelected()
     * Handle action bar item clicks */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.official_stories_discover:
                discover();
                break;
            case R.id.official_stories_refresh:
                importNewStories();
                break;
            case R.id.official_stories_search:
                search();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // ======================================================
    private void discover() {
        // TODO discover
        // get user's top interests
        mTopInterestsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // empty interest list
                    // TODO show the latest official stories
                    return;
                }
                for (DataSnapshot interestSnapshot: dataSnapshot.getChildren()) {
                    String keyword = interestSnapshot.getKey();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    // ======================================================
    private void importNewStories() {
        // TODO importNewStories
        new GuardianImporter().execute("");
    }

    // ======================================================
    private void search() {
        // TODO search
    }

    // ======================================================
    // ======================================================
    // ======================================================

    /* OfficialStoryViewHolder */
    private static class OfficialStoryViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleView;
        public TextView sourceView;
        public TextView keywordView;

        public OfficialStoryViewHolder(View v) {
            super(v);
            imageView = (ImageView) itemView.findViewById(R.id.story_image_off);
            titleView = (TextView) itemView.findViewById(R.id.story_title_off);
            sourceView = (TextView) itemView.findViewById(R.id.story_source_off);
            keywordView = (TextView) itemView.findViewById(R.id.story_keyword_off);
        }
    }

    // ======================================================
    /* UI: FirebaseRecyclerAdapter */
    private void setupRecyclerAdapter() {
        // [START setting up mFirebaseAdapter]
        mFirebaseAdapter = new FirebaseRecyclerAdapter<OfficialStory, OfficialStoryViewHolder>(
                OfficialStory.class,
                R.layout.item_story_official,
                OfficialStoryViewHolder.class,
                mOfficialStoriesDatabase) {

            @Override
            protected void populateViewHolder(final OfficialStoryViewHolder viewHolder,
                                              final OfficialStory officialStory,
                                              final int position) {
                Log.d(TAG, "populateViewHolder:" + position);

                // load info of official story
                viewHolder.titleView.setText(officialStory.title);
                viewHolder.sourceView.setText(officialStory.source);
                viewHolder.keywordView.setText(getString(
                        R.string.text_story_keyword_var, officialStory.keyword));
                String photoUrl = officialStory.photoUrl;
                if (photoUrl != null && photoUrl.length() != 0) {
                    GlideUtil.loadPhoto(photoUrl, viewHolder.imageView);
                }

                // click to read official story
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "itemClicked:" + officialStory.webpageUrl);

                        // record the click event (register user interest)
                        mInterestsDatabase.child(officialStory.keyword)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Log.d(TAG, "getKeywordTimes:onDataChange:" + dataSnapshot.getKey());
                                        Long times = (Long) dataSnapshot.getValue();
                                        if (times == null) {
                                            times = (long) 0;
                                        }
                                        dataSnapshot.getRef().setValue(times + 1);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "getKeywordTimes:onCancelled",
                                                databaseError.toException());
                                    }
                                });

                        // show content (webpage)
                        Intent showContentIntent = new Intent(
                                OfficialStoriesActivity.this,
                                OfficialStoryDetailsActivity.class);
                        showContentIntent.putExtra(
                                OfficialStoryDetailsActivity.EXTRA_INFO_ARRAY,
                                officialStory.toStringArray());
                        startActivity(showContentIntent);
                    }
                });
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int friendlyMessageCount =
                        mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                // If the recycler view is initially being loaded or
                // the user is at the bottom of the list,
                // scroll to the bottom of the list to show the newly added message.
                if ( lastVisiblePosition == -1 ||
                        ( positionStart >= (friendlyMessageCount - 1)
                                && lastVisiblePosition == (positionStart - 1) ) ) {
                    mOfficialStoriesRecyclerView.scrollToPosition(positionStart);
                }
            }
        });
        // [END setting up mFirebaseAdapter]
    }
    // ======================================================
}
