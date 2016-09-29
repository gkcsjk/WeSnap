package com.unimelb.gof.wesnap.fragment;

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

import com.unimelb.gof.wesnap.R;

/**
 * Created by qideng on 20/09/2016.
 */

public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";

    /* UI Variables */
    private Button cameraB;
    private OnClickListener clickListener;
    private TextView textView3;
    private ImageView iv3;

    public CameraFragment() {
    }

    /* Returns a singleton instance of this fragment */
    private static CameraFragment mCameraFragment = null;
    public static CameraFragment getInstance() {
        if (mCameraFragment == null) {
            mCameraFragment = new CameraFragment();
        }
        return mCameraFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG,", resultCode=" + resultCode);
        iv3.setImageURI(data.getData());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView3 = (TextView) view.findViewById(R.id.text_title_camera);
        textView3.setText(R.string.text_title_camera);

        cameraB = (Button) view.findViewById(R.id.button_start_camera);
        clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent, 1);
            }
        };
        cameraB.setOnClickListener(clickListener);

        iv3 = (ImageView) view.findViewById(R.id.image_preview_camera);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }
}
