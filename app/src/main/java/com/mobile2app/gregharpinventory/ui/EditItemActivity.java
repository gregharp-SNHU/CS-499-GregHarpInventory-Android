package com.mobile2app.gregharpinventory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.util.Toaster;

public class EditItemActivity extends AppCompatActivity {

    // variables attached to UI components
    private EditText itemNameEditText;
    private EditText itemQuantityEditText;
    private Button saveButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up the view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_item);

        // attach the UI variables to their components on the screen
        itemNameEditText = findViewById(R.id.itemNameInput);
        itemQuantityEditText = findViewById(R.id.itemQuantityInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        // read the incoming data
        Intent intent = getIntent();
        String itemId = intent.getStringExtra("item_id");
        String itemName = intent.getStringExtra("item_name");
        int itemQty = intent.getIntExtra("item_quantity", 0);

        // populate the UI fields
        itemNameEditText.setText(itemName);
        itemQuantityEditText.setText(String.valueOf(itemQty));

        // set up the save button functionality
        saveButton.setOnClickListener(v -> editItem(itemId));

        // if cancel is pressed, close the activity
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // trigger save when user presses Enter on the name field
        itemNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            editItem(itemId);
            return true;
        });

        // trigger save when user presses Enter on the quantity field
        itemQuantityEditText.setOnEditorActionListener((v, actionId, event) -> {
            editItem(itemId);
            return true;
        });
    }

    // clear any outstanding Toasts on pause
    @Override
    protected void onPause() {
        super.onPause();
        Toaster.cancel();
    }

    // function to implement the result of editing an item
    private void editItem(String itemId) {
        if (itemId == null) {
            Toaster.show(this, R.string.invalid_item_id);
            return;
        }

        // fetch the strings
        String updatedName = itemNameEditText.getText().toString().trim();
        String updatedQtyStr = itemQuantityEditText.getText().toString().trim();

        // ensure strings aren't empty
        if (updatedName.isEmpty() || updatedQtyStr.isEmpty()) {
            Toaster.show(this, R.string.name_and_quantity_required);
            return;
        }

        // parse item quantity
        int updatedQty;
        try {
            updatedQty = Integer.parseInt(updatedQtyStr);
        } catch (NumberFormatException e) {
            Toaster.show(this, R.string.quantity_invalid);
            return;
        }

        // ensure min <= qty <= max
        updatedQty = Math.max(InventoryItem.MIN_QTY, Math.min(InventoryItem.MAX_QTY, updatedQty));

        // return edited item data to calling activity
        Intent result = new Intent();
        result.putExtra("item_id", itemId);
        result.putExtra("item_name", updatedName);
        result.putExtra("item_quantity", updatedQty);
        setResult(RESULT_OK, result);
        finish();
    }
}
