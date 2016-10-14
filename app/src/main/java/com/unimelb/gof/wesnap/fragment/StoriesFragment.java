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
import com.unimelb.gof.wesnap.stories.MyStoriesActivity;
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
//    private View mGroupMyStories;
//    private TextView mTitleMyStories;
//    private RecyclerView mMyStoriesRecyclerView;
//    private MyStoriesAdapter mMyStoriesRecyclerAdapter;
//    private LinearLayoutManager mLinearLayoutManager;

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
        mMyStoriesButton = (Button) rootView.findViewById(R.id.button_my_stories);
        mMyStoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewMyStoriesIntent = new Intent(getActivity(), MyStoriesActivity.class);
                startActivity(viewMyStoriesIntent);
            }
        });

        mOfficialStoriesButton = (Button) rootView.findViewById(R.id.button_official_stories);
        mOfficialStoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO show official stories
                Toast.makeText(getActivity(), "item clicked",
                        Toast.LENGTH_SHORT).show();
            }
        });

//        mGroupMyStories = rootView.findViewById(R.id.ui_group_my);
//        mGroupMyStories.setVisibility(View.GONE);
//        mTitleMyStories = (TextView) rootView.findViewById(R.id.text_title_my_stories);
//        mMyStoriesRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_stories_my);
//        mMyStoriesRecyclerView.setTag(TAG);

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

//        // UI: LinearLayoutManager
//        mLinearLayoutManager = new LinearLayoutManager(getActivity());
//        mLinearLayoutManager.setReverseLayout(false);
//        mLinearLayoutManager.setStackFromEnd(false);
//        mMyStoriesRecyclerView.setLayoutManager(mLinearLayoutManager);
//
//        // UI: RecyclerAdapter
//        mMyStoriesRecyclerAdapter = new MyStoriesAdapter(getActivity(), mMyStoriesDatabaseRef);
//        mMyStoriesRecyclerView.setAdapter(mMyStoriesRecyclerAdapter);
    }


}
