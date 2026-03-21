package com.mobile2app.gregharpinventory.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.data.AppDatabase;
import com.mobile2app.gregharpinventory.data.ItemDao;
import com.mobile2app.gregharpinventory.data.UserDao;
import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.model.User;
import com.mobile2app.gregharpinventory.util.SMSNotifier;
import com.mobile2app.gregharpinventory.util.Toaster;

import java.util.concurrent.Executors;

public class AccountActivity extends AppCompatActivity {

    // labels for the user account and role for preferences
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_SMS_ON = "sms_enabled";
    private static final String KEY_SMS_REQUEST = "sms_requested";
    private static final String KEY_ROLE = "role";
    private static final String KEY_SAMPLE_DB = "sample_db";

    // pull the SMS switch up to a class variable so it can be used by multiple methods
    private SwitchMaterial smsSwitch;
    private boolean smsSwitchSuppress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up the view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.accountLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // load username and role from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, getString(R.string.unknown_value));
        String phone = prefs.getString(KEY_PHONE, "");
        String role = prefs.getString(KEY_ROLE, getString(R.string.unknown_value));

        // if the phone number is blank, reset the SMS prefs to disabled
        if (phone.isEmpty()) {
            prefs.edit().putBoolean(KEY_SMS_ON, false).apply();
        }

        // bind UI elements
        TextView usernameText = findViewById(R.id.usernameText);
        TextView roleText = findViewById(R.id.roleText);
        EditText phoneText = findViewById(R.id.phoneAccEditText);
        Button savePhoneButton = findViewById(R.id.savePhoneButton);

        // populate the UI
        usernameText.setText(getString(R.string.username_label, username));
        phoneText.setText(phone);
        roleText.setText(getString(R.string.role_label, role));

        // find the SMS switch status in prefs and initialize
        smsSwitch = findViewById(R.id.smsSwitch);
        smsSwitch.setChecked(prefs.getBoolean(KEY_SMS_ON, false));

        // set up SMS switch toggle handler
        smsSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            // if the SMS switch activity is suppressed, ignore this activity
            if (smsSwitchSuppress) {
                return;
            }

            // read the current number from the UI - sanitize it
            String currentDigits = phoneText.getText().toString().replaceAll("\\D+", "");

            // if enabling but no phone number entered, block event and inform user
            if (isChecked && currentDigits.isEmpty()) {
                // revert to unchecked
                button.setChecked(false);

                // Notify user
                Toaster.show(this, R.string.sms_phone_required);

                // return from handler
                return;
            }

