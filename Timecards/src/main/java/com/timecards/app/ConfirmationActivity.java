package com.timecards.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.timecards.R;
import com.timecards.api.Service;
import com.timecards.api.model.Timecard;
import com.timecards.libs.ProgressHUD;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConfirmationActivity extends Activity implements DialogInterface.OnCancelListener{

    public static final String TAG = ConfirmationActivity.class.getSimpleName();

    private ProgressHUD mProgressHUD;

    private Date mDate;

    private byte[] mPhoto;

    private double mLatitude;

    private double mLongitude;

    private int mActionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        Intent intent = this.getIntent();
        mPhoto = intent.getByteArrayExtra(CameraActivity.IMAGE_FROM_CAMERA);
        mLatitude = (double) intent.getDoubleExtra(ActionsActivity.GPS_LATITUDE, 0);
        mLongitude = (double) intent.getDoubleExtra(ActionsActivity.GPS_LONGITUDE, 0);
        mActionType = (int) intent.getIntExtra(ActionsActivity.ACTION_TYPE, 0);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        Bitmap originalImage = BitmapFactory.decodeByteArray(mPhoto, 0, mPhoto.length, options);

        //rotate original image
        Matrix matrix = new Matrix();
        matrix.postRotate(CameraPreview.ROTATION_DEGREES);
        ImageView photoImage = (ImageView) findViewById(R.id.confirmation_image);
        Bitmap bitmap = Bitmap.createBitmap(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(),
                matrix, true);
        photoImage.setImageBitmap(bitmap);

        //save rotated bytes
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        mPhoto = stream.toByteArray();

        mDate = new Date();
        String timeValue = new SimpleDateFormat("HH:mm a").format(mDate);
        String dateValue = new SimpleDateFormat("LLL dd, yyyy").format(mDate);
        String locationValue = mLatitude + "," + mLongitude;

        TextView timeLabel = (TextView) findViewById(R.id.time_label);
        timeLabel.setText(timeValue);

        TextView dateLabel = (TextView) findViewById(R.id.date_label);
        dateLabel.setText(dateValue);

        TextView locationLabel = (TextView) findViewById(R.id.gps_label);
        locationLabel.setText(locationValue);

        Button confirmationButton = (Button) findViewById(R.id.confirmation_button);
        confirmationButton.setText(mActionType == ActionsActivity.REQUEST_FROM_CAMERA_CLOCK_IN ?
                R.string.clock_in_button : R.string.clock_out_button);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.confirmation, menu);
        return true;
    }

    /**
     * Called when the user clicks the Send button
     */
    public void onConfirm(View view) {
        Timecard timecard = CurrentTimecard.getCurrentTimecard();
        final Activity activity = this;

        if (mActionType == ActionsActivity.REQUEST_FROM_CAMERA_CLOCK_IN) {
            timecard.setLatitudeIn(mLatitude);
            timecard.setLongitudeIn(mLongitude);
            timecard.setTimestampIn(mDate);
            //clock in
            Service.clockIn(this, timecard, mPhoto, new Callback<Timecard>() {
                @Override
                public void success(Timecard timecard, Response response) {
                    Log.v(TAG, timecard.toString());
                    showProgress(false);
                    finish();
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.v(TAG, retrofitError.toString());
                    showProgress(false);
                }
            });
        }else{
            timecard.setLatitudeOut(mLatitude);
            timecard.setLongitudeOut(mLongitude);
            timecard.setTimestampOut(mDate);
            //clock out
            Service.clockOut(this, timecard, mPhoto, new Callback<Timecard>() {
                @Override
                public void success(Timecard timecard, Response response) {
                    Log.v(TAG, timecard.toString());
                    finish();
                    showProgress(false);
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.v(TAG, retrofitError.toString());
                    showProgress(false);
                }
            });

        }

        showProgress(true);
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        if (show) {
            mProgressHUD = ProgressHUD.show(ConfirmationActivity.this,
                mActionType == ActionsActivity.REQUEST_FROM_CAMERA_CLOCK_IN ? getString(R.string.clocking_in) : getString(R.string.clocking_out),
                true, true, this);
        }else{
            mProgressHUD.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        mProgressHUD.dismiss();
    }
}
