package com.mobile2app.gregharpinventory.ui;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.ui.adapter.UserAdapter;
import com.mobile2app.gregharpinventory.util.Toaster;
import com.mobile2app.gregharpinventory.model.Roles;
import com.mobile2app.gregharpinventory.model.Prefs;
import com.mobile2app.gregharpinventory.model.DbKeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManageUsersActivity extends AppCompatActivity {
    // Firestore instance
    private FirebaseFirestore db;

    // adapter for the user list
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // verify the user is an Owner -- redirect if not
        SharedPreferences prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE);
        String role = prefs.getString(Prefs.KEY_ROLE, Roles.USER);
        if (!role.equals(Roles.OWNER)) {
            finish();
            return;
        }

        // set up the view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_users);
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.userManagementLayout), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

        // initialize Firestore
        db = FirebaseFirestore.getInstance();

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.userRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        recyclerView.setAdapter(adapter);

        // handle role changes
        adapter.setOnRoleChangeListener((uid, newRole) -> {
            // build the update map
            Map<String, Object> updates = new HashMap<>();
            updates.put(DbKeys.ROLE, newRole);

            // update the role in Firestore
            db.collection(DbKeys.USERS_COLL).document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toaster.show(this, getString(R.string.role_updated));

                    // reload the list to reflect the change
                    loadUsers();
                })
                .addOnFailureListener(e ->
                        Toaster.show(this, getString(R.string.role_update_failed))
                );
        });

        // handle user deletion with a confirmation dialog
        adapter.setOnDeleteListener((uid, email) -> {
            // show a confirmation dialog before deleting
            new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_user))
                .setMessage(getString(R.string.confirm_delete_user_message, email))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    // delete the user's Firestore document
                    db.collection(DbKeys.USERS_COLL).document(uid)
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toaster.show(this, getString(R.string.user_deleted));

                            // reload the list
                            loadUsers();
                        })
                        .addOnFailureListener(e ->
                            Toaster.show(this, getString(R.string.user_delete_failed))
                        );
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
        });

        // load the user list
        loadUsers();

        // set up the bottom navigation bar
        BottomNavigationView bottomNavView = findViewById(R.id.bottomNavView);
        bottomNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // if account button is pressed, go back to AccountActivity
            if (id == R.id.nav_account) {
                finish();
                return true;
            }
            // if inventory button is pressed, jump to that activity
            else if (id == R.id.nav_inventory) {
                Intent intent = new Intent(ManageUsersActivity.this, InventoryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            // if the reports button is pressed, jump to that activity
            else if (id == R.id.nav_reports) {
                Intent intent = new Intent(ManageUsersActivity.this, ReportsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    // fetch all users - except current - from Firestore and update the adapter
    private void loadUsers() {
        // get the current user's UID to exclude them from the list
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // load list of users
        db.collection(DbKeys.USERS_COLL)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> ids = new ArrayList<>();
                List<Map<String, Object>> data = new ArrayList<>();

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    // if a document matches the UID of the current user
                    if (doc.getId().equals(currentUid)) {
                        // omit the current user - user can't manage their own account
                        continue;
                    }

                    // get the email to use for sort order
                    String email = Objects.requireNonNull(Objects.requireNonNull(doc.getData())
                        .get(DbKeys.EMAIL)).toString();

                    // find the correct position to insert this user in sorted order
                    int pos = 0;
                    while (pos < data.size()) {
                        String existing = Objects.requireNonNull(data.get(pos)
                            .get(DbKeys.EMAIL)).toString();
                        if (email.compareToIgnoreCase(existing) <= 0) {
                            break;
                        }
                        pos++;
                    }

                    // insert each user into the list in sorted order
                    ids.add(pos, doc.getId());
                    data.add(pos, doc.getData());
                }

                // update the adapter with the new data
                adapter.setUsers(ids, data);
            })
            .addOnFailureListener(e ->
                Toaster.show(this, getString(R.string.user_list_failed))
            );
    }
}