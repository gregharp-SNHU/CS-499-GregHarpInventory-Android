package com.mobile2app.gregharpinventory.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import com.mobile2app.gregharpinventory.util.SMSNotifier;
import com.mobile2app.gregharpinventory.viewmodel.ItemViewModel;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mobile2app.gregharpinventory.ui.adapter.InventoryAdapter;
import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.model.InventoryItem;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    // private variables for the view
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private ActivityResultLauncher<Intent> addItemLauncher;
    private ActivityResultLauncher<Intent> editItemLauncher;

    // viewmodel for DB access
    private ItemViewModel vm;

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
                adapter.setItems(items);
            }
        });

        // register a result handler for AddItemActivity
        addItemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // get item name and quantity from the result
                        String name = result.getData().getStringExtra("item_name");
                        int quantity = result.getData().getIntExtra("item_quantity", 0);

                        // insert new item into DB
                        vm.insert(new InventoryItem(name, quantity));
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
                        long id = result.getData().getLongExtra("item_id", -1L);
                        String updatedName = result.getData().getStringExtra("item_name");
                        int updatedQuantity = result.getData().getIntExtra("item_quantity", 0);

                        if (id > 0) {
                            // build an entity with the same PK and new values
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

    // clean up the nav button when returning to this screen
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavView);
        bottomNav.setSelectedItemId(R.id.nav_inventory); // match the menu item ID
    }
}