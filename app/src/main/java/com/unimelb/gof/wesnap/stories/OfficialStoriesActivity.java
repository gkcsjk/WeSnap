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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Random;

/**
 * OfficialStoriesActivity
 * Provides UI to show the list of Official Stories
 * TODO possible improvement: more than one interests
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
    private View mGroupSearch;
    private EditText mSearchInput;
    private ImageButton mSearchSubmit;
    private TextView mNoResultText;

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
        mInterestsDatabase = FirebaseUtil.getMyInterestKeywordsRef();
        String idCurrentUser = FirebaseUtil.getMyUid();
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
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);
        mOfficialStoriesRecyclerView.setLayoutManager(mLinearLayoutManager);
        // UI: RecyclerAdapter
        setupRecyclerAdapter();
        mOfficialStoriesRecyclerView.setAdapter(mFirebaseAdapter);

        // Add showSearchInput box
        mGroupSearch = findViewById(R.id.ui_group_search);
        mGroupSearch.setVisibility(View.GONE);
        mSearchInput = (EditText) findViewById(R.id.field_search_official);
        mSearchSubmit = (ImageButton) findViewById(R.id.button_submit_search_official);
        mSearchSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = mSearchInput.getText().toString();
                if (searchTerm.length() == 0) {
                    mSearchInput.setError("no showSearchInput keyword");
                } else { // go to DiscoverActivity TODO non-existing keywords?
                    Intent showDiscoverIntent = new Intent(
                            OfficialStoriesActivity.this, DiscoverActivity.class);
                    showDiscoverIntent.putExtra(DiscoverActivity.EXTRA_INTERESTS, searchTerm);
                    startActivity(showDiscoverIntent);
                }
            }
        });
        // No result message
        mNoResultText = (TextView) findViewById(R.id.text_no_result);
        mNoResultText.setVisibility(View.GONE);
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
                showSearchInput();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // ======================================================
    private void discover() {
        Log.d(TAG, "discover");
        // get user's top interests
        mTopInterestsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "loadTopInterests:onDataChange");

                if (!dataSnapshot.exists()) {
                    Toast.makeText(OfficialStoriesActivity.this,
                            "Discover: random", Toast.LENGTH_SHORT).show();

                    // get all existing keywords from Database
                    FirebaseUtil.getKeywordsDatabase()
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "loadAllKeywords:onDataChange");
                            if (!dataSnapshot.exists()) {
                                (new GuardianImporter(null)).execute("");
                            } else {
                                Intent showDiscoverIntent = new Intent(
                                        OfficialStoriesActivity.this, DiscoverActivity.class);
                                // randomly choose one to present to user
                                int index = (new Random()).nextInt(
                                        (int) dataSnapshot.getChildrenCount());
                                int i = 0;
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (i == index) {
                                        showDiscoverIntent.putExtra(
                                                DiscoverActivity.EXTRA_INTERESTS, child.getKey());
                                        startActivity(showDiscoverIntent);
                                        return;
                                    }
                                    i++;
                                }
                            }
                            Toast.makeText(OfficialStoriesActivity.this,
                                    "Discover: nothing to show", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "loadAllKeywords:onCancelled", databaseError.toException());
                        }
                    });
                } else {
                    Toast.makeText(OfficialStoriesActivity.this,
                            "Discover: for you", Toast.LENGTH_SHORT).show();

                    // get the top NUM_INTERESTS interests
                    for (DataSnapshot interestSnapshot : dataSnapshot.getChildren()) {
                        String keyword = interestSnapshot.getKey();
                        Log.d(TAG, "loadKeyword:onDataChange:" + keyword);

                        // go to DiscoverActivity
                        if (NUM_INTERESTS == 1) {
                            Intent showDiscoverIntent = new Intent(
                                    OfficialStoriesActivity.this, DiscoverActivity.class);
                            showDiscoverIntent.putExtra(DiscoverActivity.EXTRA_INTERESTS, keyword);
                            startActivity(showDiscoverIntent);
                        } // else: TODO possible improvement: more than one interests
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadTopInterests:onCancelled", databaseError.toException());
            }
        });
    }

    // ======================================================
    /* Obtain new official stories via Guardian API, and
     * save to Firebase Database as OfficialStory items */
    private void importNewStories() {
        Log.d(TAG, "importNewStories");
        Toast.makeText(OfficialStoriesActivity.this,
                "import new stories!", Toast.LENGTH_SHORT).show();

        // check last time we scrape to avoid duplicated items,
        // and then run the importer(s)
        FirebaseUtil.getLastImportTimeRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w(TAG, "getLastImport:onDataChange");
                        String lastImport = (String) dataSnapshot.getValue();
                        (new GuardianImporter(lastImport)).execute("");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getLastImport:onCancelled", databaseError.toException());
                    }
                });
    }

    // ======================================================
    private void showSearchInput() {
        Log.d(TAG, "searchOfficialStories");
        if (mGroupSearch.getVisibility() == View.VISIBLE) {
            mGroupSearch.setVisibility(View.GONE);
        } else {
            mGroupSearch.setVisibility(View.VISIBLE);
        }
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
