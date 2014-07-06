package com.timecards.app.views;

import android.content.Context;
import android.content.DialogInterface;

import com.timecards.libs.ProgressHUD;

/**
 * Created by javier on 7/6/14.
 */
public class ProgressView implements DialogInterface.OnCancelListener{

    private static ProgressView view;

    private ProgressHUD mProgressHUD;

    private ProgressView(){

    }

    public static ProgressView getInstance(){
        if (view == null) {
            view = new ProgressView();
        }

        return view;
    }


    public void showProgress(Context context, String text) {
        if (mProgressHUD == null || !mProgressHUD.isShowing()) {
            mProgressHUD = ProgressHUD.show(context, text, true, true, this);
        }
    }

    public void dismiss() {
        if (mProgressHUD != null) {
            mProgressHUD.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        dismiss();
    }
}
