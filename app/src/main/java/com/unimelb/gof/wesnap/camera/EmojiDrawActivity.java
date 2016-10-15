package com.unimelb.gof.wesnap.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Karl on 3/10/2016.
 */

public class EmojiDrawActivity extends BaseActivity {
    private String mCurrentPath;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private ImageView mImageView;
    private TextView mTextView;
    private EmotionServiceClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji_draw);
        Intent intent = getIntent();
        mCurrentPath = intent.getStringExtra(PhotoEditor.PATH_RECEIVER);
        mImageView = (ImageView) findViewById(R.id.image_fullscreen_show_photo);
        mBitmap = PhotoEditor.setPicOnEmotion(mCurrentPath);
        mTextView = (TextView) findViewById(R.id.text_message);

        if (client == null){
            client = new EmotionServiceRestClient(getString(R.string.emotion_key));
        }

        if (mBitmap != null){
            Log.d("EmojiDrawActivity", "Do Recognize");
            mImageView.setImageBitmap(mBitmap);
            doRecognize();
        }
    }

    private void doRecognize(){
        try {
            new doRequest().execute();
        } catch (Exception e) {
            mTextView.append("Error encountered. Exception is: " + e.toString());
        }

    }

    private List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;

        public doRequest() {

        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            try {
                    return processWithAutoFaceDetection();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            if (e != null) {
                mTextView.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                if (result.size() == 0) {
                    mTextView.append("No emotion detected :(");
                } else {
                    Integer count = 0;
                    // Covert bitmap to a mutable bitmap by copying it
                    Bitmap bitmapCopy = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas faceCanvas = new Canvas(bitmapCopy);
                    faceCanvas.drawBitmap(mBitmap, 0, 0, null);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5);
                    paint.setColor(Color.RED);

                    for (RecognizeResult r : result) {
                        mTextView.append(String.format("\nFace #%1$d \n", count));
                        mTextView.append(String.format("\t anger: %1$.5f\n", r.scores.anger));
                        mTextView.append(String.format("\t contempt: %1$.5f\n", r.scores.contempt));
                        mTextView.append(String.format("\t disgust: %1$.5f\n", r.scores.disgust));
                        mTextView.append(String.format("\t fear: %1$.5f\n", r.scores.fear));
                        mTextView.append(String.format("\t happiness: %1$.5f\n", r.scores.happiness));
                        mTextView.append(String.format("\t neutral: %1$.5f\n", r.scores.neutral));
                        mTextView.append(String.format("\t sadness: %1$.5f\n", r.scores.sadness));
                        mTextView.append(String.format("\t surprise: %1$.5f\n", r.scores.surprise));
                        mTextView.append(String.format("\t face rectangle: %d, %d, %d, %d", r.faceRectangle.left, r.faceRectangle.top, r.faceRectangle.width, r.faceRectangle.height));
                        faceCanvas.drawRect(r.faceRectangle.left,
                                r.faceRectangle.top,
                                r.faceRectangle.left + r.faceRectangle.width,
                                r.faceRectangle.top + r.faceRectangle.height,
                                paint);
                        count++;
                    }
                    ImageView imageView = (ImageView) findViewById(R.id.image_fullscreen_show_photo);
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
                }
            }
        }
    }

}
