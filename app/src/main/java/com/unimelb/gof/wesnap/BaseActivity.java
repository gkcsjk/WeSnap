package com.unimelb.gof.wesnap;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * BaseActivity
 * This hosts the shared methods.
 * Most of the other activities inherit this BaseActivity.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class BaseActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog = null;
    public AlertDialog mExitAppDialog = null;
    public DialogInterface.OnClickListener mExitAppDialogListener;

    // ========================================================
    // ProgressDialog
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.action_loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    // ========================================================
    // AlertDialog: ExitApp
    public void showExitAppDialog() {
        if (mExitAppDialog == null) {
            mExitAppDialog = new AlertDialog.Builder(this).create();

            mExitAppDialogListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int button) {
                    switch(button) {
                        case AlertDialog.BUTTON_POSITIVE:
                            finish();
                            break;
                        case AlertDialog.BUTTON_NEGATIVE:
                            break;
                        default:
                            break;
                    }
                }
            };

            mExitAppDialog.setTitle("Confirm Exit");
            mExitAppDialog.setMessage("Are you sure to exit?");
            mExitAppDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                    mExitAppDialogListener);
            mExitAppDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                    mExitAppDialogListener);
        }

        mExitAppDialog.show();
    }

    public void hideExitAppDialog() {
        if (mExitAppDialog != null && mExitAppDialog.isShowing()) {
            mExitAppDialog.dismiss();
        }
    }

    // ========================================================
    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
        hideExitAppDialog();
    }

    // ========================================================
    // TODO for network failure ???
    public void goToLogin(String message) {
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // ========================================================
}