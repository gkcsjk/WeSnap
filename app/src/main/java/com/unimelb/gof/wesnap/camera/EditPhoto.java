package com.unimelb.gof.wesnap.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Karl on 3/10/2016.
 */

public class EditPhoto {

    public static final String PATH_RECEIVER = "path_receiver";

    public static Bitmap setPic( String mCurrentPath ) {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inMutable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPath, bmOptions);

        return bitmap;
    }

    public static void savePic( String mCurrentPath, Bitmap mBitmap ) {

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

    public static Bitmap setPic(String mCurrentPath, ImageView mImageView) {
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

    public static Bitmap setPic(String mCurrentPath, int w, int h) {
        Log.d("current path",mCurrentPath);
		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        /* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((w > 0) || (h > 0)) {
            scaleFactor = Math.min(photoW/w, photoH/h);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inMutable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPath, bmOptions);
        return bitmap;
    }

    public static Bitmap resizeBitmap( Bitmap mBitmap, int w, int h){
        int photoHeight = mBitmap.getHeight();
        int photoWidth = mBitmap.getWidth();
        float scaleWidth = ((float) w) / photoWidth;
        float scaleHeight = ((float) h) / photoHeight;
        float scaleFactor = 1;
        if ((w > 0) || (h > 0)) {
            scaleFactor = Math.min(scaleWidth, scaleHeight);
        }
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleFactor, scaleFactor);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                mBitmap, 0, 0, photoWidth, photoHeight, matrix, false);
        mBitmap.recycle();
        return resizedBitmap;
    }
}
