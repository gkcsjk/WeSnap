package com.unimelb.wesnap;

import android.content.Intent;
import android.media.Image;
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
public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "System";
    private static final String CAMERA_TEXT = "It's SNAP TIME";

    /*
    Private variables for each fragment, ordered by INT POSITION
     */
    //fragment 3:
    private Button cameraB;
    private TextView textView3;
    private ImageView iv3;




    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
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
        Log.i(TAG,",resultCode=" + resultCode );
        iv3.setImageURI(data.getData());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch ((getArguments().getInt(ARG_SECTION_NUMBER))){
            case 3:
                //snap
                textView3 = (TextView) view.findViewById(R.id.camera_txt);
                textView3.setText(CAMERA_TEXT);
                cameraB = (Button) view.findViewById(R.id.camera_b1);
                iv3 = (ImageView) view.findViewById(R.id.phview);
                cameraB.setOnClickListener(click);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 3:
                //Snap
                return inflater.inflate(R.layout.fragment_camera, container, false);

            default:
                View rootView = inflater.inflate(R.layout.fragment_main, container, false);

                TextView textView = (TextView) rootView.findViewById(R.id.section_label);
                textView.setText(getString(
                        R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

                return rootView;
        }



    }


}