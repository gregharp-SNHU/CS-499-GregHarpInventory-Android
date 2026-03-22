package com.mobile2app.gregharpinventory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.model.Prefs;
import com.mobile2app.gregharpinventory.ui.adapter.ReportAdapter;
import com.mobile2app.gregharpinventory.viewmodel.ReportsViewModel;
import com.mobile2app.gregharpinventory.model.Roles;

import java.util.ArrayList;

public class ReportsActivity extends AppCompatActivity {

    // variables attached to UI
    private RadioGroup reportTypeGroup;
    private boolean updatingFromVm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // verify the user has permission to view reports -- redirect basic Users
        SharedPreferences prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE);
        String role = prefs.getString(Prefs.KEY_ROLE, Roles.USER);
        if (role.equals(Roles.USER)) {
            // basic Users cannot access reports -- redirect to Inventory
            startActivity(new Intent(ReportsActivity.this, InventoryActivity.class));
            finish();
            return;
        }

        // set up the view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reportsLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // set up the recycler view
        RecyclerView recyclerView = findViewById(R.id.reportsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ReportAdapter adapter = new ReportAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // set up the view models
        ReportsViewModel reportsViewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        // set up radio group for report type
        reportTypeGroup = findViewById(R.id.reportTypeGroup);

        // set the view model to match the UI
        reportsViewModel.getReportType().observe(this, type -> {
            // enable updating from view model
            updatingFromVm = true;

            if (type == ReportsViewModel.ReportType.ALL) {
                reportTypeGroup.check(R.id.rb_all);
            }
            else if (type == ReportsViewModel.ReportType.LOW_STOCK) {
                reportTypeGroup.check(R.id.rb_low);
            }
            else if (type == ReportsViewModel.ReportType.OUT_OF_STOCK) {
                reportTypeGroup.check(R.id.rb_out);
            }

            // disable updating from view model
            updatingFromVm = false;
        });

        // set the UI to match the view model
        reportTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // if we're updating from the view model, return early
            if (updatingFromVm) {
                return;
            }

            if (checkedId == R.id.rb_all) {
                reportsViewModel.setReportType(ReportsViewModel.ReportType.ALL);
            }
            else if (checkedId == R.id.rb_low) {
                reportsViewModel.setReportType(ReportsViewModel.ReportType.LOW_STOCK);
            }
            else if (checkedId == R.id.rb_out) {
                reportsViewModel.setReportType(ReportsViewModel.ReportType.OUT_OF_STOCK);
            }
        });

        // observe back end db connection
        reportsViewModel.rows.observe(this, adapter::submitItems);

        // set up the bottom navigation bar
        BottomNavigationView bottomNavView = findViewById(R.id.bottomNavView);
        bottomNavView.setSelectedItemId(R.id.nav_reports);
        bottomNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // ignore pressing the reports button
            if (id == R.id.nav_reports) {
                // already here, do nothing
                return true;
            }
            // if inventory button is pressed, jump to that activity
            else if (id == R.id.nav_inventory) {
                Intent intent = new Intent(ReportsActivity.this, InventoryActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            // if the account button is pressed, jump to that activity
            else if (id == R.id.nav_account) {
                Intent intent = new Intent(ReportsActivity.this, AccountActivity.class);
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
        bottomNav.setSelectedItemId(R.id.nav_reports); // match the menu item ID
    }
}
