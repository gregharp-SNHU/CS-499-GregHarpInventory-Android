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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.model.DbKeys;
import com.mobile2app.gregharpinventory.model.Prefs;
import com.mobile2app.gregharpinventory.util.Toaster;
import com.mobile2app.gregharpinventory.model.Roles;

public class CreateAccountActivity extends AppCompatActivity {

    // variables attached to UI components
    private EditText newUsernameEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private EditText phoneEditText;
    private Button createAccountButton;

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    // flag to stop double-click of create button from breaking things
    private boolean creatingAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

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

            // get the username, password, and clean phone number from the UI
            String username = newUsernameEditText.getText().toString().trim();
            String password = newPasswordEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim()
                    .replaceAll("\\D+", "");

            // all new users are assigned the role of User
            String role = Roles.USER;

            // create the account in Firebase Authentication
            mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase account creation succeeded -- get the new user
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            // build the user profile document for Firestore
                            Map<String, Object> userProfile = new HashMap<>();
                            userProfile.put(DbKeys.EMAIL, firebaseUser.getEmail());
                            userProfile.put(DbKeys.PHONE, phone);
                            userProfile.put(DbKeys.ROLE, role);

                            // write the user profile to Firestore
                            FirebaseFirestore.getInstance()
                                .collection(DbKeys.USERS_COLL)
                                .document(firebaseUser.getUid())
                                .set(userProfile)
                                .addOnSuccessListener(unused -> {
                                    // save username, phone, and role in SharedPreferences
                                    SharedPreferences prefs = getSharedPreferences(Prefs.NAME,
                                            MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(Prefs.KEY_USERNAME, firebaseUser.getEmail());
                                    editor.putString(Prefs.KEY_PHONE, phone);
                                    editor.putString(Prefs.KEY_ROLE, role);
                                    editor.apply();

                                    // cancel any outstanding Toasts
                                    Toaster.cancel();

                                    // go to the account screen
                                    Intent intent = new Intent(CreateAccountActivity.this,
                                        AccountActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Firestore write failed -- notify the user
                                    Toaster.show(CreateAccountActivity.this,
                                        getString(R.string.account_save_failed));

                                    // clear the flag indicating we're creating an account
                                    creatingAccount = false;

                                    // re-enable the Create Account button
                                    createAccountButton.setEnabled(true);
                                });
                        }
                    } else {
                        // Firebase account creation failed -- check the reason
                        if (task.getException()
                                instanceof FirebaseAuthUserCollisionException) {
                            // email already exists in Firebase
                            Toaster.show(CreateAccountActivity.this,
                                    getString(R.string.user_exists));

                            // return focus to the username field and select all
                            newUsernameEditText.requestFocus();
                            newUsernameEditText.selectAll();
                        } else {
                            // other failure -- show the error message
                            String errorMsg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : getString(R.string.add_user_failed);
                            Toaster.show(CreateAccountActivity.this, errorMsg);
                        }

                        // clear the flag indicating we're creating an account
                        creatingAccount = false;

                        // re-enable the Create Account button
                        createAccountButton.setEnabled(true);
                    }
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
                && password.equals(confirmPassword));
    }
}
