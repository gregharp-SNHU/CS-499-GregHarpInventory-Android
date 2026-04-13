package com.mobile2app.gregharpinventory.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.util.Toaster;
import com.mobile2app.gregharpinventory.model.Roles;
import com.mobile2app.gregharpinventory.model.Prefs;
import com.mobile2app.gregharpinventory.model.DbKeys;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    // variables attached to UI components
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    // prefs instance and labels for the user account and role for preferences
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get preferences
        prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE);

        // initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // check if already logged in -- Firebase session and prefs must both be valid
        if (mAuth.getCurrentUser() != null && !prefs.getString(Prefs.KEY_USERNAME, "").isEmpty()) {
            // user is already logged in — skip to InventoryActivity
            startActivity(new Intent(LoginActivity.this, InventoryActivity.class));
            finish();

            // prevent executing rest of onCreate
            return;
        }

        // set up the view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // attach the UI variables to their components on the screen
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        // ensure the Login button is disabled by default
        loginButton.setEnabled(false);

        // add text changed listener to enable the Login button
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // enable the login button if the text is non-zero in length
                validateUsernameAndPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nothing to do here
            }
        };

        // add the TextWatcher to both text fields
        usernameEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);

        // let the Enter key trigger the Login button
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (loginButton.isEnabled()) {
                loginButton.performClick();
            }

            // always consume the event to keep from triggering the Create Account button
            return true;
        });

        // create the connection from the Login button to take the user to the Inventory screen
        loginButton.setOnClickListener(v -> {
            // check if the Login button is disabled
            if (!loginButton.isEnabled())
                return;

            // disable the login button to prevent double-click
            loginButton.setEnabled(false);

            // get the username and password
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // authenticate with Firebase using username (email) and password
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Firebase authentication succeeded -- get the current user
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            if (firebaseUser != null) {
                                // look up the user's role and phone from Firestore
                                FirebaseFirestore.getInstance()
                                    .collection(DbKeys.USERS_COLL)
                                    .document(firebaseUser.getUid())
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        // save user info in SharedPreferences
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString(Prefs.KEY_USERNAME, firebaseUser.getEmail());

                                        if (doc.exists()) {
                                            // use the role and phone from Firestore
                                            editor.putString(Prefs.KEY_PHONE, doc.getString(DbKeys.PHONE));
                                            editor.putString(Prefs.KEY_ROLE, doc.getString(DbKeys.ROLE));
                                        } else {
                                            // default to basic user role if no Firestore record exists
                                            editor.putString(Prefs.KEY_PHONE, "");
                                            editor.putString(Prefs.KEY_ROLE, Roles.USER);
                                        }

                                        editor.apply();

                                        // cancel any outstanding Toasts
                                        Toaster.cancel();

                                        // go to the inventory screen
                                        startActivity(new Intent(LoginActivity.this,
                                                InventoryActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Firestore lookup failed -- notify the user
                                        Toaster.show(LoginActivity.this,
                                                getString(R.string.account_load_failed));

                                        // re-enable the Login button so they can retry
                                        loginButton.setEnabled(true);
                                    });
                            } else {
                                // unexpected state -- auth succeeded but no user session
                                Toaster.show(LoginActivity.this,
                                        getString(R.string.invalid_login));
                                loginButton.setEnabled(true);
                            }
                        } else {
                            // Firebase authentication failed -- notify the user
                            Toaster.show(LoginActivity.this, R.string.invalid_login);

                            // re-enable the Login button
                            loginButton.setEnabled(true);

                            // focus on the password field
                            passwordEditText.requestFocus();
                            passwordEditText.selectAll();
                        }
                    });
        });

        // wire up the Forgot Password button
        Button forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        forgotPasswordButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();

            // ensure the user has entered an email address
            if (email.isEmpty()) {
                Toaster.show(this, getString(R.string.enter_email_first));
                usernameEditText.requestFocus();
                return;
            }

            // send the password reset email through Firebase
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toaster.show(this, getString(R.string.reset_email_sent)))
                    .addOnFailureListener(e ->
                            Toaster.show(this, getString(R.string.reset_email_failed)));
        });

        // create the connection from the Create Account button to that activity
        createAccountButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });

        // pre-populate the username if it's saved in the preferences
        prefillUsername();

        // update login button state after prefill
        validateUsernameAndPassword();
    }

    // clear any outstanding Toasts on pause
    @Override
    protected void onPause() {
        super.onPause();
        Toaster.cancel();
    }

    // pre-populate the username if it's saved in preferences whenever LoginActivity starts
    @Override
    protected void onStart() {
        super.onStart();

        // fetch the username, if it is populated
        prefillUsername();

        // update login button state after prefill
        validateUsernameAndPassword();
    }

    // function to prefill the username if saved in preferences
    private void prefillUsername() {
        String savedUsername = prefs.getString(Prefs.KEY_USERNAME, "");
        if (!savedUsername.isEmpty()) {
            // populate the username and set focus to the password field
            usernameEditText.setText(savedUsername);
            usernameEditText.setSelection(savedUsername.length());
            passwordEditText.setText("");
            passwordEditText.requestFocus();
        }
    }

    // function to determine if username and password are non-blank
    private void validateUsernameAndPassword () {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // if the username and password are not blank, enable the login button
        loginButton.setEnabled(!username.isEmpty()
                && !password.isEmpty());
    }
}