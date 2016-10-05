package com.unimelb.gof.wesnap.camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.chat.ChooseFriendActivity;

import com.unimelb.gof.wesnap.util.AppParams;

public class EditPhotoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "EditPhotoActivity";
    public static final String EXTRA_PHOTO_PATH = "photo_path";

    private String mCurrentPhotoPath;
    private int mTimeToLive = AppParams.DEFAULT_TTL;

    private ImageView mImageView;
    private Button mButtonSend;
    private ImageButton mButtonSaveMemory;
    private ImageButton mButtonSaveStory;
    private ImageButton mButtonSetTimer;
    private ImageButton mButtonFreehand;
    private ImageButton mButtonText;
    private ImageButton mButtonEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Get the local path of the photo taken */
        mCurrentPhotoPath = getIntent().getStringExtra(EXTRA_PHOTO_PATH);

        /* UI */
        setContentView(R.layout.acticity_edit_photo);

        mImageView = (ImageView) findViewById(R.id.iv_show);

        mButtonFreehand = (ImageButton) findViewById(R.id.bt_freehand);
        mButtonFreehand.setOnClickListener(this);
        mButtonText = (ImageButton) findViewById(R.id.bt_text);
        mButtonText.setOnClickListener(this);
        mButtonEmoji = (ImageButton) findViewById(R.id.bt_emoji);
        mButtonEmoji.setOnClickListener(this);

        mButtonSetTimer = (ImageButton) findViewById(R.id.bt_set_timer);
        mButtonSetTimer.setOnClickListener(this);

        mButtonSend = (Button) findViewById(R.id.bt_send);
        mButtonSend.setOnClickListener(this);

        mButtonSaveMemory = (ImageButton) findViewById(R.id.bt_save_to_memory);
        mButtonSaveMemory.setOnClickListener(this);
        mButtonSaveStory = (ImageButton) findViewById(R.id.bt_save_to_story);
        mButtonSaveStory.setOnClickListener(this);
    }

    // ========================================================
    @Override
    protected void onResume() {
        super.onResume();
        EditPhoto.setPic(mCurrentPhotoPath, mImageView);
    }

    // ========================================================
    /* onClick(): perform the requested actions by the clicked buttons */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_freehand:
                Intent freehandIntent = new Intent(this, FreehandDrawActivity.class);
                freehandIntent.putExtra(EditPhoto.PATH_RECEIVER, mCurrentPhotoPath);
                startActivity(freehandIntent);
                break;
            case R.id.bt_emoji:
                // TODO EmojiDrawActivity
                Toast.makeText(EditPhotoActivity.this,
                        R.string.action_draw_emoji,
                        Toast.LENGTH_SHORT).show();
//                Intent emojiIntent = new Intent(this, EmojiDrawActivity.class);
//                emojiIntent.putExtra(EditPhoto.PATH_RECEIVER, mCurrentPhotoPath);
//                startActivity(emojiIntent);
                break;
            case R.id.bt_text:
                Intent textIntent = new Intent(this, TextDrawActivity.class);
                textIntent.putExtra(EditPhoto.PATH_RECEIVER, mCurrentPhotoPath);
                startActivity(textIntent);
                break;
            case R.id.bt_set_timer:
                // TODO set timer alert dialog???
                break;
            case R.id.bt_send:
                sendPhoto();
                break;
            case R.id.bt_save_to_memory:
                saveMemory();
                break;
            case R.id.bt_save_to_story:
                saveStory();
                break;
            default:
                break;
        }
    }

    // ========================================================
    private void sendPhoto() {
        Intent sendPhotoIntent = new Intent(this, ChooseFriendActivity.class);
        sendPhotoIntent.putExtra(ChooseFriendActivity.EXTRA_PHOTO_PATH, mCurrentPhotoPath);
        sendPhotoIntent.putExtra(ChooseFriendActivity.EXTRA_TIME_TO_LIVE, mTimeToLive);
        startActivity(sendPhotoIntent);
        finish();
    }

    // ========================================================
    private void saveMemory() {
        // TODO saveMemory
        Toast.makeText(EditPhotoActivity.this,
                R.string.action_save_to_memory,
                Toast.LENGTH_SHORT).show();
    }

    // ========================================================
    private void saveStory() {
        // TODO saveStory
        Toast.makeText(EditPhotoActivity.this,
                R.string.action_save_to_story,
                Toast.LENGTH_SHORT).show();
    }

    // ========================================================
}
