package com.mobile2app.gregharpinventory.ui;

import android.os.Bundle;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobile2app.gregharpinventory.model.Prefs;
import com.mobile2app.gregharpinventory.util.SMSNotifier;
import com.mobile2app.gregharpinventory.viewmodel.ItemViewModel;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobile2app.gregharpinventory.ui.adapter.InventoryAdapter;
import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.model.Roles;
import com.mobile2app.gregharpinventory.util.InventoryFilter;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    // private variables for the view
    private ActivityResultLauncher<Intent> addItemLauncher;
    private ActivityResultLauncher<Intent> editItemLauncher;

    // viewmodel for DB access
    private ItemViewModel vm;

    // adapter for recycler view
    private InventoryAdapter adapter;

    // unfiltered & unsorted master list of items
    private List<InventoryItem> masterList = new ArrayList<>();

    // filter/sort UI controls
    private EditText searchEditText;
    private Spinner statusSpinner;
    private EditText thresholdEditText;
    private EditText minQtyEditText;
    private EditText maxQtyEditText;
    private TextView noResultsText;
    private RecyclerView recyclerView;

    // current sort selection -- default to name A-Z
    private int currentSortOption = InventoryFilter.SORT_NAME_AZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up the view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inventoryLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // initialize the view model
        vm = new ViewModelProvider(this).get(ItemViewModel.class);

        // initialize the recycler view
        recyclerView = findViewById(R.id.inventoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // connect the adapter to the recycler view
        adapter = new InventoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // feed database when changes are made
        vm.getAll().observe(this, items -> {
            if (items != null) {
                // load the items in the adapter
                masterList = items;
                applyFiltersAndSort();
            }
        });

        // wire up the filter & sort controls
        searchEditText = findViewById(R.id.searchEditText);
        statusSpinner = findViewById(R.id.statusSpinner);
        thresholdEditText = findViewById(R.id.thresholdEditText);
        minQtyEditText = findViewById(R.id.minQtyEditText);
        maxQtyEditText = findViewById(R.id.maxQtyEditText);
        noResultsText = findViewById(R.id.noResultsText);
        ImageButton sortButton = findViewById(R.id.sortButton);

        // set up the status spinner with filter options
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        getString(R.string.filter_all),
                        getString(R.string.filter_in_stock),
                        getString(R.string.filter_low_stock),
                        getString(R.string.filter_out_of_stock)
                });
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(spinnerAdapter);

        // load saved low stock threshold from preferences
        SharedPreferences prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE);
        int savedThreshold = prefs.getInt(Prefs.KEY_LOW_STOCK_THRESHOLD,
                InventoryFilter.DEFAULT_LOW_STOCK_THRESHOLD);
        thresholdEditText.setText(String.valueOf(savedThreshold));

        // re-filter whenever the search text changes
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                applyFiltersAndSort();
            }
        });

        // re-filter when the status spinner selection changes
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // show threshold field only when Low Stock is selected
                if (position == InventoryFilter.STATUS_LOW_STOCK) {
                    thresholdEditText.setVisibility(View.VISIBLE);
                } else {
                    thresholdEditText.setVisibility(View.GONE);
                }
                applyFiltersAndSort();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // re-filter when the low stock threshold changes
        thresholdEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // save to preferences
                try {
                    int val = Integer.parseInt(s.toString());
                    getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
                            .edit()
                            .putInt(Prefs.KEY_LOW_STOCK_THRESHOLD, val)
                            .apply();
                } catch (NumberFormatException ignored) {
                    // empty or invalid -- will use default in filter
                }
                applyFiltersAndSort();
            }
        });

        // re-filter when min quantity changes
        minQtyEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                applyFiltersAndSort();
            }
        });

        // re-filter when max quantity changes
        maxQtyEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                applyFiltersAndSort();
            }
        });

        // sort button opens a popup menu with sort options
        sortButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.sort_name_az) {
                    currentSortOption = InventoryFilter.SORT_NAME_AZ;
                } else if (id == R.id.sort_name_za) {
                    currentSortOption = InventoryFilter.SORT_NAME_ZA;
                } else if (id == R.id.sort_qty_low_high) {
                    currentSortOption = InventoryFilter.SORT_QTY_LOW_HIGH;
                } else if (id == R.id.sort_qty_high_low) {
                    currentSortOption = InventoryFilter.SORT_QTY_HIGH_LOW;
                } else {
                    return false;
                }
                applyFiltersAndSort();
                return true;
            });
            popup.show();
        });

        // register a result handler for AddItemActivity
        addItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // get item name and quantity from the result
                        String name = result.getData().getStringExtra("item_name");
                        int quantity = result.getData().getIntExtra("item_quantity", 0);

                        // ensure item name is not null
                        if (name != null) {
                            // insert new item into DB
                            vm.insert(new InventoryItem(name, quantity));
                        }
                    }
                }
        );

        // find the add item FAB and set and on click listener
        FloatingActionButton addItemFab = findViewById(R.id.addItemFab);
        addItemFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItemActivity.class);
            addItemLauncher.launch(intent);
        });

        // register a result handler for EditItemActivity
        editItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // get item id, name and quantity from result
                        String id = result.getData().getStringExtra("item_id");
                        String updatedName = result.getData().getStringExtra("item_name");
                        int updatedQuantity = result.getData().getIntExtra("item_quantity", 0);

                        // guard against null or empty item id, null name
                        if (id != null && !id.isEmpty() && updatedName != null) {
                            // build an entity with the same firestore ID and new values
                            InventoryItem updatedItem = new InventoryItem(id, updatedName, updatedQuantity);
                            vm.update(updatedItem);

                            if (updatedQuantity == 0) {
                                SMSNotifier.notifyOutOfStock(this, updatedName);
                            }
                        }
                    }
                }
        );

        // connect the long click listener
        adapter.setOnItemEditListener((position, item) -> {
            Intent intent = new Intent(InventoryActivity.this, EditItemActivity.class);
            intent.putExtra("item_id", item.getItemId());
            intent.putExtra("item_name", item.getItemName());
            intent.putExtra("item_quantity", item.getItemQuantity());
            editItemLauncher.launch(intent);
        });

        // connect delete listener
        adapter.setOnItemDeleteListener(item -> vm.delete(item));

        // connect the quantity update listener
        // persist +/- to DB through the ViewModel
        adapter.setOnItemQuantityChangeListener((item, oldQty, newQty) -> {
            // update the item quantity
            vm.updateQuantity(item.getItemId(), newQty);

            // fire SMS on transition from >0 to zero quantity
            if ((oldQty > 0) && (newQty == 0)) {
                SMSNotifier.notifyOutOfStock(this, item.getItemName());
            }
        });


        // set up the bottom navigation bar
        BottomNavigationView bottomNavView = findViewById(R.id.bottomNavView);

        // hide the Reports tab for basic Users -- only Managers and Owners can access
        String role = prefs.getString(Prefs.KEY_ROLE, Roles.USER);
        if (role.equals(Roles.USER)) {
            bottomNavView.getMenu().findItem(R.id.nav_reports).setVisible(false);
        }

        // wire up the listener for the bottom nav bar
        bottomNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // ignore pressing the inventory button
            if (id == R.id.nav_inventory) {
                // already here, do nothing
                return true;
            }
            // if reports button is pressed, jump to that activity
            else if (id == R.id.nav_reports) {
                Intent intent = new Intent(InventoryActivity.this, ReportsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            // if the account button is pressed, jump to that activity
            else if (id == R.id.nav_account) {
                Intent intent = new Intent(InventoryActivity.this, AccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    // applyFiltersAndSort() - read all current filter/sort inputs and
    // apply them to the master item list
    //
    // This is the central method that connects the UI controls to the
    // InventoryFilter utility class. It's called whenever any filter input
    // changes or the database emits a new list.
    private void applyFiltersAndSort() {
        // read current search query
        String query = searchEditText.getText().toString();

        // read status filter from spinner position
        int status = statusSpinner.getSelectedItemPosition();

        // read low stock threshold -- use default if empty or invalid
        int threshold;
        try {
            threshold = Integer.parseInt(thresholdEditText.getText().toString());
        } catch (NumberFormatException e) {
            threshold = InventoryFilter.DEFAULT_LOW_STOCK_THRESHOLD;
        }

        // read min quantity -- -1 disables the lower bound
        int minQty;
        try {
            minQty = Integer.parseInt(minQtyEditText.getText().toString());
        } catch (NumberFormatException e) {
            minQty = -1;
        }

        // read max quantity -- -1 disables the upper bound
        int maxQty;
        try {
            maxQty = Integer.parseInt(maxQtyEditText.getText().toString());
        } catch (NumberFormatException e) {
            maxQty = -1;
        }

        // apply all filters and sort via the utility class
        // this is O(n log n) overall -- see InventoryFilter for details
        List<InventoryItem> filtered = InventoryFilter.processAll(
                masterList, query, status, threshold, minQty, maxQty,
                currentSortOption);

        // update the adapter with the filtered/sorted results
        adapter.setItems(filtered);

        // scroll to top so user sees the beginning of the new result set
        recyclerView.scrollToPosition(0);

        // show "no results" message if filters eliminated everything
        if (filtered.isEmpty() && !masterList.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
        } else {
            noResultsText.setVisibility(View.GONE);
        }
    }

    // clean up the nav button when returning to this screen
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavView);
        bottomNav.setSelectedItemId(R.id.nav_inventory); // match the menu item ID
    }
}