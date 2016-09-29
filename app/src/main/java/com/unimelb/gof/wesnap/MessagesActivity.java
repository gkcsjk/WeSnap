package com.unimelb.gof.wesnap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

/**
 * Created by qideng on 21/9/16.
 */

public class MessagesActivity extends AppCompatActivity {
    private static final String TAG = "MessagesActivity";

    public static final String EXTRA_CHAT_KEY = "chat_key";
    private String mChatKey;
    private RecyclerView mMessagesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);

        // Get post key from intent
        mChatKey = getIntent().getStringExtra(EXTRA_CHAT_KEY);
        if (mChatKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CHAT_KEY");
        }
    }
}
