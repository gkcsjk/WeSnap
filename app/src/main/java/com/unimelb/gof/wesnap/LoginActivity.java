package com.unimelb.gof.wesnap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import com.unimelb.gof.wesnap.models.*;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;

/**
 * LoginActivity
 * For user register and login.
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private static final String FIELD_REQUIRED = "This field is required";
    private static final int MIN_LENGTH = 6;
    private static final String TOO_SHORT = "require at least "+MIN_LENGTH+" characters";
    public static final String NOT_ALLOWED_CHAR = ". $ # [ ] / are not allowed";
    private static final String USERNAME_EXIST = "Username exists. Try again.";

    /* UI components */
    // login
    private ViewGroup mLoginUI;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mLoginButton;
    private Button mRegisterButton;
    // register
    private ViewGroup mRegisterUI;
    private EditText mRegUsernameField;
    private EditText mRegDisplayedNameField;
    private EditText mRegEmailField;
    private EditText mRegPasswordField;
    private Button mRegSubmitButton;
    private Button mRegCancelButton;

    /* Firebase Auth variables */
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean isNewUser; /* set to true for new register */

    // ========================================================
    /* onCreate()
     * (Change of user auth state is handled here by the listener.) */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        /* UI components */
        setContentView(R.layout.activity_login);
        // login
        mLoginUI = (ViewGroup) findViewById(R.id.ui_group_login);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        mLoginButton = (Button) findViewById(R.id.button_login);
        mLoginButton.setOnClickListener(this);
        mRegisterButton = (Button) findViewById(R.id.button_register);
        mRegisterButton.setOnClickListener(this);
        // register
        mRegisterUI = (ViewGroup) findViewById(R.id.ui_group_register);
        mRegisterUI.setVisibility(View.GONE);
        mRegUsernameField = (EditText) findViewById(R.id.field_username_r);
        mRegDisplayedNameField = (EditText) findViewById(R.id.field_display_name_r);
        mRegEmailField = (EditText) findViewById(R.id.field_email_r);
        mRegPasswordField = (EditText) findViewById(R.id.field_password_r);
        mRegSubmitButton = (Button) findViewById(R.id.button_submit_register);
        mRegSubmitButton.setOnClickListener(this);
        mRegCancelButton = (Button) findViewById(R.id.button_cancel_register);
        mRegCancelButton.setOnClickListener(this);

        /* Firebase Auth */
        mAuth = FirebaseAuth.getInstance();
        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Listen for Firebase Auth state changes
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in: direct to Main activity
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    // for new user: save the user info to Firebase Database
                    if (isNewUser) {
                        saveNewUser(user.getUid());
                    }
                    hideProgressDialog();

                    // direct to main
                    goToMain();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    // reset to login
                    hideProgressDialog();
                    showLogin();
                }
            }
        };
        // [END auth_state_listener]
    }

    // ========================================================
    /* saveNewUser(): save account info to Firebase Database, and
     * send out the initial messages */
    private void saveNewUser(String newUid) {
        Log.d(TAG, "saveNewUser:id=" + newUid);
        String newName = mRegDisplayedNameField.getText().toString();

        // initial Chat & Message from Dev Team
        Chat newChat = AppParams.getWelcomeChat(newUid, newName);
        DatabaseReference newChatRef = FirebaseUtil.getChatsRef().push();
        String newChatId = newChatRef.getKey();
        newChatRef.setValue(newChat);

        Message newMessage = AppParams.getWelcomeMessage();
        FirebaseUtil.getMessagesRef().child(newChatId).push().setValue(newMessage);

        // new user & username to database
        User newUser = new User(
                newUid, mRegUsernameField.getText().toString(),
                newName, mRegEmailField.getText().toString(),
                AppParams.ID_DEV_TEAM, newChatId
        );
        FirebaseUtil.getUsersRef().child(newUid).setValue(newUser);
        FirebaseUtil.getUsernamesRef().child(mRegUsernameField.getText().toString()).setValue(newUid);
    }

    // ========================================================
    /* onStart(): add listener */
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mAuth.addAuthStateListener(mAuthListener);
    }

    // ========================================================
    /* onStop(): remove listener */
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // ========================================================
    /* onClick(): perform the requested actions by the clicked buttons */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
            case R.id.button_login:
                isNewUser = false;
                login();
                break;
            case R.id.button_register:
                showRegister();
                break;
            case R.id.button_submit_register:
                isNewUser = true;
                register();
                break;
            case R.id.button_cancel_register:
                showLogin();
                break;
        }
    }

    // ========================================================
    // login logic

    /* show the LOGIN screen */
    private void showLogin() {
        ((TextView) findViewById(R.id.text_title_login)).setText(R.string.text_title_login);
        mLoginUI.setVisibility(View.VISIBLE);
        mRegisterUI.setVisibility(View.GONE);
    }

    /* login(): login via the given email/password */
    private void login() {
        final String email = mEmailField.getText().toString();
        final String password = mPasswordField.getText().toString();
        final EditText[] fields = new EditText[]{mEmailField, mPasswordField};
        final String[] values = new String[]{email, password};
        // input validation
        if (!validateEmpty(fields, values)) {
            return;
        }

        // show loading
        mLoginButton.setVisibility(View.GONE);
        mRegisterButton.setVisibility(View.GONE);
        showProgressDialog();

        // [START sign_in_with_email]
        Log.d(TAG, "login:email=" + email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this,
                                    R.string.status_auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            mLoginButton.setVisibility(View.VISIBLE);
                            mRegisterButton.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        }

                        // If sign in succeeds, the auth state listener will be notified
                        // and logic to handle the signed in user can be handled in the listener.
                    }
                });
        // [END sign_in_with_email]
    }

    // ========================================================
    // register logic

    /* show the REGISTER screen */
    private void showRegister() {
        ((TextView) findViewById(R.id.text_title_login)).setText(R.string.text_title_register);
        mLoginUI.setVisibility(View.GONE);
        mRegisterUI.setVisibility(View.VISIBLE);
    }

    /* register(): try to create a new user account */
    private void register() {
        final String username = mRegUsernameField.getText().toString();
        final String displayedName = mRegDisplayedNameField.getText().toString();
        final String email = mRegEmailField.getText().toString();
        final String password = mRegPasswordField.getText().toString();
        final EditText[] fields = new EditText[]{mRegUsernameField,
                mRegDisplayedNameField, mRegEmailField, mRegPasswordField};
        final String[] values = new String[]{username, displayedName, email, password};

        // input validation
        if (!validateEmpty(fields, values)) {
            return;
        }
        if (password.length() < MIN_LENGTH) {
            mRegPasswordField.setError(TOO_SHORT);
            return;
        }
        for (String s : FirebaseUtil.NOT_ALLOWED_CHAR) {
            // Firebase "key" can include any unicode characters except for
            // . $ # [ ] / and ASCII control characters 0-31 and 127
            if (username.contains(s)) {
                mRegUsernameField.setError(NOT_ALLOWED_CHAR);
                return;
            }
        }

        // update UI
        mRegSubmitButton.setVisibility(View.GONE);
        showProgressDialog();

        // [START check_username_unique]
        Log.d(TAG, "register:email=" + email);
        FirebaseUtil.getUsernamesRef().child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w(TAG, "checkUsername:onDataChange");
                        if (dataSnapshot.exists()) { // isUnique = false;
                            mRegUsernameField.setError(USERNAME_EXIST);
                            mRegSubmitButton.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        } else {
                            // isUnique = true;
                            createAccount(email, password, fields);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "checkUsername:onCancelled", databaseError.toException());
                    }
                });
        // [END check_username_unique]
    }

    /* createAccount(): create account via the given username/name/email/password */
    private void createAccount(String email, String password, final EditText[] fields) {
        Log.d(TAG, "createAccount:email=" + email);

        // [START create_new_acccount]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If createUserWithEmail fails, display a message to the user.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "createUserWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this,
                                    R.string.status_register_failed,
                                    Toast.LENGTH_SHORT).show();
                            isNewUser = false;
                            mRegSubmitButton.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        }

                        // If createUserWithEmail succeeds, the auth state listener will be notified
                        // and logic to handle the signed in user can be handled in the listener.
                    }
                });
        // [END create_new_acccount]
    }

    // ========================================================
    // input data validation

    /* validateEmpty(): check empty fields in the form; return false when invalid */
    private boolean validateEmpty(EditText[] fields, String[] values) {
        boolean valid = true;
        int i;
        // check empty
        for (i = 0; i < fields.length; i++) {
            if (values[i] == null || values[i].length() < 1) {
                fields[i].setError(FIELD_REQUIRED);
                valid = false;
            } else {
                fields[i].setError(null);
            }
        }
        return valid;
    }

    // ========================================================
    // navigation

    /* Directs to the main screen */
    private void goToMain() {
        Log.d(TAG, "goToMain");

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /* Let back key triggers exit app dialog */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Exit the app after confirming via dialog;
            // only show the Dialog when the activity is not finished
            if (!isFinishing()) {
                showExitAppDialog();
            }
            return true;
        }
        return false;
    }
}
