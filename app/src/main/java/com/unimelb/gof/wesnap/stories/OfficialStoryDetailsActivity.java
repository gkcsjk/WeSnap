package com.unimelb.gof.wesnap.stories;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.OfficialStory;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
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
    private TextView keywordView;
    private ImageButton readSimilarButton;
    private ImageButton subscribeButton;
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
        keywordView = (TextView) findViewById(R.id.keyword);
        keywordView.setText(getString(R.string.text_story_keyword_var, thisStory.keyword));
        readSimilarButton = (ImageButton) findViewById(R.id.bt_similar);
        subscribeButton = (ImageButton) findViewById(R.id.bt_subscribe);
        subscribeButton.setImageResource(R.drawable.ic_action_subscribe);
        // TODO check if already subscribed?

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

        /* set click listeners */
        // show web content
        readStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "readStory:showWebContent:title=" + thisStory.title);
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

        // show stories with the same keyword
        readSimilarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "readSimilar:showDiscover:keyword=" + thisStory.keyword);
                Intent showDiscoverIntent = new Intent(
                        OfficialStoryDetailsActivity.this,
                        DiscoverActivity.class);
                showDiscoverIntent.putExtra(DiscoverActivity.EXTRA_INTERESTS, thisStory.keyword);
                startActivity(showDiscoverIntent);
            }
        });

        // subscribe to stories with the same keyword
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // TODO subscribe?
                Log.d(TAG, "subscribe:keyword=" + thisStory.keyword);
                // save to Firebase Database
                if (FirebaseUtil.getMySubscriptionKeywordsRef() != null) {
                    FirebaseUtil.getMySubscriptionKeywordsRef()
                            .child(thisStory.keyword).setValue(true);
                }
                // update UI
                subscribeButton.setImageResource(R.drawable.ic_action_subscribe_done);
                Toast.makeText(OfficialStoryDetailsActivity.this,
                        "Subscribed to " + thisStory.keyword,
                        Toast.LENGTH_SHORT).show();

                // TODO for un-subscribe
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
