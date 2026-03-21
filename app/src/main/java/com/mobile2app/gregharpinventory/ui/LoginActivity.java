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
import com.mobile2app.gregharpinventory.data.AppDatabase;
import com.mobile2app.gregharpinventory.data.UserDao;
import com.mobile2app.gregharpinventory.model.User;
import com.mobile2app.gregharpinventory.util.Toaster;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    // variables attached to UI components
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button createAccountButton;

    // prefs instance and labels for the user account and role for preferences
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get preferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // check if already logged in -- if so jump straight to inventory
        if (!prefs.getString(KEY_USERNAME, "").isEmpty()) {
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
        createAccountButton = findViewById(R.id.createAccountButton);

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

            // run the database work in the background
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                UserDao userDao = db.userDao();
                User user = userDao.login(username, password);

                // handle user notification and preferences setting on the UI thread
                runOnUiThread(() -> {
                    // if the user isn't found in the DB or the password is incorrect
                    if (user == null) {
                        Toaster.show(this, R.string.invalid_login);

                        // re-enable the Login button
                        loginButton.setEnabled(true);

                        // focus on the password field
                        passwordEditText.requestFocus();
                        passwordEditText.selectAll();
                    }
                    // otherwise log the user in and safe in preferences
                    else {
                        // save username and role in SharedPreferences
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(KEY_USERNAME, user.getUsername());
                        editor.putString(KEY_PHONE, user.getPhone());
                        editor.putString(KEY_ROLE, user.getRole());
                        editor.apply();

                        // cancel any outstanding Toasts
                        Toaster.cancel();

                        // go to the inventory screen
                        startActivity(new Intent(LoginActivity.this, InventoryActivity.class));
                        finish();
                    }
                });
            });
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
        String savedUsername = prefs.getString(KEY_USERNAME, "");
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