package com.unimelb.gof.wesnap.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.unimelb.gof.wesnap.BaseActivity;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Karl on 3/10/2016.
 */

public class BaseEditPhotoActivity extends BaseActivity {


    public Bitmap setPic( String mCurrentPath ) {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inMutable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPath, bmOptions);

        return bitmap;
    }

    public void savePic( String mCurrentPath, Bitmap mBitmap ) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mCurrentPath);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap setPic(String mCurrentPath, ImageView mImageView) {
		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();
        Log.d("current path",mCurrentPath);
		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPath, bmOptions);
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
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPath, bmOptions);
        mImageView.setImageBitmap(bitmap);

        return bitmap;
    }
}