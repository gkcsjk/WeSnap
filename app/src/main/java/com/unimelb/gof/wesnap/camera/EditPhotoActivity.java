package com.unimelb.gof.wesnap.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;

public class EditPhotoActivity extends BaseActivity implements View.OnClickListener {

    private String mCurrentPhotoPath;
    private Bitmap mBitmap;
    private Button mButtonSend;
    private Button mButton1, mButton2, mButton3;
    private Canvas mCanvas;
    private Paint mPaint;
    private DrawView mDrawView;
    private ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mCurrentPhotoPath = intent.getStringExtra("CameraFragment");
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
        mBitmap = setPic();
        mCanvas = new Canvas(mBitmap);
        mPaint = new Paint(mPaint);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_freehand:
                mImageView.setVisibility(View.GONE);
                mDrawView.setVisibility(View.VISIBLE);
                mDrawView = new DrawView(this, mCanvas, mBitmap, mPaint);
                mDrawView.findViewById(R.id.iv_edit);
                break;
            case R.id.bt_emoji:
                //drawEmoji();
                break;
            case R.id.bt_text:
                //drawText();
                break;
            case R.id.bt_send:
                //sendPhoto();
                break;
            default:
                break;
        }

    }

    private void drawFreehand(){


    }

    private Bitmap setPic() {
		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();
        Log.d("current path",mCurrentPhotoPath);
		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        /* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inMutable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);

        return bitmap;
    }


}