            // request SEND_SMS permissions if not yet granted
            if (isChecked
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED)) {
                // ensure UI switch remains off until we get permission
                smsSwitchSuppress = true;
                button.setChecked(false);
                smsSwitchSuppress = false;

                // remember that the user tried to enable SMS
                prefs.edit().putBoolean(KEY_SMS_REQUEST, true).apply();

                // request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMSNotifier.REQ_SEND_SMS
                );

                return;
            }

            // save that the user enabled or disabled SMS in prefs
            prefs.edit().putBoolean(KEY_SMS_ON, isChecked).apply();
        });

        // set up save handler for phone number changes
        savePhoneButton.setOnClickListener(v-> {
            // perform user database update in the background
            Executors.newSingleThreadExecutor().execute(() -> {
                // get the user to update
                AppDatabase db = AppDatabase.getInstance((getApplicationContext()));
                UserDao userDao = db.userDao();
                User user = userDao.findByUsername(username);

                // read raw input and normalize to digits
                String changedDigits = phoneText.getText().toString().replaceAll("\\D+", "");

                // ensure user was found
                if (user != null) {
                    // set the new phone number and update the database
                    user.setPhone(changedDigits);
                    userDao.updateUser(user);
                }

                // run prefs update on UI thread
                runOnUiThread(() -> {
                    // set up prefs editor
                    SharedPreferences.Editor editor = prefs.edit();

                    // safe the digits (even if blank)
                    editor.putString(KEY_PHONE, changedDigits);

                    // if digits are empty (phone number deleted)
                    if (changedDigits.isEmpty()) {
                        // disable SMS messages
                        editor.putBoolean(KEY_SMS_ON, false);
                        smsSwitch.setChecked(false);
                    }

                    // write the changes
                    editor.apply();
                });
            });
        });

        // hook up Log Out button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            // clear username and role from SharedPreferences
            prefs.edit()
                    .remove(KEY_USERNAME)
                    .remove(KEY_ROLE)
                    .apply();

            // go back to LoginActivity
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // hook up the button to seed the sample database
        Button sampleButton = findViewById(R.id.sampleDataButton);

        // if the database is already seeded, disable this button
        if (prefs.getBoolean(KEY_SAMPLE_DB, false)) {
            // disable the button
            sampleButton.setEnabled(false);

            // change the button text to say sample is already loaded
            sampleButton.setText(R.string.sample_db_already_loaded);
        }

        // set onClickListener for sampleButton
        sampleButton.setOnClickListener(v ->{
            // prevent re-load if already done
            if (prefs.getBoolean(KEY_SAMPLE_DB, false)) {
                Toaster.show(this, R.string.sample_db_already_loaded);
                return;
            }

            // disable button to avoid double-click
            sampleButton.setEnabled(false);

            // set prefs to note that database load button has been pressed
            prefs.edit().putBoolean(KEY_SAMPLE_DB, true).apply();

            // run database operations in the background
            Executors.newSingleThreadExecutor().execute(() -> {
                // count items inserted
                int insertedCount = 0;

                try {
                    // connect to the item database Dao
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    ItemDao itemDao = db.itemDao();

                    // sample items list
                    InventoryItem[] samples = new InventoryItem[] {
                            new InventoryItem("AAA 1st Item", 1),
                            new InventoryItem("AAA 2nd Item", 2),
                            new InventoryItem("AAA 3rd Item", 3),
                            new InventoryItem("AAZ Zero Quantity", 0),
                            new InventoryItem("Apples", 12),
                            new InventoryItem("Blueberries", 90),
                            new InventoryItem("Strawberries", 32),
                            new InventoryItem("Oranges", 6),
                            new InventoryItem("Peaches", 15),
                            new InventoryItem("Bottles of Beer", 99),
                            new InventoryItem("Cats", 0),
                            new InventoryItem("Dogs", 5),
                            new InventoryItem("Fish", 21),
                            new InventoryItem("Hamsters", 2),
                            new InventoryItem("Measuring Tapes", 1),
                            new InventoryItem("Nails", 99),
                            new InventoryItem("Hammers", 10),
                            new InventoryItem("Screwdrivers", 4),
                            new InventoryItem("Rolls of Tape", 10),
                            new InventoryItem("Delete Me", 3),
                    };

                    // insert each item
                    for (InventoryItem item: samples) {
                        // insert() returns item ID on success
                        if (itemDao.insert(item) > 0) {
                            // count this item
                            insertedCount++;
                        }
                    }

                    // report successful addition to the DB via UI thread
                    int finalCount = insertedCount;
                    runOnUiThread(() -> {
                        // notify user via a Toast
                        Toaster.show(this, getString(R.string.db_load_done, finalCount));
                    });
                }
                catch (Exception e) {
                    runOnUiThread(() -> {
                        Toaster.show(this, getString(R.string.db_load_failed));
                    });
                }
            });
        });

        // set up the bottom navigation bar
        BottomNavigationView bottomNavView = findViewById(R.id.bottomNavView);
        bottomNavView.setSelectedItemId(R.id.nav_account);
        bottomNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // ignore pressing the account button
            if (id == R.id.nav_account) {
                // already here, do nothing
                return true;
            }
            // if inventory button is pressed, jump to that activity
            else if (id == R.id.nav_inventory) {
                Intent intent = new Intent(AccountActivity.this, InventoryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            // if the reports button is pressed, jump to that activity
            else if (id == R.id.nav_reports) {
                Intent intent = new Intent(AccountActivity.this, ReportsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    // clean up the nav button when returning to this screen
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavView);
        // match the menu item ID
        bottomNav.setSelectedItemId(R.id.nav_account);
        }

    // SMS permissions request handler
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // check if the user has requested permission to send SMS messages
        if (requestCode == SMSNotifier.REQ_SEND_SMS) {
            // connect to the SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            // determine if the user tried to enable SMS
            boolean requestSMS = prefs.getBoolean(KEY_SMS_REQUEST, false);

            // remove the temporary request flag from prefs
            prefs.edit().remove(KEY_SMS_REQUEST).apply();

            if ((requestSMS && grantResults.length > 0)
                    && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // flip the UI switch to on
                smsSwitchSuppress = true;
                smsSwitch.setChecked(true);
                smsSwitchSuppress = false;

                // permission granted - save that in Prefs
                prefs.edit().putBoolean(KEY_SMS_ON, true).apply();
            }
            else {
                // keep the UI switch off
                smsSwitchSuppress = true;
                smsSwitch.setChecked(false);
                smsSwitchSuppress = false;

                // permission denied - save that in prefs
                prefs.edit().putBoolean(KEY_SMS_ON, false).apply();
            }
        }
    }
}
