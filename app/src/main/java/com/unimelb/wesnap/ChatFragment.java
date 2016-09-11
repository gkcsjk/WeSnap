package com.unimelb.wesnap;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";

    /* UI Variables */

    /* Fragment singleton??? */
    private static ChatFragment mChatFragment = null;

    public ChatFragment() {
    }

    /* Returns a singleton instance of this fragment */
    public static ChatFragment getInstance() {
        if (mChatFragment == null) {
            mChatFragment = new ChatFragment();
        }
        return mChatFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
}