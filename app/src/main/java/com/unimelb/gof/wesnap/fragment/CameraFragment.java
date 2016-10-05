package com.unimelb.gof.wesnap.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.camera.BaseEditPhotoActivity;
import com.unimelb.gof.wesnap.camera.EditPhotoActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    /* UI Variables */
    private Button startCameraB;
    private OnClickListener clickListener_startcamera;
    private TextView textView;
    private String mCurrentPhotoPath;

    public CameraFragment() {
    }

    private File createImageFile() throws IOException{
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = (TextView) view.findViewById(R.id.text_title_camera);
        textView.setText(R.string.text_title_camera);

        startCameraB = (Button) view.findViewById(R.id.button_start_camera);
        clickListener_startcamera = new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPhotoPath = null;
                Intent startCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (startCameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    File photoFile = null;
                    try{
                        photoFile = createImageFile();
                    } catch (IOException ex){
                        Log.d("Error", "Create file error");
                    }
                    if (photoFile  != null) {
                        Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                "com.unimelb.gof.wesnap.fileprovider",
                                photoFile);
                        startCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(startCameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        };
        startCameraB.setOnClickListener(clickListener_startcamera);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK){
            if (mCurrentPhotoPath != null){
                Intent editPhotoIntent = new Intent(getActivity(), EditPhotoActivity.class);
                editPhotoIntent.putExtra(BaseEditPhotoActivity.PATH_RECEIVER, mCurrentPhotoPath);
                startActivity(editPhotoIntent);
            }
        }
    }
}
