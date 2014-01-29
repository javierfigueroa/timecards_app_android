package com.timecards.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timecards.R;
import com.timecards.api.Service;
import com.timecards.api.model.User;
import com.timecards.libs.ProgressHUD;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity implements DialogInterface.OnCancelListener {
    /**
     * The default email to populate the email field with.
     */
    public static final String PREFS_NAME = "com.company.login.pref_name";
    public static final String LOGIN_STATE = "com.company.login.state";
    public static final String USER = "com.company.login.user";
    public static final String COMPANY = "com.company.login.company";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mCompany;
    private String mEmail;
    private String mPassword;

    // UI references.
    private EditText mCompanyView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;

    private ProgressHUD mProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mCompanyView = (EditText) findViewById(R.id.company);
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        //set username
        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String userJson = preferences.getString(LoginActivity.USER, null);

        if (userJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                User user = mapper.readValue(userJson, User.class);
                mCompanyView.setText(user.getCompany());
                mEmailView.setText(user.getEmail());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    @Override
    public void onBackPressed(){

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        mCompanyView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mCompany = mCompanyView.getText().toString();
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mCompany)) {
            mCompanyView.setError(getString(R.string.error_field_required));
            focusView = mCompanyView;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
//            mAuthTask = new UserLoginTask();
//            mAuthTask.execute((Void) null);


            Service.signIn(getApplicationContext(), mEmail, mPassword, mCompany, new Callback<User>() {
                @Override
                public void success(User user, Response response) {
                    user.setPassword(mPassword);
                    user.setCompany(mCompany);

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(LOGIN_STATE, true);
                    editor.putString(COMPANY, mCompany);

                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        String json = mapper.writeValueAsString(user);
                        editor.putString(USER, json);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    editor.commit();

                    showProgress(false);
                    finish();
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    showProgress(false);
                }
            });
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        if (show) {
            mProgressHUD = ProgressHUD.show(LoginActivity.this, getString(R.string.loading), true, true, this);
        }else{
            mProgressHUD.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        mProgressHUD.dismiss();
    }
}
