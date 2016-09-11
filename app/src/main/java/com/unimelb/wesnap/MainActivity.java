package com.unimelb.wesnap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    /* Settings for the Tabs */
    private static final int TAB_NUM = 5;
    private static final String TAB_TITLE_CONTACTS = "Contact";
    private static final String TAB_TITLE_CHAT = "Chat";
    private static final String TAB_TITLE_SNAP = "Snap";
    private static final String TAB_TITLE_STORIES = "Stories";
    private static final String TAB_TITLE_ME = "Me";
    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections.
     * We use a {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory.
     * If this becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private String mPhotoUrl;
    private GoogleApiClient mGoogleApiClient;

    //Dialog interface click listener
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
        public void onClick(DialogInterface dialog, int button){
            switch (button){
                case AlertDialog.BUTTON_POSITIVE:
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    break;
                default:
                    break;
            }
        }
    };

    // ======================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        //Initialise Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

//        //Set up the action bar.
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++){
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(mSectionsPagerAdapter.getTabView(i));
        }

    }


    // ======================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // ======================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //noinspection SimplifiableIfStatement
        switch(item.getItemId()){
            case R.id.action_settings:
                return true;
            case R.id.logout:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                Intent intent = new Intent(this, LoginChooserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.exit:
                exitApp();
        }


        return super.onOptionsItemSelected(item);
    }

    public void exitApp(){
        AlertDialog isExit = new AlertDialog.Builder(this).create();
        isExit.setTitle("Confirm Exit");
        isExit.setMessage("Are you sure to exit?");
        isExit.setButton(AlertDialog.BUTTON_POSITIVE,"YES", listener);
        isExit.setButton(AlertDialog.BUTTON_NEGATIVE,"NO", listener);
        isExit.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            exitApp();
        }
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    // ======================================================
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /* Instantiate the PlaceholderFragment for the given page */
        @Override
        public Fragment getItem(int position) {
            // TODO: add the relevant fragment / activity here
            switch(position) {
                default:
                   return PlaceholderFragment.newInstance(position + 1);
            }
        }

        /* Set the total number of tabs: TAB_NUM */
        @Override
        public int getCount() {
            return TAB_NUM;
        }

        public View getTabView( int position ){
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.tab_icon,null);
            TextView tv = (TextView) view.findViewById(R.id.iconText);
            ImageView iv = (ImageView) view.findViewById(R.id.icon);
            switch (position) {
                case 0:
                    tv.setText(TAB_TITLE_CONTACTS);
                    iv.setImageResource(R.mipmap.contacts);
                    break;
                case 1:
                    tv.setText(TAB_TITLE_CHAT);
                    iv.setImageResource(R.mipmap.chat);
                    break;
                case 2:
                    tv.setText(TAB_TITLE_SNAP);
                    iv.setImageResource(R.mipmap.camera);
                    break;
                case 3:
                    tv.setText(TAB_TITLE_STORIES);
                    iv.setImageResource(R.mipmap.discover);
                    break;
                case 4:
                    tv.setText(TAB_TITLE_ME);
                    iv.setImageResource(R.mipmap.profile);
                    break;
            }
            return view;
        }
    }


}
