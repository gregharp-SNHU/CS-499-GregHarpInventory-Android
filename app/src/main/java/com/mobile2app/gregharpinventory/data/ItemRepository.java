package com.mobile2app.gregharpinventory.data;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.model.ReportRow;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemRepository {
    // DAO reference for DB operations
    private final ItemDao dao;

    // background thread for queries
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    // LiveData streamed directly from Room
    private final LiveData<List<InventoryItem>> itemsLive;

    // constructor for repository
    public ItemRepository(Context context) {
        // get DAO from singleton DB
        dao = AppDatabase.getInstance(context).itemDao();

        // subscribe to Room's LiveData
        itemsLive = dao.getAllLive();
    }

    // return LiveData for all items
    public LiveData<List<InventoryItem>> getAll() {
        return itemsLive;
    }

    // insert a new item and refresh list
    public void insert(InventoryItem item) {
        io.execute(() -> dao.insert(item));
    }

    // update an entire item object
    public void update(InventoryItem item) {
        io.execute(() -> dao.update(item));
    }

    // delete an item and refresh list
    public void delete(InventoryItem item) {
        io.execute(() -> dao.delete(item));
    }

    // update only item quantity
    public void updateQuantity(long id, int qty) {
        io.execute(() -> dao.updateQuantity(id, qty));
    }

    // ItemRepository.java (additions)
    public LiveData<List<ReportRow>> getAllForReport() {
        return dao.getAllForReport();
    }

    public LiveData<List<ReportRow>> getLowStockForReport(int threshold) {
        return dao.getLowStockForReport(threshold);
    }

    public LiveData<List<ReportRow>> getOutOfStockForReport() {
        return dao.getOutOfStockForReport();
    }

}
