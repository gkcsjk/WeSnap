package com.unimelb.gof.wesnap.camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.chat.ChooseFriendActivity;

public class EditPhotoActivity extends BaseEditPhotoActivity implements View.OnClickListener {

    private static final String TAG = "EditPhotoActivity";
    private String mCurrentPhotoPath;
    private Button mButtonSend;
    private Button mButton1, mButton2, mButton3;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mCurrentPhotoPath = intent.getStringExtra(PATH_RECEIVER);
        setContentView(R.layout.acticity_edit_photo);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_edit_photo);
        setSupportActionBar(mToolbar);

        mButton1 = (Button) findViewById(R.id.bt_freehand);
        mButton1.setOnClickListener(this);
        mButton2 = (Button) findViewById(R.id.bt_emoji);
        mButton2.setOnClickListener(this);
        mButton3 = (Button) findViewById(R.id.bt_text);
        mButton3.setOnClickListener(this);
        mButtonSend = (Button) findViewById(R.id.bt_send);
        mButtonSend.setOnClickListener(this);
        mImageView = (ImageView) findViewById(R.id.iv_show);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPic(mCurrentPhotoPath, mImageView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_freehand:
                Intent freehandIntent = new Intent(this, FreehandDrawActivity.class);
                freehandIntent.putExtra(PATH_RECEIVER, mCurrentPhotoPath);
                startActivity(freehandIntent);
                break;
            case R.id.bt_emoji:
                Intent emojiIntent = new Intent(this, EmojiDrawActivity.class);
                emojiIntent.putExtra(PATH_RECEIVER, mCurrentPhotoPath);
                startActivity(emojiIntent);
                break;
            case R.id.bt_text:
                Intent textIntent = new Intent(this, TextDrawActivity.class);
                textIntent.putExtra(PATH_RECEIVER, mCurrentPhotoPath);
                startActivity(textIntent);
                break;
            case R.id.bt_send:
                sendPhoto();
                break;
            default:
                break;
        }
    }

    private void sendPhoto(){
        Intent sendPhotoIntent = new Intent(this, ChooseFriendActivity.class);
        sendPhotoIntent.putExtra(ChooseFriendActivity.EXTRA_PHTOT_PATH, mCurrentPhotoPath);
        startActivity(sendPhotoIntent);
        finish();
    }
}
