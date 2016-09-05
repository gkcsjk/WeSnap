package com.unimelb.wesnap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.unimelb.wesnap.R.*;

/**
 * A login screen that offers login via email/password.
 */
public class EmailPasswordLoginActivity
        extends BaseLoginActivity
        implements View.OnClickListener {

    private static final String TAG = "EmailPasswordLogin";
    private static final String FIELD_REQUIRED = "This field is required";

    private EditText mEmailField;
    private EditText mPasswordField;
    private TextView mStatusTextView;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    // ======================================================
    /*
    * onCreate()
    * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_email_password_login);

        // Views
        mEmailField = (EditText) findViewById(id.field_email);
        mPasswordField = (EditText) findViewById(id.field_password);
        mStatusTextView = (TextView) findViewById(id.status);

        // Buttons
        findViewById(id.email_login_button).setOnClickListener(this);
        findViewById(id.email_register_button).setOnClickListener(this);
        findViewById(id.email_logout_button).setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // notification of auth state changes come from createAccount() & login()

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    // TODO
                    startActivity(new Intent(EmailPasswordLoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    // TODO
                    updateUI(user);
                }

                // updateUI(user);
            }
        };
        // [END auth_state_listener]
    }

    // ======================================================
    /*
    * onStart(): add listener
    * */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    // ======================================================
    /*
    * onStop(): remove listener
    * */
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // ======================================================
    /*
    * onClick(): perform the requested actions by the clicked buttons
    * */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
            case id.email_register_button:
                createAccount(
                        mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case id.email_login_button:
                login(
                        mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case id.email_logout_button:
                logout();
                break;
        }
    }

    // ======================================================
    // private methods
    // ======================================================

    /*
     * createAccount():
     * create account via the given email/password
     * */
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);

        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If createUserWithEmail fails, display a message to the user.
                        // If createUserWithEmail succeeds,
                        // the auth state listener will be notified
                        // and logic to handle the signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "createUserWithEmail:failed", task.getException());
                            Toast.makeText(EmailPasswordLoginActivity.this,
                                    string.status_register_failed,
                                    Toast.LENGTH_SHORT).show();
                            mStatusTextView.setText(string.status_register_failed);
                        }

                        hideProgressDialog();
                    }
                });
        // [END create_user_with_email]
    }

    /*
     * login():
     * login via the given email/password
     * */
    private void login(String email, String password) {
        Log.d(TAG, "login:" + email);

        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user.
                        // If sign in succeeds,
                        // the auth state listener will be notified
                        // and logic to handle the signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(EmailPasswordLoginActivity.this,
                                    string.status_auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            mStatusTextView.setText(string.status_auth_failed);
                        }

                        hideProgressDialog();
                    }
                });
        // [END sign_in_with_email]
    }

    /*
     * logout()
     * */
    private void logout() {
        mAuth.signOut();
        updateUI(null);
    }

    /*
    * validateForm():
    * check the form; return false when invalid
    * */
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError(FIELD_REQUIRED);
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(FIELD_REQUIRED);
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /*
    * updateUI():
    * check if user has logged in and display buttons accordingly
    * */
    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        if (user != null) {
            mStatusTextView.setText(
                    getString(string.status_email_fmt, user.getEmail()));

            findViewById(id.email_password_buttons).setVisibility(View.GONE);
            findViewById(id.email_password_fields).setVisibility(View.GONE);
            findViewById(id.email_logout_button).setVisibility(View.VISIBLE);

        } else {
            mStatusTextView.setText(
                    string.status_logged_out);

            findViewById(id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(id.email_logout_button).setVisibility(View.GONE);
        }
    }
}

