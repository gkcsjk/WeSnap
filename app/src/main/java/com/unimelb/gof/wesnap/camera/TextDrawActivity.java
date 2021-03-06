package com.unimelb.gof.wesnap.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;

/**
 * TextDrawActivity
 * This activity let user draw text on the photo. User can select the position
 * by just simple click the photos.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */

public class TextDrawActivity extends BaseActivity implements View.OnClickListener{

    private final static String HINT_TEXT = "Position selected.";
    private final static String TAG = "TextDrawActivity";

    private ImageView mImageview;
    private TextView mTextView;
    private EditText mEditText;
    private Button mButton1, mButton2;
    private String mCurrentPath;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private String mText;
    private float x,y;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_text);

        Intent intent = getIntent();
        mCurrentPath = intent.getStringExtra(PhotoEditor.PATH_RECEIVER);
        mImageview = (ImageView) findViewById(R.id.iv_show);
        mTextView = (TextView) findViewById(R.id.tv_tip);
        mEditText = (EditText) findViewById(R.id.et_text);
        mButton1 = (Button) findViewById(R.id.bt_clear);
        mButton1.setOnClickListener(this);
        mButton2 = (Button) findViewById(R.id.bt_set);
        mButton2.setOnClickListener(this);
        mBitmap = PhotoEditor.setPicOnEmotion(mCurrentPath);
        mImageview.setImageBitmap(mBitmap);
        mCanvas = new Canvas(mBitmap);

        mImageview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float touchX = event.getX();
                float touchY = event.getY();
                x = mBitmap.getWidth()*touchX/mImageview.getWidth();
                y = mBitmap.getHeight()*touchY/mImageview.getHeight();
                mTextView.setText(HINT_TEXT);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_clear:
                mEditText.setText("");
                mText = "";
                break;
            case R.id.bt_set:
                setText();
                break;
            default:
                break;
        }
    }

    private void setText(){
        mText = mEditText.getText().toString();
        Log.d(TAG, mText);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setTextSize(50);
        mCanvas.drawText(mText ,x, y, mPaint);
        Log.d(TAG, String.valueOf(x)+"  " +String.valueOf(y));
        mImageview.setImageBitmap(mBitmap);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isFinishing()) {
                showSaveEditDialog(mCurrentPath, mBitmap);
            }
            return true;
        }
        return false;
    }
}
