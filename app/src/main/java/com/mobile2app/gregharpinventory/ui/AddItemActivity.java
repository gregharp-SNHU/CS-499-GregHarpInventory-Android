package com.mobile2app.gregharpinventory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.mobile2app.gregharpinventory.R;

public class AddItemActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_add_item);

        // attach the UI variables to their components on the screen
        itemNameEditText = findViewById(R.id.itemNameInput);
        itemQuantityEditText = findViewById(R.id.itemQuantityInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        // set up the save button functionality
        saveButton.setOnClickListener(v -> addItem());

        // if cancel is pressed, close the activity
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // trigger save when user presses Enter on the name field
        itemNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            addItem();
            return true;
        });

        // trigger save when user presses Enter on the quantity field
        itemQuantityEditText.setOnEditorActionListener((v, actionId, event) -> {
            addItem();
            return true;
        });
    }

    // function to add an item to the list
    private void addItem() {
        // get the item name and quantity from the UI and trim it
        String name = itemNameEditText.getText().toString().trim();
        String quantityStr = itemQuantityEditText.getText().toString().trim();

        // validate name isn't empty
        if (TextUtils.isEmpty(name)) {
            itemNameEditText.setError("Item name is required");
            return;
        }

        // validate quantity isn't empty
        if (TextUtils.isEmpty(quantityStr)) {
            itemQuantityEditText.setError("Quantity is required");
            return;
        }

        // parse the quantity value
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                itemQuantityEditText.setError("Quantity must be non-negative");
                return;
            }
        } catch (NumberFormatException e) {
            itemQuantityEditText.setError("Quantity must be a number");
            return;
        }

        // return new item data to calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("item_name", name);
        resultIntent.putExtra("item_quantity", quantity);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
