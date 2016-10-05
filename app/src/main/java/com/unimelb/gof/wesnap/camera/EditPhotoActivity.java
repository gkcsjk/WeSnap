package com.unimelb.gof.wesnap.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.unimelb.gof.wesnap.R;

public class EditPhotoActivity extends BaseEditPhotoActivity
//        implements View.OnClickListener
{

    private static final String TAG = "EditPhoto Activity";
    private String mCurrentPhotoPath;
    private Bitmap mBitmap;
    private Button mButtonSend;
    private Button mButton1, mButton2, mButton3;
    private Canvas mCanvas;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mCurrentPhotoPath = intent.getStringExtra("CameraFragment");
        setContentView(R.layout.acticity_edit_photo);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_edit_photo);
        setSupportActionBar(mToolbar);

//        mButton1 = (Button) findViewById(R.id.bt_freehand);
//        mButton1.setOnClickListener(this);
//        mButton2 = (Button) findViewById(R.id.bt_emoji);
//        mButton2.setOnClickListener(this);
//        mButton3 = (Button) findViewById(R.id.bt_text);
//        mButton3.setOnClickListener(this);
//        mButtonSend = (Button) findViewById(R.id.bt_send);
//        mButtonSend.setOnClickListener(this);

        mImageView = (ImageView) findViewById(R.id.iv_show);
    }

    // ========================================================
    /* onCreateOptionsMenu()
     * Inflate the menu: add items to the action bar if it is present */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_photo, menu);
        return true;
    }

    /* onOptionsItemSelected()
     * Handle action bar item clicks */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.bt_freehand:
                Intent freehandIntent = new Intent(this, FreehandDrawActivity.class);
                freehandIntent.putExtra(TAG, mCurrentPhotoPath);
                startActivity(freehandIntent);
                break;
            case R.id.bt_emoji:
                Intent emojiIntent = new Intent(this, EmojiDrawActivity.class);
                emojiIntent.putExtra(TAG, mCurrentPhotoPath);
                startActivity(emojiIntent);
                break;
            case R.id.bt_text:
                Intent textIntent = new Intent(this, TextDrawActivity.class);
                textIntent.putExtra(TAG, mCurrentPhotoPath);
                startActivity(textIntent);
                break;
            case R.id.bt_send:
                sendPhoto();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendPhoto() {
        Toast.makeText(EditPhotoActivity.this,
                R.string.action_send_photo,
                Toast.LENGTH_SHORT).show();
    }

    // ========================================================
    @Override
    protected void onResume() {
        super.onResume();
        mBitmap = setPic(mCurrentPhotoPath, mImageView);
        mCanvas = new Canvas(mBitmap);
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.bt_freehand:
//                Intent freehandIntent = new Intent(this, FreehandDrawActivity.class);
//                freehandIntent.putExtra(TAG, mCurrentPhotoPath);
//                startActivity(freehandIntent);
//                break;
//            case R.id.bt_emoji:
//                Intent emojiIntent = new Intent(this, EmojiDrawActivity.class);
//                emojiIntent.putExtra(TAG, mCurrentPhotoPath);
//                startActivity(emojiIntent);
//                break;
//            case R.id.bt_text:
//                Intent textIntent = new Intent(this, TextDrawActivity.class);
//                textIntent.putExtra(TAG, mCurrentPhotoPath);
//                startActivity(textIntent);
//                break;
//            case R.id.bt_send:
//                //sendPhoto();
//                break;
//            default:
//                break;
//        }
//    }

}
