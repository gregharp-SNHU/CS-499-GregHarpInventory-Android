// ReportRow.java
package com.mobile2app.gregharpinventory.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

public class ReportRow {
    @ColumnInfo(name = "itemId") public long itemId;
    @ColumnInfo(name = "itemName") @NonNull public String itemName = InventoryItem.DEFAULT_ITEM_NAME;
    @ColumnInfo(name = "itemQuantity") public int itemQuantity;
    @ColumnInfo(name = "isLow") public boolean isLow;
}
