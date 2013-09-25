package com.timecards.app;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.timecards.R;


public class CameraActivity extends Activity {

    public final static String IMAGE_FROM_CAMERA = "IMAGE_FROM_CAMERA";

    private Camera mCamera;

    private byte[] mPhoto;

    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        final View contentView = findViewById(R.id.camera_preview);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPhoto != null) {
                            Button captureButton = (Button) findViewById(R.id.button_capture);
                            captureButton.setText(R.string.capture_button);

                            mPhoto = null;
                            mCamera.startPreview();
                        }else{

                            // get an image from the camera
                            mCamera.takePicture(null, null, mPictureCallback);
                        }
                    }
                }
        );


        Button doneButton = (Button) findViewById(R.id.button_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                releaseCamera();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(IMAGE_FROM_CAMERA, mPhoto);
                setResult(Activity.RESULT_OK, resultIntent);

                finish();
            }
        });
    }

    private Camera getCameraInstance() {
        int cameraCount = 0;
        Camera camera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo );
            if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
                try {
                    camera = Camera.open( camIdx );
                } catch (RuntimeException e) {

                }
            }
        }

        return camera;
    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Button captureButton = (Button) findViewById(R.id.button_capture);
            captureButton.setText(R.string.cancel_button);

            mPhoto = data;
        }
    };

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        releaseCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
