package com.unimelb.gof.wesnap;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.chat.ChooseFriendActivity;
import com.unimelb.gof.wesnap.friend.SearchUsernameActivity;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity
 * Provides tab views for each major section.
 * Provides menu options at the action bar.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    /* Variables for the Logged-in User */
    private FirebaseAuth mFirebaseAuth;

    /* UI components */
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private FloatingActionButton mFab;

    // ========================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        /* Get User */
        // Check if user already logged in
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            // if not, direct to Login activity
            Log.d(TAG, "goToLogin");
            goToLogin("User logged out");
            return;
        } else {
            // if yes, get current user info and store a local copy
            ValueEventListener profileListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "getCurrentUser:onDataChange");
                    User currentUser = dataSnapshot.getValue(User.class);
                    if (currentUser != null) {
                        AppParams.currentUser = currentUser;
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getCurrentUser:onCancelled",
                            databaseError.toException());
                    goToLogin("Fail to retrieve current user info.");
                }
            };
            FirebaseUtil.getUsersRef()
                    .child(mFirebaseAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(profileListener);
        }

        /* Render UI */
        // Set up view
        setContentView(R.layout.activity_main);
        // Add Toolbar to main screen
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        // Add tabs to main screen
        this.setupTabs();
        // Add FAB to chat screen: starting a new chat
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        MainActivity.this, ChooseFriendActivity.class);
                startActivity(intent);
            }
        });
    }

    // ========================================================
    /* Set up the tabs in main screen */
    private void setupTabs() {
        // Create MyTabAdapter that contains info of each tab
        MyTabAdapter myTabAdapter =
                new MyTabAdapter(getSupportFragmentManager());
        myTabAdapter.addFragment(
                new ChatsFragment(), "Chats", R.drawable.ic_action_chat);
        myTabAdapter.addFragment(
                new CameraFragment(), "Snap", R.drawable.ic_action_camera);
        myTabAdapter.addFragment(
                new StoriesFragment(), "Stories", R.drawable.ic_action_stories);
        myTabAdapter.addFragment(
                new MeFragment(), "Me", R.drawable.ic_action_me);

        // Set ViewPager for each Tabs
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(myTabAdapter);

        // Set Tabs with TabLayout
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        // Set the title and icon for each tab
        for (int i = 0; i < myTabAdapter.getCount(); i++) {
            TabLayout.Tab mTab = mTabLayout.getTabAt(i);
            mTab.setCustomView(myTabAdapter.getTabView(i));
        }

        // Set diff FAB / AppBar actions
        mTabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        mViewPager.setCurrentItem(tab.getPosition());
                        mFab.clearAnimation();
                        if (mTabLayout.getSelectedTabPosition() == 0) {
                            // for chats screen
                            mFab.show();
                        } else {
                            mFab.hide();
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                    }
                });
    }

    // ========================================================
    /* MyTabAdapter */
    private class MyTabAdapter extends FragmentPagerAdapter {//FragmentStatePagerAdapter
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private final List<Integer> mFragmentIconList = new ArrayList<>();

        public MyTabAdapter(FragmentManager manager) {
            super(manager);
        }

        public void addFragment(Fragment fragment, String title, int icon) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            mFragmentIconList.add(icon);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public int getPageIcon(int position) {
            return mFragmentIconList.get(position);
        }

        /* returns the view for the tab at the given position */
        public View getTabView(int position) {
            View view = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.item_tab, null);
            TextView mTabTitle = (TextView) view.findViewById(R.id.title_tab);
            mTabTitle.setText(getPageTitle(position));
            ImageView mTabIcon = (ImageView) view.findViewById(R.id.icon_tab);
            mTabIcon.setImageResource(getPageIcon(position));
            return view;
        }
    }

    // ========================================================
    /* onCreateOptionsMenu()
     * Inflate the menu: add items to the action bar if it is present */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* onOptionsItemSelected()
     * Handle action bar item clicks */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.app_bar_search:
                search();
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.exit:
                showExitAppDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ========================================================
    /* Search button on app bar */
    private void search() {
        Log.d(TAG, "search:username");
        Intent intent = new Intent(
                MainActivity.this, SearchUsernameActivity.class);
        startActivity(intent);
    }

    // ========================================================
    /* Logout from Firebase */
    private void logout() {
        Log.d(TAG, "logout");

        // Logout
        if (mFirebaseAuth != null) {
            mFirebaseAuth.signOut();
        }

        // Reset local copy of user info
        AppParams.currentUser = null;

        // Restart from LoginActivity
        goToLogin("user logged out");
    }

    // ========================================================
    /* Let back key triggers exit app dialog */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Exit the app after confirming via dialog;
            // only show the Dialog when the activity is not finished
            if (!isFinishing()) {
                showExitAppDialog();
            }
            return true;
        }
        return false;
    }

    // ========================================================
}
