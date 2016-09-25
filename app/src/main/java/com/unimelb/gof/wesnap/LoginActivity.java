package com.unimelb.gof.wesnap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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

import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.models.User;

/**
 * TODO comments
 */
public class LoginActivity extends BaseActivity
        implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final String FIELD_REQUIRED = "This field is required";
    private static final String TOO_SHORT = "Password should be at least 6 characters.";
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
    private boolean isNewUser;

    // ========================================================
    /*
    * onCreate()
    * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI components
        mLoginUI = (ViewGroup) findViewById(R.id.ui_group_login);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);
        mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(this);

        mRegisterUI = (ViewGroup) findViewById(R.id.ui_group_register);
        mRegisterUI.setVisibility(View.GONE);
        mRegUsernameField = (EditText) findViewById(R.id.field_username_r);
        mRegDisplayedNameField = (EditText) findViewById(R.id.field_display_name_r);
        mRegEmailField = (EditText) findViewById(R.id.field_email_r);
        mRegPasswordField = (EditText) findViewById(R.id.field_password_r);
        mRegSubmitButton = (Button) findViewById(R.id.register_submit_button);
        mRegSubmitButton.setOnClickListener(this);
        mRegCancelButton = (Button) findViewById(R.id.register_cancel_button);
        mRegCancelButton.setOnClickListener(this);

        // Firebase Auth
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

                    // for new user: save the user to Firebase Database
                    if (isNewUser) {
                        User mNewUser = new User(
                                mRegUsernameField.getText().toString(),
                                mRegDisplayedNameField.getText().toString(),
                                mRegEmailField.getText().toString(),
                                null );
                        FirebaseUtil.getCurrentUserRef().setValue(mNewUser);
                        FirebaseUtil.getUsernamesRef()
                                .child(mRegUsernameField.getText().toString())
                                .setValue(user.getUid());

                        // hide loading
                        hideProgressDialog();
                    }

                    // direct to main
                    goToMain();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    // reset to login
                    showLogin();
                }
            }
        };
        // [END auth_state_listener]
    }

    // ========================================================
    /*
    * onStart(): add listener
    * */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    // ========================================================
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

    // ========================================================
    /*
    * onClick(): perform the requested actions by the clicked buttons
    * */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch(i) {
            case R.id.login_button:
                isNewUser = false;
                login(mEmailField.getText().toString(),
                        mPasswordField.getText().toString());
                break;
            case R.id.register_button:
                showRegister();
                break;
            case R.id.register_submit_button:
                isNewUser = true;
                register(mRegEmailField.getText().toString(),
                        mRegPasswordField.getText().toString());
                break;
            case R.id.register_cancel_button:
                showLogin();
                break;
        }
    }

    // ========================================================
    // login and register logic

    /* show the REGISTER screen */
    private void showRegister() {
        ((TextView) findViewById(R.id.text_title_login))
                .setText(R.string.text_title_register);
        mLoginUI.setVisibility(View.GONE);
        mRegisterUI.setVisibility(View.VISIBLE);
    }

    /* show the LOGIN screen */
    private void showLogin() {
        ((TextView) findViewById(R.id.text_title_login))
                .setText(R.string.text_title_login);
        mLoginUI.setVisibility(View.VISIBLE);
        mRegisterUI.setVisibility(View.GONE);
    }

    /*
     * login(): login via the given email/password
     * */
    private void login(String email, String password) {
        Log.d(TAG, "login:" + email);

        EditText[] fields = new EditText[]{mEmailField, mPasswordField};

        // input validation
        if (!validateEmpty(fields)) {
            return;
        }

        // show loading
        showProgressDialog();

        // [START sign_in_with_email]
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
                        }

                        // If sign in succeeds, the auth state listener will be notified
                        // and logic to handle the signed in user can be handled in the listener.

                        // hide loading
                        hideProgressDialog();
                    }
                });
        // [END sign_in_with_email]
    }

    /**
     * register(): create account via the given username/name/email/password
     * */
    private void register(String email, String password) {
        Log.d(TAG, "register:" + email);

        // input validation
        EditText[] fields = new EditText[]{
                mRegUsernameField, mRegDisplayedNameField,
                mRegEmailField, mRegPasswordField
        };
        if (!validateEmpty(fields)) {
            return;
        }
        if (password.length() < 6) {
            mRegPasswordField.setError(TOO_SHORT);
            return;
        }

        // show loading
        showProgressDialog();
        mRegSubmitButton.setVisibility(View.GONE);

        FirebaseUtil.getUsernamesRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String username = mRegUsernameField.getText().toString();

                        Log.w(TAG, "getUsername:onDataChange" + username);

                        if (dataSnapshot.hasChild(username)) {
                            mRegUsernameField.setError(USERNAME_EXIST);
                            mRegSubmitButton.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        } else { // isUnique = true;
                            createAccount();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                    }
                });
    }

    private void createAccount() {
        // create new Firebase account
        mAuth.createUserWithEmailAndPassword(
                mRegEmailField.getText().toString(),
                mRegPasswordField.getText().toString())
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
    }

    /* validateEmpty(): check empty fields in the form; return false when invalid */
    private boolean validateEmpty(EditText[] fields) {
        boolean valid = true;

        // check empty
        for ( EditText f : fields ) {
            if (TextUtils.isEmpty(f.getText().toString())) {
                f.setError(FIELD_REQUIRED);
                valid = false;
            } else {
                f.setError(null);
            }
        }

        return valid;
    }

    // ========================================================
    // navigation

    /* directs to the main screen */
    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

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
