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
import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.util.SMSNotifier;
import com.mobile2app.gregharpinventory.util.Toaster;
import com.mobile2app.gregharpinventory.model.Roles;
import com.mobile2app.gregharpinventory.model.Prefs;
import com.mobile2app.gregharpinventory.model.DbKeys;
import com.mobile2app.gregharpinventory.data.ItemRepository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

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
        SharedPreferences prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE);
        String username = prefs.getString(Prefs.KEY_USERNAME, getString(R.string.unknown_value));
        String phone = prefs.getString(Prefs.KEY_PHONE, "");
        String role = prefs.getString(Prefs.KEY_ROLE, getString(R.string.unknown_value));

        // if the phone number is blank, reset the SMS prefs to disabled
        if (phone.isEmpty()) {
            prefs.edit().putBoolean(Prefs.KEY_SMS_ON, false).apply();
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
        smsSwitch.setChecked(prefs.getBoolean(Prefs.KEY_SMS_ON, false));

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
                prefs.edit().putBoolean(Prefs.KEY_SMS_REQUEST, true).apply();

                // request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMSNotifier.REQ_SEND_SMS
                );

                return;
            }

            // save that the user enabled or disabled SMS in prefs
            prefs.edit().putBoolean(Prefs.KEY_SMS_ON, isChecked).apply();
        });

        // set up save handler for phone number changes
        savePhoneButton.setOnClickListener(v-> {
            // read raw input and normalize to digits
            String changedDigits = phoneText.getText().toString().replaceAll("\\D+", "");

            // get the current Firebase user
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null) {
                // build the update map for Firestore
                Map<String, Object> updates = new HashMap<>();
                updates.put(DbKeys.PHONE, changedDigits);

                // update the phone number in Firestore
                FirebaseFirestore.getInstance()
                    .collection(DbKeys.USERS_COLL)
                    .document(firebaseUser.getUid())
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                        // set up prefs editor
                        SharedPreferences.Editor editor = prefs.edit();

                        // save the digits (even if blank)
                        editor.putString(Prefs.KEY_PHONE, changedDigits);

                        // if digits are empty (phone number deleted)
                        if (changedDigits.isEmpty()) {
                            // disable SMS messages
                            editor.putBoolean(Prefs.KEY_SMS_ON, false);
                            smsSwitch.setChecked(false);
                        }

                        // write the changes
                        editor.apply();
                    })
                    .addOnFailureListener(e -> {
                        // Firestore update failed -- notify the user
                        Toaster.show(AccountActivity.this,
                                getString(R.string.phone_save_failed));
                    });
            }
        });

        // hook up Log Out button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            // sign out of Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // clear username and role from SharedPreferences
            prefs.edit()
                    .remove(Prefs.KEY_USERNAME)
                    .remove(Prefs.KEY_ROLE)
                    .apply();

            // go back to LoginActivity
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // hook up the Manage Users button -- only visible to Owners
        Button manageUsersButton = findViewById(R.id.manageUsersButton);
        if (!role.equals(Roles.OWNER)) {
            manageUsersButton.setVisibility(android.view.View.GONE);
        } else {
            manageUsersButton.setOnClickListener(v -> {
                startActivity(new Intent(AccountActivity.this,
                    ManageUsersActivity.class));
            });
        }

        // hook up the button to seed the sample database
        Button sampleButton = findViewById(R.id.sampleDataButton);
        // only show the sample data button for the Owner role
        if (!role.equals(Roles.OWNER)) {
            sampleButton.setVisibility(android.view.View.GONE);
        } else { // determine whether to enable the sample load button
            // start with button disabled - enable it based upon items database contents
            sampleButton.setEnabled(false);

            // check if the items collection in Firebase has any data
            FirebaseFirestore.getInstance()
                    .collection(DbKeys.ITEMS_COLL)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.isEmpty()) {
                            // no items in database - enable the button
                            sampleButton.setEnabled(true);
                        } else {
                            // items exist in database -- leave disabled and set text
                            sampleButton.setText(R.string.sample_db_already_loaded);
                        }
                    })
                    .addOnFailureListener(err -> {
                       // db check failed -- leave button disabled to be safe
                       Toaster.show(this, getString(R.string.db_load_failed));
                    });
        }

        // set onClickListener for sampleButton
        sampleButton.setOnClickListener(v ->{
            // disable button to avoid double-click
            sampleButton.setEnabled(false);

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

            // insert each sample item into Firestore items collection
            ItemRepository repo = new ItemRepository(getApplicationContext());
            for (InventoryItem item: samples) {
                // insert each sample item into the items collection
                repo.insert(item);
            }

            // notify user the sample database is loaded
            Toaster.show(this, getString(R.string.db_load_done, samples.length));
        });

        // set up the bottom navigation bar
        BottomNavigationView bottomNavView = findViewById(R.id.bottomNavView);

        // hide the Reports tab for basic Users -- only Managers and Owners can access
        if (role.equals(Roles.USER)) {
            bottomNavView.getMenu().findItem(R.id.nav_reports).setVisible(false);
        }

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
            SharedPreferences prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE);

            // determine if the user tried to enable SMS
            boolean requestSMS = prefs.getBoolean(Prefs.KEY_SMS_REQUEST, false);

            // remove the temporary request flag from prefs
            prefs.edit().remove(Prefs.KEY_SMS_REQUEST).apply();

            if ((requestSMS && grantResults.length > 0)
                    && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // flip the UI switch to on
                smsSwitchSuppress = true;
                smsSwitch.setChecked(true);
                smsSwitchSuppress = false;

                // permission granted - save that in Prefs
                prefs.edit().putBoolean(Prefs.KEY_SMS_ON, true).apply();
            }
            else {
                // keep the UI switch off
                smsSwitchSuppress = true;
                smsSwitch.setChecked(false);
                smsSwitchSuppress = false;

                // permission denied - save that in prefs
                prefs.edit().putBoolean(Prefs.KEY_SMS_ON, false).apply();
            }
        }
    }
}
