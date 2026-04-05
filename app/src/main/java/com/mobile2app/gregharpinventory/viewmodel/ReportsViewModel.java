// ReportsViewModel.java
package com.mobile2app.gregharpinventory.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.util.List;

import com.mobile2app.gregharpinventory.data.ItemRepository;
import com.mobile2app.gregharpinventory.model.ReportRow;

public class ReportsViewModel extends AndroidViewModel {
    public enum ReportType {ALL, LOW_STOCK, OUT_OF_STOCK }

    private final ItemRepository repo;
    private final MutableLiveData<ReportType> selectedType = new MutableLiveData<>(ReportType.ALL);
    private final MutableLiveData<Integer> threshold = new MutableLiveData<>(4);
    public final LiveData<List<ReportRow>> rows;

    // constructor -- create the repository and wire up report type switching
    public ReportsViewModel(@NonNull Application app) {
        super(app);
        repo = new ItemRepository(app);

        rows = Transformations.switchMap(selectedType, type -> {
            switch (type) {
                case LOW_STOCK:
                    return Transformations.switchMap(threshold, repo::getLowStockForReport);
                case OUT_OF_STOCK:
                    return repo.getOutOfStockForReport();
                case ALL:
                default:
                    return repo.getAllForReport();
            }
        });
    }

    // set report type
    public void setReportType(ReportType type) {
        selectedType.setValue(type);
    }

    // set low inventory threshold
    public void setLowThreshold(int t) {
        threshold.setValue(t);
    }

    // return current report type
    public LiveData<ReportType> getReportType() {
        return selectedType;
    }

    // detach the snapshot listener when the ViewModel is destroyed
    @Override
    protected void onCleared() {
        super.onCleared();
        repo.removeListener();
    }
}
