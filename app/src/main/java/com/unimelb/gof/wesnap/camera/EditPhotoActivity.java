package com.unimelb.gof.wesnap.camera;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.chat.ChooseFriendActivity;

import com.unimelb.gof.wesnap.chat.MessagesActivity;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.PhotoUploader;

import java.io.File;

public class EditPhotoActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "EditPhotoActivity";
    public static final String EXTRA_PHOTO_PATH = "photo_path";
    public static final String EXTRA_CHAT_ID = "chat_id";

    private String mCurrentPhotoPath;
    private int mTimeToLive = AppParams.DEFAULT_TTL;
    private String mChatId = null;

    private ImageView mImageView;
    private Button mButtonSend;
    private ImageButton mButtonSaveMemory;
    private ImageButton mButtonSaveStory;
    private ImageButton mButtonSetTimer;
    private Dialog mDialog;
    private ImageButton mButtonFreehand;
    private ImageButton mButtonText;
    private ImageButton mButtonEmoji;
    private boolean mIsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Get the local path of the photo taken */
        mCurrentPhotoPath = getIntent().getStringExtra(EXTRA_PHOTO_PATH);
        if (mCurrentPhotoPath == null) {
            throw new IllegalArgumentException("Must pass EXTRA_PHOTO_PATH");
        }
        /* Get chat id if passed */
        mChatId = getIntent().getStringExtra(EXTRA_CHAT_ID);

        /* UI */
        setContentView(R.layout.acticity_edit_photo);

        mImageView = (ImageView) findViewById(R.id.iv_show);
        mImageView.setOnClickListener(this);

        ImageButton buttonCancel = (ImageButton) findViewById(R.id.bt_cancel_edit);
        buttonCancel.setOnClickListener(this);

        mButtonFreehand = (ImageButton) findViewById(R.id.bt_freehand);
        mButtonFreehand.setOnClickListener(this);
        mButtonText = (ImageButton) findViewById(R.id.bt_text);
        mButtonText.setOnClickListener(this);
        mButtonEmoji = (ImageButton) findViewById(R.id.bt_emoji);
        mButtonEmoji.setOnClickListener(this);

        mButtonSetTimer = (ImageButton) findViewById(R.id.bt_set_timer);
        mButtonSetTimer.setOnClickListener(this);
        mDialog = new Dialog(EditPhotoActivity.this);
        mDialog.setTitle("Photo Timer");
        mDialog.setContentView(R.layout.number_picker_dialog);

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
        PhotoEditor.setPic(mCurrentPhotoPath, mImageView);
    }

    // ========================================================
    /* onClick(): perform the requested actions by the clicked buttons */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.iv_show:
                updateControls();
                break;
            case R.id.bt_cancel_edit:
                finish();
                break;
            case R.id.bt_freehand:
                Intent freehandIntent = new Intent(this, FreehandDrawActivity.class);
                freehandIntent.putExtra(PhotoEditor.PATH_RECEIVER, mCurrentPhotoPath);
                startActivity(freehandIntent);
                break;
            case R.id.bt_emoji:
                Intent emojiIntent = new Intent(this, EmojiDrawActivity.class);
                emojiIntent.putExtra(PhotoEditor.PATH_RECEIVER, mCurrentPhotoPath);
                startActivity(emojiIntent);
                break;
            case R.id.bt_text:
                Intent textIntent = new Intent(this, TextDrawActivity.class);
                textIntent.putExtra(PhotoEditor.PATH_RECEIVER, mCurrentPhotoPath);
                startActivity(textIntent);
                break;
            case R.id.bt_set_timer:
                showNumberPicker();
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
    /* Show/Hide the control buttons */
    private void updateControls() {
        if (mIsVisible) {
            findViewById(R.id.ui_group_edit_buttons).setVisibility(View.GONE);
            findViewById(R.id.ui_group_share_buttons).setVisibility(View.GONE);
            findViewById(R.id.bt_send).setVisibility(View.GONE);
            mIsVisible = false;
        } else {
            findViewById(R.id.ui_group_edit_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.ui_group_share_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.bt_send).setVisibility(View.VISIBLE);
            mIsVisible = true;
        }
    }

    // ========================================================
    /* Show the NumberPicker dialog for setting timer on photo */
    private void showNumberPicker() {
        Log.d(TAG, "showNumberPicker");

        final NumberPicker numberPicker = (NumberPicker) mDialog.findViewById(R.id.number_picker);
        numberPicker.setMinValue(AppParams.MIN_TTL);
        numberPicker.setMaxValue(AppParams.MAX_TTL);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setOnValueChangedListener(
                new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        Log.d(TAG, "showNumberPicker:newValue=" + newVal);
                    }
                });

        Button btConfirm = (Button) mDialog.findViewById(R.id.button_confirm_timer);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeToLive = numberPicker.getValue();
                mDialog.dismiss();
            }
        });

        Button btCancel = (Button) mDialog.findViewById(R.id.button_cancel_timer);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.show();
    }

    // ========================================================
    /* Send the photo to friends */
    private void sendPhoto() {
        if (mChatId != null) {
            // if given chat id, send to the specified chat
            Intent data = new Intent();
            data.putExtra(MessagesActivity.EXTRA_PHOTO_PATH, mCurrentPhotoPath);
            data.putExtra(MessagesActivity.EXTRA_TIME_TO_LIVE, mTimeToLive);
            setResult(RESULT_OK, data);
        } else {
            // choose a friend as receiver
            Intent sendPhotoIntent = new Intent(this, ChooseFriendActivity.class);
            sendPhotoIntent.putExtra(ChooseFriendActivity.EXTRA_PHOTO_PATH, mCurrentPhotoPath);
            sendPhotoIntent.putExtra(ChooseFriendActivity.EXTRA_TIME_TO_LIVE, mTimeToLive);
            startActivity(sendPhotoIntent);
        }
        finish();
    }

    // ========================================================
    /* Save as current user's "memory" */
    private void saveMemory() {
        File photoFile = new File(mCurrentPhotoPath);
        Uri photoUri = FileProvider.getUriForFile(EditPhotoActivity.this,
                AppParams.FILEPROVIDER, photoFile);
        PhotoUploader.uploadToMemories(photoUri, EditPhotoActivity.this);
    }

    // ========================================================
    /* Share as current user's "story" */
    private void saveStory() {
        File photoFile = new File(mCurrentPhotoPath);
        Uri photoUri = FileProvider.getUriForFile(EditPhotoActivity.this,
                AppParams.FILEPROVIDER, photoFile);
        PhotoUploader.uploadToStories(photoUri, EditPhotoActivity.this);
    }

    // ========================================================
}
