package com.mobile2app.gregharpinventory.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.mobile2app.gregharpinventory.data.ItemRepository;
import com.mobile2app.gregharpinventory.model.InventoryItem;
import java.util.List;

public class ItemViewModel extends AndroidViewModel {
    // repository for data access
    private final ItemRepository repo;

    // constructor for item view model
    public ItemViewModel(@NonNull Application app) {
        super(app);

        // create repository instance
        repo = new ItemRepository(app);
    }

    // return LiveData for all items
    public LiveData<List<InventoryItem>> getAll() {
        return repo.getAll();
    }

    // insert a new item
    public void insert(InventoryItem item) {
        repo.insert(item);
    }

    // update an existing item
    public void update(InventoryItem item) {
        repo.update(item);
    }

    // delete an item
    public void delete(InventoryItem item) {
        repo.delete(item);
    }

    // update only item quantity
    public void updateQuantity(String id, int qty) {
        repo.updateQuantity(id, qty);
    }

    // detach the snapshot listener when the ViewModel is destroyed
    @Override
    protected void onCleared() {
        super.onCleared();
        repo.removeListener();
    }
}
