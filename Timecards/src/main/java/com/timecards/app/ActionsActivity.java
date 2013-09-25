package com.timecards.app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.timecards.R;
import com.timecards.api.RestConstants;
import com.timecards.api.Service;
import com.timecards.api.model.Timecard;
import com.timecards.libs.ProgressHUD;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ActionsActivity extends Activity implements DialogInterface.OnCancelListener {
    public static final int REQUEST_FROM_CAMERA_CLOCK_IN = 1;

    public static final int REQUEST_FROM_CAMERA_CLOCK_OUT = 2;

    public final static String GPS_LATITUDE = "GPS_LATITUDE";

    public final static String GPS_LONGITUDE = "GPS_LONGITUDE";

    public final static String ACTION_TYPE = "ACTION_TYPE";

    private LocationManager mLocationManager;

    private LocationListener mLocationListener;

    private ProgressHUD mProgressHUD;

    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_actions);

        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        if (settings.getBoolean(LoginActivity.LOGIN_STATE, false) == false) {
            showLoginActivity();
        }

        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                mLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
    }

    @Override
    protected void onStart() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        if (settings.getBoolean(LoginActivity.LOGIN_STATE, false) == true) {
            String company = settings.getString(LoginActivity.COMPANY, "");
            RestConstants.getInstance().setCompany(company);
            showProgress(true);

            Service.getToday(getApplicationContext(), new Callback<Timecard>() {
                @Override
                public void success(Timecard timecard, Response response) {
                    //Set soft timecard
                    CurrentTimecard.setCurrentTimecard(timecard);
                    //Set button states

                    Button clockInButton = (Button) findViewById(R.id.clock_in_button);
                    Button clockOutButton = (Button) findViewById(R.id.clock_out_button);

                    if (timecard.getTimestampIn() == null) {
                        clockInButton.setText(R.string.clock_in_button);
                        clockInButton.setEnabled(true);
                        clockOutButton.setEnabled(false);
                    }else{
                        clockInButton.setText(R.string.clock_in_clocked_button);
                        clockInButton.setEnabled(false);
                        clockOutButton.setEnabled(true);
                    }

                    showProgress(false);
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    showProgress(false);
                }
            });
        }

    }

    private void showLoginActivity(){
        //Show login view
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    /**
     * Called when the user clicks the Send button
     */
    public void clockIn(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_FROM_CAMERA_CLOCK_IN);
    }

    /**
     * Called when the user clicks the Send button
     */
    public void clockOut(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_FROM_CAMERA_CLOCK_OUT);
    }

    /**
     * Called when the user clicks the Send button
     */
    public void signOut(View view) {

        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(LoginActivity.LOGIN_STATE, false);
        editor.commit();

        showLoginActivity();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        if (show) {
            mProgressHUD = ProgressHUD.show(ActionsActivity.this, getString(R.string.loading), true, true, this);
        }else{
            mProgressHUD.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_FROM_CAMERA_CLOCK_IN || requestCode == REQUEST_FROM_CAMERA_CLOCK_OUT)
                && resultCode == RESULT_OK) {

                try {
                    byte[] photo = (byte[]) data.getByteArrayExtra(CameraActivity.IMAGE_FROM_CAMERA);
                    double latitude = mLocation.getLatitude();
                    double longitude = mLocation.getLongitude();

                    if (photo != null && latitude != 0) {
                        Intent confirmationActivityIntent = new Intent(this, ConfirmationActivity.class);

                        confirmationActivityIntent.putExtra(CameraActivity.IMAGE_FROM_CAMERA, photo);
                        confirmationActivityIntent.putExtra(GPS_LATITUDE, latitude);
                        confirmationActivityIntent.putExtra(GPS_LONGITUDE, longitude);
                        confirmationActivityIntent.putExtra(ACTION_TYPE, requestCode);

                        startActivity(confirmationActivityIntent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

        }

    }

    @Override
    public void onBackPressed() {

        // Remove the listener you previously added
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        mProgressHUD.dismiss();
    }
}
