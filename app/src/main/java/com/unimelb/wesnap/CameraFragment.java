package com.unimelb.wesnap;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";

    /* UI Variables */
    private Button cameraB;
    private TextView textView3;
    private ImageView iv3;

    /* Fragment singleton??? */
    private static CameraFragment mCameraFragment = null;

    public CameraFragment() {
    }

    /* Returns a singleton instance of this fragment */
    public static CameraFragment getInstance() {
        if (mCameraFragment == null) {
            mCameraFragment = new CameraFragment();
        }
        return mCameraFragment;
    }

    private OnClickListener click = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(intent, 1);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG,", resultCode=" + resultCode);
        iv3.setImageURI(data.getData());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView3 = (TextView) view.findViewById(R.id.camera_txt);
        textView3.setText(R.string.text_camera);
        cameraB = (Button) view.findViewById(R.id.camera_b1);
        iv3 = (ImageView) view.findViewById(R.id.phview);
        cameraB.setOnClickListener(click);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }
}