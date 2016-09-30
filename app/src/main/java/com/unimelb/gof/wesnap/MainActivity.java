package com.unimelb.gof.wesnap;

import android.content.Intent;
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

import com.unimelb.gof.wesnap.fragment.CameraFragment;
import com.unimelb.gof.wesnap.fragment.ChatsFragment;
import com.unimelb.gof.wesnap.fragment.FriendsFragment;
import com.unimelb.gof.wesnap.friend.AddFriendChooserActivity;
import com.unimelb.gof.wesnap.util.CurrentUserParams;

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
    private FirebaseUser mFirebaseUser;

    // ========================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        /* Get User */
        // Check if user already logged in; if not, direct to Login activity
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            Log.d(TAG, "goToLogin");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        // Set up CurrentUserParams
        CurrentUserParams.getInstance();

        /* Render UI */
        // Set up view
        setContentView(R.layout.activity_main);
        // Add Toolbar to main screen
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        // Add tabs to main screen
        this.setupTabs();

        // Firebase
        // TODO
    }

    // ========================================================
    /* Set up the tabs in main screen */
    private void setupTabs() {
        // Create MyTabAdapter that contains info of each tab
        MyTabAdapter myTabAdapter = new MyTabAdapter(getSupportFragmentManager());
        myTabAdapter.addFragment(new FriendsFragment(), "Friends", R.drawable.ic_action_friends);
        myTabAdapter.addFragment(new ChatsFragment(), "Chats", R.drawable.ic_action_chat);
        myTabAdapter.addFragment(new CameraFragment(), "Snap", R.drawable.ic_action_camera);
        myTabAdapter.addFragment(new CameraFragment(), "Stories", R.drawable.ic_action_stories);
        myTabAdapter.addFragment(new CameraFragment(), "Me", R.drawable.ic_action_me);

        // Set ViewPager for each Tabs
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(myTabAdapter);

        // Set Tabs with TabLayout
        TabLayout mTabs = (TabLayout) findViewById(R.id.tabs);
        mTabs.setupWithViewPager(mViewPager);

        // Set the title and icon for each tab
        for (int i = 0; i < myTabAdapter.getCount(); i++) {
            TabLayout.Tab mTab = mTabs.getTabAt(i);
            mTab.setCustomView(myTabAdapter.getTabView(i));
        }
    }

    /* MyTabAdapter */
    private class MyTabAdapter extends FragmentPagerAdapter {
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
            case R.id.add_friend:
                Intent intent = new Intent(MainActivity.this, AddFriendChooserActivity.class);
                startActivity(intent);
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
    /* Logout from Firebase */
    private void logout() {
        Log.d(TAG, "logout");

        // Logout
        if(mFirebaseAuth != null) {
            mFirebaseAuth.signOut();
        }

        // Reset current user params
        CurrentUserParams.resetCurrentUserParams();

        // Restart from LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
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
}
