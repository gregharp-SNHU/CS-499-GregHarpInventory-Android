package com.mobile2app.gregharpinventory.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.model.ReportRow;

import java.util.List;

@Dao
public interface ItemDao {
    // fetch all items as a livedata list ordered by name (case insensitive)
    @Query("SELECT * FROM items ORDER BY itemName COLLATE NOCASE")
    LiveData<List<InventoryItem>> getAllLive();

    // insert a new item, returns new row id
    @Insert
    long insert(InventoryItem item);

    // delete a specific item
    @Delete
    int delete(InventoryItem item);

    // delete item by id
    @Query("DELETE FROM items WHERE itemId = :id")
    int deleteById(long id);

    // update an entire item (name and quantity)
    @Update
    int update(InventoryItem item);

    // update item name by id
    @Query("UPDATE items SET itemName = :name WHERE itemId = :id")
    int updateName(long id, String name);

    // update item quantity by id
    @Query("UPDATE items SET itemQuantity = :qty WHERE itemId = :id")
    int updateQuantity(long id, int qty);

    // return live ReportRow list of items
    @Query("SELECT itemId, itemName, itemQuantity, 0 AS isLow FROM items ORDER BY itemName COLLATE NOCASE")
    LiveData<List<ReportRow>> getAllForReport();

    @Query("SELECT itemId, itemName, itemQuantity, 1 AS isLow FROM items WHERE itemQuantity <= :threshold ORDER BY itemQuantity ASC, itemName COLLATE NOCASE")
    LiveData<List<ReportRow>> getLowStockForReport(int threshold);

    @Query("SELECT itemId, itemName, itemQuantity, (itemQuantity = 0) AS isLow FROM items WHERE itemQuantity = 0 ORDER BY itemName COLLATE NOCASE")
    LiveData<List<ReportRow>> getOutOfStockForReport();
}