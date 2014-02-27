package com.timecards.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timecards.R;
import com.timecards.api.RestConstants;
import com.timecards.api.Service;
import com.timecards.api.model.Project;
import com.timecards.api.model.Timecard;
import com.timecards.api.model.User;
import com.timecards.libs.ProgressHUD;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ActionsActivity extends Activity implements DialogInterface.OnCancelListener {
    public static final int REQUEST_FROM_CAMERA_CLOCK_IN = 1;

    public static final int REQUEST_FROM_CAMERA_CLOCK_OUT = 2;

    public static final int REQUEST_FROM_PROJECTS = 3;

    public final static long SECOND_MILLIS = 1000;
    public final static long MINUTE_MILLIS = SECOND_MILLIS*60;
    public final static long HOUR_MILLIS = MINUTE_MILLIS*60;
    public final static long DAY_MILLIS = HOUR_MILLIS*24;

    public static final String TAG = ActionsActivity.class.getSimpleName();

    public final static String GPS_LATITUDE = "GPS_LATITUDE";

    public final static String GPS_LONGITUDE = "GPS_LONGITUDE";

    public final static String ACTION_TYPE = "ACTION_TYPE";

    public static List<Project> mProjects;

    private LocationManager mLocationManager;

    private LocationListener mLocationListener;

    private ProgressHUD mProgressHUD;

    private Location mLocation;

    private Boolean inBackground;

    private AlertDialog mLocationServicesDialog;

    private final static String [] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this;

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_actions);

        String dateValue = new SimpleDateFormat("LLL dd, yyyy").format(new Date());
        TextView dateLabel = (TextView)findViewById(R.id.date_textView);
        dateLabel.setText(dateValue);

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

            public void onProviderEnabled(String provider) {
                Log.d(TAG, "Location provider enabled");
                //Close dialog if already open
                if (mLocationServicesDialog != null) {
                    mLocationServicesDialog.dismiss();
                }
            }

            public void onProviderDisabled(String provider) {
                Log.d(TAG, "Location provider disabled");

                //Close dialog if already open
                if (mLocationServicesDialog == null || !mLocationServicesDialog.isShowing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.gps_not_found_title);  // GPS not found
                    builder.setMessage(R.string.gps_not_found_message); // Want to enable?
                    builder.setPositiveButton(R.string.set_location_services, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
                    mLocationServicesDialog = builder.create();
                    mLocationServicesDialog.show();
                }
                return;
            }
        };

        // Register the listener with the Location Manager to receive location updates
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        if (settings.getBoolean(LoginActivity.LOGIN_STATE, false) == true) {
            String company = settings.getString(LoginActivity.COMPANY, "");
            RestConstants.getInstance().setCompany(company);

            Timecard timecard = CurrentTimecard.getCurrentTimecard();
            if (timecard.getTimestampIn() != null) {
                setButtonStates();
            }else{
                getTodaysTimecard();
            }
        }
    }

    private void getTodaysTimecard() {
        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
            showProgress(true);
            Service.getToday(getApplicationContext(), new Callback<Timecard>() {
                @Override
                public void success(Timecard timecard, Response response) {
                    //Set soft timecard
                    CurrentTimecard.setCurrentTimecard(timecard);
                    //Set button states
                    setButtonStates();
                    showProgress(false);
                    getProjects();
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    showProgress(false);
                    setButtonStates();
                }
            });
        }
    }

    private void getProjects() {
        Service.getProjects(getApplicationContext(), new Callback<List<Project>>() {
            @Override
            public void success(List<Project> projects, Response response) {
                if (!projects.isEmpty()) {
                    Button secondaryButton = (Button) findViewById(R.id.secondary_action_button);

                    Timecard timecard = CurrentTimecard.getCurrentTimecard();
                    if (timecard.getTimestampIn() != null) {
                        secondaryButton.setVisibility(View.VISIBLE);
                        setProjectInButton(projects, secondaryButton, timecard);
                    }

                    mProjects = new ArrayList<Project>();
                    mProjects.addAll(projects);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }

    private void setProjectInButton(List<Project> projects, Button secondaryButton, Timecard timecard) {
        for (Project project : projects) {
            if (project.getId().equalsIgnoreCase(timecard.getProjectId())) {
                secondaryButton.setText(project.getName());
                break;
            }
        }
    }

    private void setButtonStates() {
        Timecard timecard = CurrentTimecard.getCurrentTimecard();

        //set username
        setUser();

        //set avatar
        setAvatar(timecard);

        if (timecard.getTimestampIn() == null) {
            setEmptyTimecardState();
        }else{
            setNonEmptyTimecardState(timecard);
        }
    }

    private void setUser() {
        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String userJson = preferences.getString(LoginActivity.USER, null);

        ObjectMapper mapper = new ObjectMapper();
        try {
            User user = mapper.readValue(userJson, User.class);
            TextView usernameLabel = (TextView) findViewById(R.id.username_textView);
            usernameLabel.setText(user.getFirstName() + " " + user.getLastName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setNonEmptyTimecardState(Timecard timecard) {
        Button clockInButton = (Button) findViewById(R.id.clock_in_button);
        FrameLayout timecardLayout = (FrameLayout) findViewById(R.id.timecard_layout);
        Button secondaryButton = (Button) findViewById(R.id.secondary_action_button);
        TextView coachView = (TextView) findViewById(R.id.coach_textview);
        final Context context = this;

        clockInButton.setText(R.string.clock_out_button);
        clockInButton.setBackgroundResource(R.drawable.redbtn);
        clockInButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, CameraActivity.class);
                startActivityForResult(intent, REQUEST_FROM_CAMERA_CLOCK_OUT);
            }
        });

        //set day of the week
        Calendar c = Calendar.getInstance();
        c.setTime(timecard.getTimestampIn());

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        TextView dayOfWeekLabel = (TextView)findViewById(R.id.dayOfWeek_textView);
        dayOfWeekLabel.setText(daysOfWeek[dayOfWeek-1]);

        //set logged time
        Date now = new Date();
        int hours = (int)(((now.getTime() - timecard.getTimestampIn().getTime())/1000)/3600);
        int minutes = (int)((now.getTime()/MINUTE_MILLIS) - (timecard.getTimestampIn().getTime()/MINUTE_MILLIS)) - (hours * 60);
        TextView timeLoggedLabel = (TextView)findViewById(R.id.timeLogged_textView);
        timeLoggedLabel.setText(hours + "h:" + minutes + "m");

        //time
        String timeValue = new SimpleDateFormat("h:mm").format(timecard.getTimestampIn());
        TextView timeLabel = (TextView)findViewById(R.id.hhmm_textView);
        timeLabel.setText(timeValue);
        String AMPMValue = new SimpleDateFormat("a").format(timecard.getTimestampIn());
        TextView AMPMLabel = (TextView)findViewById(R.id.amPm_textView);
        AMPMLabel.setText(AMPMValue);

        timecardLayout.setVisibility(View.VISIBLE);
        coachView.setVisibility(View.INVISIBLE);

        if (mProjects != null && !mProjects.isEmpty()) {
            secondaryButton.setVisibility(View.VISIBLE);
        }
    }

    private void setEmptyTimecardState() {
        Button clockInButton = (Button) findViewById(R.id.clock_in_button);
        FrameLayout timecardLayout = (FrameLayout) findViewById(R.id.timecard_layout);
        Button secondaryButton = (Button) findViewById(R.id.secondary_action_button);
        TextView coachView = (TextView) findViewById(R.id.coach_textview);
        final Context context = this;

        clockInButton.setText(R.string.clock_in_button);
        clockInButton.setBackgroundResource(R.drawable.greenbtn);
        clockInButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, CameraActivity.class);
                startActivityForResult(intent, REQUEST_FROM_CAMERA_CLOCK_IN);
            }
        });

        timecardLayout.setVisibility(View.INVISIBLE);

        secondaryButton.setText(R.string.assign_project);
        secondaryButton.setVisibility(View.INVISIBLE);
        coachView.setVisibility(View.VISIBLE);
    }

    private void setAvatar(Timecard timecard) {
        final Context context = this;
        ImageView avatar = (ImageView)findViewById(R.id.avatar_imageView);
        Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.profileavatar);
        avatar.setImageBitmap(timecard.getPhotoInUrl() == null ?
                   getCircledBitmap(image) : getBitmapFromURL(timecard.getPhotoInUrl()));
        avatar.setVisibility(View.VISIBLE);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /**
     * Called when the user clicks the projects button
     */
    public void onSelectProject(View view) {
        Intent projectsActivityIntent = new Intent(this, ProjectsActivity.class);
        startActivityForResult(projectsActivityIntent, REQUEST_FROM_PROJECTS);
    }

    /**
     * Called when the user clicks the Send button
     */
    public void signOut(View view) {
        //Confirmation dialog before sign out
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_out);
        builder.setMessage(R.string.are_you_sure);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(LoginActivity.LOGIN_STATE, false);
                editor.commit();

                showLoginActivity();
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();
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
                        submitTimecard(rotateImageBytes(photo), requestCode);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

        }else if (requestCode == REQUEST_FROM_PROJECTS) {
            Timecard timecard = CurrentTimecard.getCurrentTimecard();
            Button secondaryButton = (Button) findViewById(R.id.secondary_action_button);
            setProjectInButton(mProjects, secondaryButton, timecard);
        }
    }

    public void submitTimecard(byte[] mPhoto, int mActionType) {
        Timecard timecard = CurrentTimecard.getCurrentTimecard();
        final Activity activity = this;

        if (mActionType == ActionsActivity.REQUEST_FROM_CAMERA_CLOCK_IN) {
            timecard.setLatitudeIn(mLocation.getLatitude());
            timecard.setLongitudeIn(mLocation.getLongitude());
            timecard.setTimestampIn(new Date());
            //clock in
            Service.clockIn(this, timecard, mPhoto, new Callback<Timecard>() {
                @Override
                public void success(Timecard timecard, Response response) {
                    Log.v(TAG, timecard.toString());
                    CurrentTimecard.setCurrentTimecard(timecard);
                    showProgress(false);
                    setButtonStates();
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.v(TAG, retrofitError.toString());
                    showProgress(false);
                }
            });
        }else{
            timecard.setLatitudeOut(mLocation.getLatitude());
            timecard.setLongitudeOut(mLocation.getLongitude());
            timecard.setTimestampOut(new Date());
            //clock out
            Service.clockOut(this, timecard, mPhoto, new Callback<Timecard>() {
                @Override
                public void success(Timecard timecard, Response response) {
                    CurrentTimecard.setCurrentTimecard(new Timecard());
                    Log.v(TAG, timecard.toString());
                    showProgress(false);
                    setButtonStates();
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

    public static Bitmap getBitmapFromURL(String src) {
        try{
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(src).getContent());
            return getCircledBitmap(bitmap);
        }  catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] rotateImageBytes(byte[] bytes) {
        try {
            Bitmap thePicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Matrix m = new Matrix();
            m.postRotate(CameraPreview.ROTATION_DEGREES);
            thePicture = Bitmap.createBitmap(thePicture, 0, 0, thePicture.getWidth(), thePicture.getHeight(), m, true);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            thePicture.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap getCircledBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);


        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2,
                bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
