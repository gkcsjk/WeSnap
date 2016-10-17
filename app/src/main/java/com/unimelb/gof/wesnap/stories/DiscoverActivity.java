package com.unimelb.gof.wesnap.stories;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.OfficialStory;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * DiscoverActivity
 * Provides UI to recommend Official Stories
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class DiscoverActivity extends BaseActivity {
    private static final String TAG = "DiscoverActivity";
    public static final String EXTRA_INTERESTS = "top_interest";

    /* UI Variables */
    private RecyclerView mDiscoverRecyclerView;
    private DiscoverAdapter mDiscoverAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView mNoResultText;

    /* Firebase Database */
    private String mKeyword;
    private DatabaseReference mStoryIdsRef;
    private DatabaseReference mOfficialStoriesDatabase;
    private DatabaseReference mInterestsDatabase;

    public DiscoverActivity() {
    }

    // ========================================================
    /* onCreated() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_stories);

        // get keyword passed over
        mKeyword = getIntent().getStringExtra(EXTRA_INTERESTS);
        if (mKeyword == null) {
            throw new IllegalArgumentException("Must pass EXTRA_INTERESTS");
        }

        // Firebase Refs
        mOfficialStoriesDatabase = FirebaseUtil.getOfficialStoriesDatabase();
        mStoryIdsRef = FirebaseUtil.getKeywordsDatabase().child(mKeyword);

        mInterestsDatabase = FirebaseUtil.getMyInterestKeywordsRef();
        String idCurrentUser = FirebaseUtil.getMyUid();
        if (mInterestsDatabase == null || idCurrentUser == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            (new BaseActivity()).goToLogin("unexpected null value");
            return;
        }

        // Add Toolbar to main screen
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_stories);
        setSupportActionBar(mToolbar);

        // Add recycler
        mDiscoverRecyclerView = (RecyclerView) findViewById(
                R.id.recycler_stories);
        mDiscoverRecyclerView.setTag(TAG);

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(DiscoverActivity.this);
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);
        mDiscoverRecyclerView.setLayoutManager(mLinearLayoutManager);

        // UI: RecyclerAdapter
        mDiscoverAdapter = new DiscoverAdapter(
                DiscoverActivity.this, mStoryIdsRef);
        mDiscoverRecyclerView.setAdapter(mDiscoverAdapter);

        /* No result message */
        mNoResultText = (TextView) findViewById(R.id.text_no_result);
    }

    // ========================================================
    /* onStop(): remove listener */
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        mDiscoverAdapter.cleanupListener();
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
    /* DiscoverAdapter */
    private class DiscoverAdapter
            extends RecyclerView.Adapter<OfficialStoryViewHolder> {

        Context mContext;
        DatabaseReference mDatabaseReference;
        ChildEventListener mChildEventListener;

        List<String> mStoryIds = new ArrayList<>();
        List<OfficialStory> mStories = new ArrayList<>();

        DiscoverAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot,
                                         String previousChildName) {
                    Log.d(TAG, "getOfficialStoryIds:onChildAdded:" +
                            dataSnapshot.getKey());

                    // get official story id
                    final String newOfficialStoryId = dataSnapshot.getKey();
                    // get "keyword/officialStoryId/"
                    mOfficialStoriesDatabase.child(newOfficialStoryId)
                            .addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d(TAG, "getOfficialStory:onDataChange:" +
                                            dataSnapshot.getKey());

                                    if (!dataSnapshot.exists()) {
                                        Log.w(TAG, "getOfficialStory:non-existing:" +
                                                newOfficialStoryId);
                                        mDatabaseReference.child(newOfficialStoryId)
                                                .removeValue();
                                        return;
                                    }

                                    // load data
                                    OfficialStory officialStory = dataSnapshot
                                            .getValue(OfficialStory.class);
                                    // update RecyclerView
                                    mStories.add(officialStory);
                                    mStoryIds.add(newOfficialStoryId);
                                    notifyItemInserted(mStories.size() - 1);
                                    // hide "no result"
                                    mNoResultText.setVisibility(View.GONE);
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "getOfficialStory:onCancelled",
                                            databaseError.toException());
                                }
                            });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot,
                                           String previousChildName) {
                    Log.d(TAG, "getOfficialStoryIds:onChildChanged:" +
                            dataSnapshot.getKey());
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "getOfficialStoryIds:onChildRemoved:" +
                            dataSnapshot.getKey());
                    // get official story id and index
                    String removedId = dataSnapshot.getKey();
                    int index = mStoryIds.indexOf(removedId);
                    if (index > -1) {
                        // Remove data from the list
                        mStoryIds.remove(index);
                        mStories.remove(index);
                        // Update the RecyclerView
                        notifyItemRemoved(index);
                        // show "no result" if empty
                        if (mStories.size() <= 0) {
                            mNoResultText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.w(TAG, "getOfficialStoryIds:unknown_child:" +
                                removedId);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot,
                                         String previousChildName) {
                    Log.d(TAG, "getOfficialStoryIds:onChildMoved:" +
                            dataSnapshot.getKey());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getOfficialStoryIds:onCancelled",
                            databaseError.toException());
                    Toast.makeText(mContext, "Failed to load official story ids",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public OfficialStoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_story_official, parent, false);
            return new OfficialStoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final OfficialStoryViewHolder viewHolder, int position) {
            Log.d(TAG, "populateViewHolder:" + position);

            // Load the item view with thisStory user info
            final int index = position;
            final OfficialStory officialStory = mStories.get(index);

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
                            DiscoverActivity.this,
                            OfficialStoryDetailsActivity.class);
                    showContentIntent.putExtra(
                            OfficialStoryDetailsActivity.EXTRA_INFO_ARRAY,
                            officialStory.toStringArray());
                    startActivity(showContentIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mStories.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
    }

    // ======================================================
}
