package com.mobile2app.gregharpinventory.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
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
import android.widget.RadioGroup;

import java.util.concurrent.Executors;

import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.data.AppDatabase;
import com.mobile2app.gregharpinventory.data.UserDao;
import com.mobile2app.gregharpinventory.model.User;
import com.mobile2app.gregharpinventory.util.Toaster;

public class CreateAccountActivity extends AppCompatActivity {

    // variables attached to UI components
    private EditText newUsernameEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private EditText phoneEditText;
    private RadioGroup roleRadioGroup;
    private Button createAccountButton;

    // flag to stop double-click of create button from breaking things
    private boolean creatingAccount = false;

    // labels for the user account and role for preferences
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up the view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createAccountLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // attach the UI variables to their components on the screen
        newUsernameEditText = findViewById(R.id.newUsernameEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        createAccountButton = findViewById(R.id.createAccountButton);

        // ensure the Create Account button is disabled by default
        createAccountButton.setEnabled(false);

        // add test changed listener to enable the Create Account button
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // enable the Create Account button if conditions are met
                validateUsernameAndPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nothing to do here
            }
        };

        // Add the TextWatcher to all three text fields
        newUsernameEditText.addTextChangedListener(watcher);
        newPasswordEditText.addTextChangedListener(watcher);
        confirmPasswordEditText.addTextChangedListener(watcher);

        // attach the validation function to the radio group
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId)
                -> validateUsernameAndPassword());

        // let the Enter key trigger the Create Account button
        confirmPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (createAccountButton.isEnabled()) {
                createAccountButton.performClick();
            }

            // always consume Enter
            return true;
        });

        // create the connection from the Create Account button to take the user to the Inventory screen
        createAccountButton.setOnClickListener(v -> {
            // check if we're already creating an account -- button was double-clicked
            if (creatingAccount)
                // if so, ignore this click
                return;

            // set the flag to true -- we are now creating an account
            creatingAccount = true;

            // disable the Create Account button to prevent double-click
            createAccountButton.setEnabled(false);

            // get the username and password from the UI
            String username = newUsernameEditText.getText().toString().trim();
            String password = newPasswordEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            // get the role from the radio buttons
            String role;
            int checkedRole = roleRadioGroup.getCheckedRadioButtonId();
            // can't use switch() because R.id.x aren't compile-time constants
            if (checkedRole == R.id.roleOwner) {
                role = User.ROLE_OWNER;
            }
            else if (checkedRole == R.id.roleManager) {
                role = User.ROLE_MANAGER;
            }
            else { //default: (checkedRole == R.id.roleUser)
                role = User.ROLE_USER;
            }

            // build the user entry
            final User user = new User();
            try {
                user.setUsername(username);
                user.setPassword(password);
                user.setPhone(phone);
                user.setRole(role);
            }
            catch (IllegalArgumentException e) {
                // notify user of invalid credentials
                Toaster.show(this, e.getMessage());

                // clear the flag indicating we're creating an account
                creatingAccount = false;

                // re-enable the Create Account button
                createAccountButton.setEnabled(true);

                return;
            }

            // do the DB work in the background
            Executors.newSingleThreadExecutor().execute(() -> {
                // get a reference to the user DB
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                UserDao userDao = db.userDao();

                // attempt to insert user into the database
                try {
                    // insert user -- ID is generated by Room
                    if(userDao.insertUser(user) <= 0) {
                        // notify user if database creation failed
                        runOnUiThread(() -> {
                            Toaster.show(this, getString(R.string.add_user_failed));

                            // clear the flag indicating we're creating an account
                            creatingAccount = false;

                            // re-enable the Create Account button
                            createAccountButton.setEnabled(true);
                        });

                        return;
                    }
                }
                catch (SQLiteConstraintException e) {
                    // notify user that name is not unique
                    runOnUiThread(() -> {
                            Toaster.show(this, getString(R.string.user_exists));

                    // clear the flag indicating we're creating an account
                    creatingAccount = false;

                    // re-enable the Create Account button
                    createAccountButton.setEnabled(true);

                    // return focus to the username field and select all
                    newUsernameEditText.requestFocus();
                    newUsernameEditText.selectAll();
                    });

                    return;
                }

                // in the main UI thread, save preferences and navigate to Inventory
                runOnUiThread(() -> {
                    // save username, phone, and role in SharedPreferences
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_USERNAME, user.getUsername());
                    editor.putString(KEY_PHONE, user.getPhone());
                    editor.putString(KEY_ROLE, user.getRole());
                    editor.apply();

                    // go to the account screen
                    Intent intent = new Intent(CreateAccountActivity.this, AccountActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // cancel any outstanding Toasts
                    Toaster.cancel();

                    finish();
                });
            });
        });
    }

    // clear any outstanding Toasts on pause
    @Override
    protected void onPause() {
        super.onPause();
        Toaster.cancel();
    }

    // function to determine if username is valid and passwords match
    private void validateUsernameAndPassword () {
        String username = newUsernameEditText.getText().toString().trim();
        String password = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        boolean roleSelected = roleRadioGroup.getCheckedRadioButtonId() != -1;

        // display a message that password don't match
        if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.passwords_must_match));
        }
        else {
            // clear error message when password are non-blank and match
            confirmPasswordEditText.setError(null);
        }

        // enable the button if the username is non-blank and non-blank passwords match
        createAccountButton.setEnabled(!username.isEmpty()
                && !password.isEmpty()
                && password.equals(confirmPassword)
                && roleSelected);
    }
}
