package com.unimelb.gof.wesnap;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

/**
 * TODO add comments: MainActivity
 */
public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    /* Variables for the Logged-in User */
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    /* Settings for the Tabs */
    private static final int TAB_NUM = 5;
    private static final String[] TAB_TITLES = new String[] {
            "Friends",
            "Chat",
            "Snap",
            "Stories",
            "Me"
    };
    private static final Fragment[] TAB_FRAGMENTS = new Fragment[] {
            FriendsFragment.getInstance(),
            ChatsFragment.getInstance(),
            CameraFragment.getInstance(),
            new CameraFragment(),   // TODO
            new CameraFragment()    // TODO
    };
    private static final int[] TAB_ICONS_INT = {
            R.drawable.ic_action_friends,
            R.drawable.ic_action_chat,
            R.drawable.ic_action_camera,
            R.drawable.ic_action_stories,
            R.drawable.ic_action_me
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user already logged in
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            // If not, direct to Login activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

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
    /**
     * Set up the tabs in main screen
     * */
    private void setupTabs() {
        // Create MyTabAdapter that contains info of each tab
        MyTabAdapter myTabAdapter =
                new MyTabAdapter(getSupportFragmentManager());

        // Setup ViewPager for each Tabs
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(myTabAdapter);

        // Setup Tabs with TabLayout
        TabLayout mTabs = (TabLayout) findViewById(R.id.tabs);
        mTabs.setupWithViewPager(mViewPager);

        // Set up the given viewPager with one Fragment instance for tab
        for (int i = 0; i < myTabAdapter.getCount(); i++) {
            TabLayout.Tab mTab = mTabs.getTabAt(i);
            mTab.setCustomView(myTabAdapter.getTabView(i));
        }
    }

    // ========================================================
    /**
     * MyTabAdapter
     * */
    private class MyTabAdapter extends FragmentPagerAdapter {
        private Fragment[] mFragmentList;
        private String[] mFragmentTitleList;
        private int[] mFragmentIconList;

        public MyTabAdapter(FragmentManager manager) {
            super(manager);

            mFragmentList = TAB_FRAGMENTS;
            mFragmentTitleList = TAB_TITLES;
            mFragmentIconList = TAB_ICONS_INT;
        }

        @Override
        public int getCount() {
            return TAB_NUM;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList[position];
        }

        /** returns the view for the tab at the given position */
        public View getTabView(int position) {
            View view = LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.item_tab, null);

            TextView mTabTitle = (TextView) view.findViewById(R.id.title_tab);
            ImageView mTabIcon = (ImageView) view.findViewById(R.id.icon_tab);

            mTabTitle.setText(mFragmentTitleList[position]);
            mTabIcon.setImageResource(mFragmentIconList[position]);

            return view;
        }
    }
    // [END MyTabAdapter]

    // ========================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // Inflate menu resource file
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
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
    /*
    * Logout from Firebase
    * */
    public void logout() {
        // Logout
        if(mFirebaseAuth != null) {
            mFirebaseAuth.signOut();
        }

        // Restart from LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // ========================================================

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
