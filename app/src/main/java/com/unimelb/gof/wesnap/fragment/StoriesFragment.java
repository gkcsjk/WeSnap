package com.unimelb.gof.wesnap.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.Story;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * StoriesFragment
 * This fragment provides UI for stories.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class StoriesFragment extends Fragment {
    private static final String TAG = "StoriesFragment";

    /* UI Variables */
    private View mGroupMyStories;
    private TextView mTitleMyStories;
    private RecyclerView mMyStoriesRecyclerView;
    private MyStoriesAdapter mMyStoriesRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database variables */
    // My Stories
    private DatabaseReference mMyStoriesDatabaseRef;
    // Friends' Stories
    private DatabaseReference mFriendsStoriesDatabaseRef;

    public StoriesFragment() {
    }

    // ======================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stories, container, false);

        mGroupMyStories = rootView.findViewById(R.id.ui_group_my);
        mGroupMyStories.setVisibility(View.GONE);
        mTitleMyStories = (TextView) rootView.findViewById(R.id.text_title_my_stories);
        mMyStoriesRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_stories_my);
        mMyStoriesRecyclerView.setTag(TAG);

        return rootView;
    }

    // ========================================================
    /* onActivityCreated() */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        // Database Refs
        mMyStoriesDatabaseRef = FirebaseUtil.getMyStoriesDatabase();
        mFriendsStoriesDatabaseRef = FirebaseUtil.getFriendsStoriesDatabase();
        String uid = FirebaseUtil.getCurrentUserId();
        if (mMyStoriesDatabaseRef == null || mFriendsStoriesDatabaseRef == null
                || uid == null ) {
            // null value error out
            Log.e(TAG, "unexpectedly null Database References; goToLogin()");
            (new BaseActivity()).goToLogin("unexpected null value");
            return;
        }
        mMyStoriesDatabaseRef = mMyStoriesDatabaseRef.child(uid);
        mFriendsStoriesDatabaseRef = mFriendsStoriesDatabaseRef.child(uid);

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);
        mMyStoriesRecyclerView.setLayoutManager(mLinearLayoutManager);

        // UI: RecyclerAdapter
        mMyStoriesRecyclerAdapter = new MyStoriesAdapter(getActivity(), mMyStoriesDatabaseRef);
        mMyStoriesRecyclerView.setAdapter(mMyStoriesRecyclerAdapter);
    }

    // ======================================================
    // ======================================================
    // ======================================================
    /* MyStoryViewHolder */
    public static class MyStoryViewHolder extends RecyclerView.ViewHolder {
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
    public class MyStoriesAdapter
            extends RecyclerView.Adapter<MyStoryViewHolder> {

        private final DatabaseReference mStoriesDatabase =
                FirebaseUtil.getStoriesDatabase();

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
                    // check if expired
//                    long newStoryTimestamp = (long) dataSnapshot.getValue();
//                    long diffHours = (System.currentTimeMillis() - newStoryTimestamp) / Story.MILLISECONDS_IN_ONE_HOUR;
//                    if (diffHours >= Story.HOURS_TO_LIVE) {
//                        // TODO
//                        // expired: the story was published 24 hours ago
//                        dataSnapshot.getRef().removeValue();
//                        return;
//                    }
                    // get "stories/storyId/"
                    mStoriesDatabase.child(newStoryId)
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
                                    mGroupMyStories.setVisibility(View.VISIBLE);
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
                        if (mStories.size() < 1) {
                            mGroupMyStories.setVisibility(View.GONE);
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
            String photoUrl = story.getPhotoUrl();
            Log.d(TAG, "onBindViewHolder:photoUrl=" + photoUrl);
            if (photoUrl != null && photoUrl.length() != 0) {
                GlideUtil.loadImage(photoUrl, viewHolder.imageView);
            }

            // on click: directs to message list
            viewHolder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.w(TAG, "onClick");
                            // TODO show story photo fullscreen?
//                            Intent intent = new Intent(mContent, MessagesActivity.class);
//                            intent.putExtra(
//                                    MessagesActivity.EXTRA_story_ID,
//                                    mStoryIds.get(position));
//                            intent.putExtra(
//                                    MessagesActivity.EXTRA_story_TITLE,
//                                    viewHolder.titleView.getText().toString());
//                            context.startActivity(intent);
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
}
