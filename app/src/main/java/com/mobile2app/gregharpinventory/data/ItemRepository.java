package com.mobile2app.gregharpinventory.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.model.ReportRow;
import com.mobile2app.gregharpinventory.model.DbKeys;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ItemRepository {
    // Firestore instance for DB operations
    private final FirebaseFirestore db;

    // live list of all items from firestore
    private final MutableLiveData<List<InventoryItem>> itemsLive =
            new MutableLiveData<>(new ArrayList<>());

    // handle for snapshot listener
    private ListenerRegistration listenerReg;

    // constructor for repository -- get the Firestore instance
    public ItemRepository(Context context) {
        // get the Firestore instance
        db = FirebaseFirestore.getInstance();

        // attach the snapshot listener
        attachListener();
    }

    // attach snapshot listener ordered by name to items collection
    private void attachListener() {
        listenerReg = db.collection(DbKeys.ITEMS_COLL)
                .orderBy(DbKeys.ITEM_NAME)
                .addSnapshotListener((snapshots, err) -> {
                    // guard against errors and empty snapshots
                    if (err != null || snapshots == null) {
                        return;
                    }

                    // convert each document to an InventoryItem
                    List<InventoryItem> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String name = doc.getString(DbKeys.ITEM_NAME);
                        Long qty = doc.getLong(DbKeys.ITEM_QTY);
                        int qtyVal = 0; // integer value of qty returned by firestore

                        // guard against null name
                        if (name == null) {
                            name = InventoryItem.DEFAULT_ITEM_NAME;
                        }

                        // guard against null qty - defaults to 0
                        if (qty != null) {
                            qtyVal = qty.intValue();
                        }

                        // generate the inventory item
                        InventoryItem item = new InventoryItem(doc.getId(), name, qtyVal);

                        // add item to the list
                        items.add(item);
                    }

                    // post the updated list of items
                    itemsLive.postValue(items);
                });
    }

    // detach snapshot listener -- called from ItemViewModel.onCleared()
    public void removeListener() {
        if (listenerReg != null) {
            listenerReg.remove();
        }
    }

    // return LiveData for all items
    public LiveData<List<InventoryItem>> getAll() {
        return itemsLive;
    }

    // insert a new item and refresh list
    public void insert(InventoryItem item) {
        // insert the item into Firestore items
        db.collection(DbKeys.ITEMS_COLL).add(buildMap(item));
    }

    // update an entire item object
    public void update(InventoryItem item) {
        // guard against updating an item that's not present in the db
        if (item.getItemId().isEmpty()) {
            return;
        }

        // update the item in Firestore items
        db.collection(DbKeys.ITEMS_COLL)
                .document(item.getItemId())
                .set(buildMap(item));
    }

    // delete an item and refresh list
    public void delete(InventoryItem item) {
        // guard against deleting an item that's not present in the db
        if (item.getItemId().isEmpty()) {
            return;
        }

        // delete the item in Firestore items
        db.collection(DbKeys.ITEMS_COLL)
                .document(item.getItemId())
                .delete();
    }

    // update only item quantity
    public void updateQuantity(String id, int qty) {
        // guard against updating an item that's not present in the db
        if (id == null || id.isEmpty()) {
            return;
        }

        // update the quantity of the item in Firestore items
        db.collection(DbKeys.ITEMS_COLL)
                .document(id)
                .update(DbKeys.ITEM_QTY, qty);
    }

    // return all items mapped to report rows (isLow is always false)
    public LiveData<List<ReportRow>> getAllForReport() {
        return Transformations.map(itemsLive, items -> {
            // build report rows from items db -- all items
            List<ReportRow> rows = new ArrayList<>();
            for (InventoryItem item : items) {
                rows.add(toReportRow(item, false));
            }

            return rows;
        });
    }

    // return items at or below inventory threshold (isLow is always true)
    public LiveData<List<ReportRow>> getLowStockForReport(int threshold) {
        return Transformations.map(itemsLive, items -> {
            // build report rows from items db -- qty <= threshold
            List<ReportRow> rows = new ArrayList<>();
            for (InventoryItem item : items) {
                if (item.getItemQuantity() <= threshold) {
                    rows.add(toReportRow(item, true));
                }
            }

            // sort items to match previous SQL ordering
            rows.sort((a, b) -> {
                int cmp = Integer.compare(a.itemQuantity, b.itemQuantity);
                if (cmp != 0) {
                    return cmp;
                }
                return a.itemName.compareToIgnoreCase(b.itemName);
            });

            return rows;
        });
    }

    // return only out-of-stock items (qty == 0)
    public LiveData<List<ReportRow>> getOutOfStockForReport() {
        return Transformations.map(itemsLive, items -> {
            // build report rows from items db -- qty == 0
            List<ReportRow> rows = new ArrayList<>();
            for (InventoryItem item : items) {
                if (item.getItemQuantity() == 0) {
                    rows.add(toReportRow(item, true));
                }
            }

            return rows;
        });
    }

    // build a firestore field map from an inventory item
    private Map<String, Object> buildMap(InventoryItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put(DbKeys.ITEM_NAME, item.getItemName());
        map.put(DbKeys.ITEM_QTY, item.getItemQuantity());
        return map;
    }

    // build a ReportRow from an InventoryItem
    private ReportRow toReportRow(InventoryItem item, boolean isLow) {
        ReportRow row = new ReportRow();
        row.itemId = item.getItemId();
        row.itemName = item.getItemName();
        row.itemQuantity = item.getItemQuantity();
        row.isLow = isLow;
        return row;
    }
}
