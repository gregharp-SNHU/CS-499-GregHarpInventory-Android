package com.mobile2app.gregharpinventory.model;

import androidx.annotation.NonNull;

public class InventoryItem {
    // lower and upper bounds for quantity
    public static final int MIN_QTY = 0;
    public static final int MAX_QTY = 99;

    // default item name when invalid name provided
    public static final String DEFAULT_ITEM_NAME = "No item name";

    // private class variables for database fields
    private String itemId = ""; // firestore document ID (empty means not saved)
    @NonNull
    private String itemName = DEFAULT_ITEM_NAME;
    private int itemQuantity;

    // default constructor for Firestore use
    public InventoryItem() {
        // insert default values
        this.itemName = DEFAULT_ITEM_NAME;
        this.itemQuantity = MIN_QTY;
    }

    // parameterized constructor to create new items for insertion
    public InventoryItem(@NonNull String name, int quantity) {
        // use the setters since they perform validation
        setItemName(name);
        setItemQuantity(quantity);
    }

    // parameterized constructor to with id in addition to name and qty
    public InventoryItem(String id, @NonNull String name, int quantity) {
        // use the setters since they perform validation
        setItemId(id);
        setItemName(name);
        setItemQuantity(quantity);
    }

    // setter for ID
    public void setItemId(String id) {
        // verify id is valid
        if (id != null) {
            this.itemId = id;
        } else {
            this.itemId = "";
        }

    }

    // getter for ID
    public String getItemId() {
        return itemId;
    }

    // setter for item name
    public void setItemName(String name) {
        // check for null
        if (name != null) {
            // trim the string
            String trimmed = name.trim();

            // verify string is non-empty
            if (!trimmed.isEmpty()) {
                // set item name to trimmed string
                this.itemName = trimmed;

                // return to complete process
                return;
            }
        }
        // set a default item name -- strings.xml not available here
        this.itemName = DEFAULT_ITEM_NAME;
    }

    // getter for item name
    @NonNull
    public String getItemName() {
        return itemName;
    }

    // setter for item quantity
    public void setItemQuantity(int quantity) {
        if (quantity < MIN_QTY) {
            this.itemQuantity = MIN_QTY;
        }
        else if (quantity > MAX_QTY) {
            this.itemQuantity = MAX_QTY;
        }
        else {
            this.itemQuantity = quantity;
        }
    }

    // getter for item quantity
    public int getItemQuantity() {
        return itemQuantity;
    }

    // method to increment quantity (limit <= 99)
    public int incrementItemQuantity() {
        if (this.itemQuantity < MAX_QTY) {
            this.itemQuantity++;
        }
        return this.itemQuantity;
    }

    // method to decrement quantity (limit >= 0)
    public int decrementItemQuantity() {
        if (this.itemQuantity > MIN_QTY) {
            this.itemQuantity--;
        }
        return this.itemQuantity;
    }
}
