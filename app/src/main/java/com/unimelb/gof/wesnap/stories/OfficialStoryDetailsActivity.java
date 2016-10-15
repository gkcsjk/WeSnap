package com.unimelb.gof.wesnap.stories;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.OfficialStory;
import com.unimelb.gof.wesnap.util.GlideUtil;

/**
 * OfficialStoryDetailsActivity
 * Provides webpage view for the official story
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class OfficialStoryDetailsActivity extends BaseActivity {
    private static final String TAG = "OfficialStoryDetails";

    public static final String EXTRA_INFO_ARRAY = "official_story_in_array";

    private ImageView thumnailView;
    private TextView titleView;
    private Button readSource;
    private Button readKeyword;
    private TextView summaryView;
    private Button readStory;
    private WebView mWebpageView;

    private OfficialStory thisStory;

    // ======================================================
    /* onCreate() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official_story_details);

        /* create story object */
        String[] array = getIntent().getStringArrayExtra(EXTRA_INFO_ARRAY);
        if (array == null) {
            throw new IllegalArgumentException("Must pass EXTRA_INFO_ARRAY");
        }
        thisStory = new OfficialStory(array);

        /* load info fields */
        readSource = (Button) findViewById(R.id.source);
        readSource.setText(thisStory.source);
        readKeyword = (Button) findViewById(R.id.keyword);
        readKeyword.setText(getString(R.string.text_story_keyword_var, thisStory.keyword));

        thumnailView = (ImageView) findViewById(R.id.thumnail);
        String photoUrl = thisStory.photoUrl;
        if (photoUrl != null && photoUrl.length() != 0) {
            GlideUtil.loadPhoto(photoUrl, thumnailView);
        }
        titleView = (TextView) findViewById(R.id.title);
        titleView.setText(thisStory.title);
        summaryView = (TextView) findViewById(R.id.sumary);
        summaryView.setText(thisStory.summary);
        readStory = (Button) findViewById(R.id.read_more);

        mWebpageView = (WebView) findViewById(R.id.webpage);
        mWebpageView.setVisibility(View.GONE);

        readStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "itemClicked:" + thisStory.title);
                // hide
                thumnailView.setVisibility(View.GONE);
                titleView.setVisibility(View.GONE);
                summaryView.setVisibility(View.GONE);
                readStory.setVisibility(View.GONE);
                // show
                mWebpageView.loadUrl(thisStory.webpageUrl);
                mWebpageView.setVisibility(View.VISIBLE);
            }
        });

        readSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO subscribe?
                Log.d(TAG, "itemClicked:" + thisStory.source);
                Toast.makeText(OfficialStoryDetailsActivity.this,
                        "itemClicked:" + thisStory.source,
                        Toast.LENGTH_SHORT).show();
            }
        });

        readKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO subscribe?
                Log.d(TAG, "buttonClicked:" + thisStory.keyword);
//                Toast.makeText(OfficialStoryDetailsActivity.this,
//                        "itemClicked:" + thisStory.keyword,
//                        Toast.LENGTH_SHORT).show();
                Intent showDiscoverIntent = new Intent(
                        OfficialStoryDetailsActivity.this,
                        DiscoverActivity.class);
                showDiscoverIntent.putExtra(DiscoverActivity.EXTRA_INTERESTS, thisStory.keyword);
                startActivity(showDiscoverIntent);
            }
        });
    }

    // ========================================================
    /* Let back key triggers exit app dialog */
    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }
}
