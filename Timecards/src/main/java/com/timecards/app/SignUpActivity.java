package com.timecards.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timecards.R;
import com.timecards.api.Service;
import com.timecards.api.model.User;
import com.timecards.app.views.ProgressView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SignUpActivity extends Activity {
    // UI references.
    private EditText mCompanyView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mEmailView;
    private EditText mPasswordView;

    private AlertDialog.Builder getDialogBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_up);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder;
    }

    private void showAlert(int message){
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setMessage(message);
        builder.create().show();
    }

    private void showAlert(String message){
        AlertDialog.Builder builder = getDialogBuilder();
        builder.setMessage(message);
        builder.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sign_up);

        mCompanyView = (EditText) findViewById(R.id.web_address_text);
        mEmailView = (EditText) findViewById(R.id.email_text);
        mFirstNameView = (EditText) findViewById(R.id.first_name_text);
        mLastNameView = (EditText) findViewById(R.id.last_name_text);
        mPasswordView = (EditText) findViewById(R.id.password_text);

        findViewById(R.id.sign_up_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void signup() {
        mCompanyView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);

        final String mCompany = mCompanyView.getText().toString().trim().toLowerCase();
        final String mEmail = mEmailView.getText().toString().trim().toLowerCase();
        final String mFirstName = mFirstNameView.getText().toString().trim().toLowerCase();
        final String mLastName = mLastNameView.getText().toString().trim().toLowerCase();
        final String mPassword = mPasswordView.getText().toString().trim().toLowerCase();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mCompany)) {
            mCompanyView.setError(getString(R.string.error_field_required));
            focusView = mCompanyView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mFirstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mLastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
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
            ProgressView.getInstance().showProgress(this, getString(R.string.loading));
            Service.signUp(this, mEmail, mPassword, mFirstName, mLastName, mCompany, new Callback<User>() {
                @Override
                public void success(User user, Response response) {
                    ProgressView.getInstance().dismiss();
                    if(user.getErrors() != null && user.getErrors().size() > 0) {
                        StringBuffer stringBuffer = new StringBuffer();
                        for (String key : user.getErrors().keySet()) {
                            if (key.equalsIgnoreCase("base")) {
                                stringBuffer.append(user.getErrors().get(key)[0] + "\n");
                            }else{
                                stringBuffer.append(key + " " + user.getErrors().get(key)[0] + "\n");
                            }
                        }

                        showAlert(stringBuffer.toString());
                    }else{
                        user.setPassword(mPassword);
                        user.setCompany(mCompany);

                        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(LoginActivity.LOGIN_STATE, true);
                        editor.putString(LoginActivity.COMPANY, mCompany);

                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            String json = mapper.writeValueAsString(user);
                            editor.putString(LoginActivity.USER, json);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                        editor.commit();
                        setResult(RESULT_OK, null);
                        finish();
                    }
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    ProgressView.getInstance().dismiss();
                    showAlert(R.string.sign_up_message_error);
                }
            });

        }
    }
}
