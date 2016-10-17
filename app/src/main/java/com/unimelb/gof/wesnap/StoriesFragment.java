package com.unimelb.gof.wesnap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.models.OfficialStory;
import com.unimelb.gof.wesnap.models.Story;
import com.unimelb.gof.wesnap.stories.MyStoriesActivity;
import com.unimelb.gof.wesnap.stories.OfficialStoriesActivity;
import com.unimelb.gof.wesnap.stories.OfficialStoryDetailsActivity;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StoriesFragment
 * This fragment provides UI for the "stories" tab.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class StoriesFragment extends Fragment {
    private static final String TAG = "StoriesFragment";

    /* UI Variables */
    private Button mMyStoriesButton;
    private Button mOfficialStoriesButton;
    // UI group: Subscription
    private View mGroupSub;
    private RecyclerView mSubscriptionRecyclerView;
    private SubscriptionAdapter mSubscriptionRecyclerAdapter;
    private LinearLayoutManager mHorizontalLinearLayoutManager;
    // UI group: Friends Stories
    private RecyclerView mFriendsStoriesRecyclerView;
    private FriendsStoriesAdapter mFriendsStoriesRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database variables */
    // Friends stories
    private DatabaseReference mFriendsStoriesDatabase;
    private DatabaseReference mAllStoriesDatabase;
    // Subscription
    private DatabaseReference mOfficialStoriesDatabase;
    private DatabaseReference mInterestsDatabase;
    private DatabaseReference mSubscriptionDatabase;
    private ValueEventListener mSubscriptionListener;

    /* List of subscribed keywords */
    public Set<String> mSubscriptionKeywords = null;

    public StoriesFragment() {
    }

    // ======================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stories, container, false);

        mMyStoriesButton = (Button) rootView.findViewById(R.id.button_my_stories);
        mMyStoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show my stories
                Intent viewMyStoriesIntent = new Intent(
                        getActivity(), MyStoriesActivity.class);
                startActivity(viewMyStoriesIntent);
            }
        });

        mOfficialStoriesButton = (Button) rootView.findViewById(R.id.button_official_stories);
        mOfficialStoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show official stories
                Intent viewOfficialStoriesIntent = new Intent(
                        getActivity(), OfficialStoriesActivity.class);
                startActivity(viewOfficialStoriesIntent);
            }
        });

        /* UI group: Subscription */
        mGroupSub = rootView.findViewById(R.id.ui_group_subscriptions);
        mGroupSub.setVisibility(View.GONE);
        TextView textSubscriptionTitle = (TextView) rootView.findViewById(
                R.id.text_title_subscriptions);
        mSubscriptionRecyclerView = (RecyclerView) rootView.findViewById(
                R.id.recycler_subscriptions);
        mSubscriptionRecyclerView.setTag(TAG);

        /* UI group: Friends Stories */
        TextView textFriendsStoriesTitle = (TextView) rootView.findViewById(
                R.id.text_title_friends_stories);
        mFriendsStoriesRecyclerView = (RecyclerView) rootView.findViewById(
                R.id.recycler_friends_stories);
        mFriendsStoriesRecyclerView.setTag(TAG);

        return rootView;
    }

    // ========================================================
    /* onActivityCreated() */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        /* Firebase Database Reference */
        mFriendsStoriesDatabase = FirebaseUtil.getFriendsStoriesDatabase();
        mSubscriptionDatabase = FirebaseUtil.getMySubscriptionKeywordsRef();
        mInterestsDatabase = FirebaseUtil.getMyInterestKeywordsRef();
        String idCurrentUser = FirebaseUtil.getMyUid();
        if (mFriendsStoriesDatabase == null || mSubscriptionDatabase == null
                || mInterestsDatabase == null || idCurrentUser == null ) {
            // null value error out
            Log.e(TAG, "unexpectedly null Database References; goToLogin()");
            (new BaseActivity()).goToLogin("unexpected null value");
            return;
        }

        mFriendsStoriesDatabase = mFriendsStoriesDatabase.child(idCurrentUser);
        mAllStoriesDatabase = FirebaseUtil.getStoriesDatabase();
        mOfficialStoriesDatabase = FirebaseUtil.getOfficialStoriesDatabase();

        /*------------------------------------------------*/
        /* for friends stories */
        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mFriendsStoriesRecyclerView.setLayoutManager(mLinearLayoutManager);
        // UI: RecyclerAdapter
        mFriendsStoriesRecyclerAdapter = new FriendsStoriesAdapter(
                getActivity(), mFriendsStoriesDatabase);
        mFriendsStoriesRecyclerView.setAdapter(mFriendsStoriesRecyclerAdapter);

        /*------------------------------------------------*/
        /* for subscription list */
        // UI: LinearLayoutManager
        mHorizontalLinearLayoutManager = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mHorizontalLinearLayoutManager.setStackFromEnd(false);
        mSubscriptionRecyclerView.setLayoutManager(mHorizontalLinearLayoutManager);
        // UI: RecyclerAdapter
        mSubscriptionRecyclerAdapter = new SubscriptionAdapter(
                getActivity(), mOfficialStoriesDatabase);
        mSubscriptionRecyclerView.setAdapter(mSubscriptionRecyclerAdapter);

        // get my list of subscribed keywords
        mSubscriptionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "mSubscriptionListener:onDataChange");
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                if (map != null) { // show view
                    mSubscriptionKeywords = map.keySet();
                    mGroupSub.setVisibility(View.VISIBLE);
                    mSubscriptionRecyclerAdapter.resetListener();
                    Log.d(TAG, "mSubscriptionListener:onDataChange:list=" +
                            mSubscriptionKeywords.toString());
                } else { // hide view
                    mSubscriptionKeywords = null;
                    mGroupSub.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "mSubscriptionListener:onCancelled", databaseError.toException());
            }
        };
        mSubscriptionDatabase.addValueEventListener(mSubscriptionListener);
    }

    // ======================================================
    // ======================================================
    // ======================================================
    /* FriendStoryViewHolder */
    private static class FriendStoryViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView timeView;
        public TextView nameView;

        public FriendStoryViewHolder(View v) {
            super(v);
            imageView = (ImageView) itemView.findViewById(R.id.story_image_fd);
            timeView = (TextView) itemView.findViewById(R.id.story_time_fd);
            nameView = (TextView) itemView.findViewById(R.id.story_name_fd);
        }
    }

    // ======================================================
    /* FriendsStoriesAdapter */
    public class FriendsStoriesAdapter
            extends RecyclerView.Adapter<FriendStoryViewHolder> {

        private Context mContext; // from the calling activity
        private DatabaseReference mListRef; // list of items we need to monitor
        private ChildEventListener mChildEventListener; // listener for the item list

        private List<String> mStoryIds = new ArrayList<>();
        private List<Story> mStories = new ArrayList<>();

        public FriendsStoriesAdapter(final Context context, DatabaseReference ref) {
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

                    // check if expired
                    long newStoryTimestamp = (long) dataSnapshot.getValue();
                    long diffHours = (System.currentTimeMillis() - newStoryTimestamp)
                            / Story.MILLISECONDS_IN_ONE_HOUR;
                    if (diffHours >= Story.HOURS_TO_LIVE) {
                        // expired: the story was published 24 hours ago
                        dataSnapshot.getRef().removeValue();
                        return;
                    }

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
                                    // update RecyclerView
                                    mStories.add(story);
                                    mStoryIds.add(newStoryId);
                                    notifyItemInserted(mStories.size() - 1);
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
        public FriendStoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_story_friends, parent, false);
            return new FriendStoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final FriendStoryViewHolder viewHolder,
                                     final int position) {
            Log.d(TAG, "onBindViewHolder:" + position);
            final Story story = mStories.get(position);

            viewHolder.nameView.setText(mContext.getString(
                    R.string.text_author_name, story.getAuthorName()));

            if (story.getDiffHours() >= 2) {
                viewHolder.timeView.setText(mContext.getString(
                        R.string.text_hours_ago, story.getDiffHours()));
            } else {
                viewHolder.timeView.setText(mContext.getString(
                        R.string.text_just_now));
            }

            final String photoUrl = story.getPhotoUrl();
            if (photoUrl != null && photoUrl.length() != 0) {
                GlideUtil.loadPhoto(photoUrl, viewHolder.imageView);
            }

            // on click: directs to message list
            viewHolder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // show story photo fullscreen
                            Intent showPhotoIntent = new Intent(getActivity(), PhotoFullscreenActivity.class);
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

//        public void cleanupListener() {
//            if (mChildEventListener != null) {
//                mListRef.removeEventListener(mChildEventListener);
//            }
//        }
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
    /* SubscriptionAdapter */
    private class SubscriptionAdapter
            extends RecyclerView.Adapter<OfficialStoryViewHolder> {

        Context mContext;
        DatabaseReference mDatabaseReference;
        ChildEventListener mChildEventListener;

        List<String> mStoryIds = new ArrayList<>();
        List<OfficialStory> mStories = new ArrayList<>();

        SubscriptionAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getOfficialStory:onChildAdded:" + dataSnapshot.getKey());
                    // load data
                    OfficialStory officialStory =
                            dataSnapshot.getValue(OfficialStory.class);
                    // check subscription
                    if (mSubscriptionKeywords != null
                            && mSubscriptionKeywords.contains(officialStory.keyword)) {
                        // update RecyclerView
                        mStories.add(officialStory);
                        mStoryIds.add(dataSnapshot.getKey());
                        notifyItemInserted(mStories.size() - 1);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getOfficialStoryIds:onChildChanged:" + dataSnapshot.getKey());
                    Toast.makeText(mContext, "Changed:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "getOfficialStoryIds:onChildRemoved:" + dataSnapshot.getKey());
                    // get official story id and index
                    String removedId = dataSnapshot.getKey();
                    int index = mStoryIds.indexOf(removedId);
                    if (index > -1) { // if shown:
                        // Remove data from the list
                        mStoryIds.remove(index);
                        mStories.remove(index);
                        // Update the RecyclerView
                        notifyItemRemoved(index);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getOfficialStoryIds:onChildMoved:" + dataSnapshot.getKey());
                    // This method is triggered when a child location's priority changes.
                    Toast.makeText(mContext, "Moved:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getOfficialStoryIds:onCancelled", databaseError.toException());
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
            View view = inflater.inflate(R.layout.item_story_official_subscription, parent, false);
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
                                    Log.d(TAG, "getKeywordTimes:onDataChange:" +
                                            dataSnapshot.getKey());
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

                    // show story
                    Intent showContentIntent = new Intent(
                            getActivity(),
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

        public void resetListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
                mDatabaseReference.addChildEventListener(mChildEventListener);
            }
        }
    }

    // ======================================================
}
