package com.unimelb.gof.wesnap.fragment;

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
import com.google.firebase.storage.StorageReference;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.PhotoFullscreenActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.Story;
import com.unimelb.gof.wesnap.stories.MyStoriesActivity;
import com.unimelb.gof.wesnap.stories.OfficialStoriesActivity;
import com.unimelb.gof.wesnap.util.AppParams;
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
    private Button mMyStoriesButton;
    private Button mOfficialStoriesButton;
    private TextView mFriendsStoriesTitle;
    private RecyclerView mFriendsStoriesRecyclerView;
    private FriendsStoriesAdapter mFriendsStoriesRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    /* Firebase Database / Storage variables */
    private DatabaseReference mFriendsStoriesDatabase;
    private DatabaseReference mAllStoriesDatabase;

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

        mFriendsStoriesTitle = (TextView) rootView.findViewById(R.id.text_title_friends_stories);
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

        // Database Refs
        mFriendsStoriesDatabase = FirebaseUtil.getFriendsStoriesDatabase();
        mAllStoriesDatabase = FirebaseUtil.getStoriesDatabase();
        String idCurrentUser = FirebaseUtil.getCurrentUserId();
        if (mFriendsStoriesDatabase == null || idCurrentUser == null ) {
            // null value error out
            Log.e(TAG, "unexpectedly null Database References; goToLogin()");
            (new BaseActivity()).goToLogin("unexpected null value");
            return;
        }
        mFriendsStoriesDatabase = mFriendsStoriesDatabase.child(idCurrentUser);

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mFriendsStoriesRecyclerView.setLayoutManager(mLinearLayoutManager);

        // UI: RecyclerAdapter
        mFriendsStoriesRecyclerAdapter = new FriendsStoriesAdapter(
                getActivity(), mFriendsStoriesDatabase);
        mFriendsStoriesRecyclerView.setAdapter(mFriendsStoriesRecyclerAdapter);

//        // UI: OnScrollListener
//        mFriendsStoriesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dy == mFriendsStoriesRecyclerAdapter.getItemCount()) {
//                    Log.e(TAG,"scroll to last");
//                }
//            }
//        });
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

}
