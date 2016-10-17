package com.unimelb.gof.wesnap.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * EmojiDrawActivity
 * This activity let user draw emoji on the photos.
 * Microsoft Emotion API is used to detect the human faces and
 * their emotions.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
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
        mTextView.setText("Loading...");
        try {
            new doRequest().execute();
        } catch (Exception e) {
            mTextView.append("Error encountered. Exception is: " + e.toString());
        }
    }

    private void showResults(RecognizeResult r){
        mTextView.setText("");
        List<Map<String, Double>> scores;
        scores = new ArrayList<Map<String, Double>>();
        Map<String, Double> map;
        map = new HashMap<String, Double>();
        map.put("anger", r.scores.anger);
        map.put("disgust", r.scores.disgust);
        map.put("fear", r.scores.fear);
        map.put("happiness", r.scores.happiness);
        map.put("neutral", r.scores.neutral+r.scores.contempt);
        map.put("sadness", r.scores.anger);
        map.put("surprise", r.scores.surprise);
        Double max = Collections.max(map.values());
        String maxEmotion = null;
        Drawable emoji;
        List<Integer> faceRect= new ArrayList<Integer>();
        faceRect.add(r.faceRectangle.left);
        faceRect.add(r.faceRectangle.top);
        faceRect.add(r.faceRectangle.left + r.faceRectangle.width);
        faceRect.add(r.faceRectangle.top + r.faceRectangle.height);

        for (Map.Entry<String,Double> entry: map.entrySet()){
            if (Objects.equals(max, entry.getValue())){
                maxEmotion = entry.getKey();
            }
        }
        if (maxEmotion != null){
            mTextView.append(maxEmotion);
            switch (maxEmotion){
                case "anger":
                    emoji = getDrawable(R.mipmap.anger);
                    drawEmoji(emoji, faceRect);
                    break;
                case "disgust":
                    emoji = getDrawable(R.mipmap.disgust);
                    drawEmoji(emoji, faceRect);
                    break;
                case "happiness":
                    emoji = getDrawable(R.mipmap.happiness);
                    drawEmoji(emoji, faceRect);
                    break;
                case "fear":
                    emoji = getDrawable(R.mipmap.fear);
                    drawEmoji(emoji, faceRect);
                    break;
                case "neutral":
                    emoji = getDrawable(R.mipmap.neutral);
                    drawEmoji(emoji, faceRect);
                    break;
                case "sadness":
                    emoji = getDrawable(R.mipmap.sadness);
                    drawEmoji(emoji, faceRect);
                    break;
                case "surprise":
                    emoji = getDrawable(R.mipmap.surprise);
                    drawEmoji(emoji, faceRect);
                default:
                    break;
            }
        }
    }

    private void drawEmoji( Drawable emotion, List<Integer> faceRect ){
        Log.d("emoji", "Drawing emoji...");
        Canvas drawEmojiCanvas = new Canvas(mBitmap);
        emotion.setBounds(faceRect.get(0),
                faceRect.get(1),
                faceRect.get(2),
                faceRect.get(3));
        emotion.draw(drawEmojiCanvas);
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
                    for (RecognizeResult r : result) {
                        showResults(r);
                    }
                }
            }
        }
    }

}
