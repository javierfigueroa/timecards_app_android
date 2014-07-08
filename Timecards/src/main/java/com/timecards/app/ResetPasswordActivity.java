package com.timecards.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.timecards.R;
import com.timecards.api.Service;
import com.timecards.app.views.ProgressView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ResetPasswordActivity extends Activity{
    // UI references.
    private EditText mCompanyView;
    private EditText mEmailView;

    private void showAlert(int message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.password_reset);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_reset_password);

        mCompanyView = (EditText) findViewById(R.id.web_address_text);
        mEmailView = (EditText) findViewById(R.id.email_text);

        findViewById(R.id.reset_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reset_password, menu);
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

    public void resetPassword() {
        mCompanyView.setError(null);
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String mCompany = mCompanyView.getText().toString().trim().toLowerCase();
        String mEmail = mEmailView.getText().toString().trim().toLowerCase();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mCompany)) {
            mCompanyView.setError(getString(R.string.error_field_required));
            focusView = mCompanyView;
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
            Service.forgotPassword(this, mEmail, mCompany, new Callback<String>() {
                @Override
                public void success(String s, Response response) {
                    ProgressView.getInstance().dismiss();
                    showAlert(R.string.password_reset_message);
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    ProgressView.getInstance().dismiss();
                    showAlert(R.string.password_reset_message);
                }
            });
        }
    }

}
