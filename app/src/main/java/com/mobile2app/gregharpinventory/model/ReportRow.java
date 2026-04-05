// ReportRow.java
package com.mobile2app.gregharpinventory.model;

import androidx.annotation.NonNull;

public class ReportRow {
    public String itemId; // updated to string with migration to Firestore
    @NonNull public String itemName = InventoryItem.DEFAULT_ITEM_NAME;
    public int itemQuantity;
    public boolean isLow;
}
