package com.mobile2app.gregharpinventory.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.model.ReportRow;

import java.util.List;
import java.util.ArrayList;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    // local copy of the list items
    private final List<ReportRow> rows = new ArrayList<>();

    // constructor
    public ReportAdapter(List<ReportRow> initial) {
        if (initial != null) {
            this.rows.addAll(initial);
        }
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportRow row = rows.get(position);
        holder.nameText.setText(row.itemName);
        holder.quantityText.setText(String.valueOf(row.itemQuantity));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @Override
    public long getItemId(int position) {
        return rows.get(position).itemId;
    }

    // submit items for the report
    public void submitItems(List<ReportRow> newItems) {
        List<ReportRow> oldList = new ArrayList<>(rows);
        List<ReportRow> newList;
        if (newItems == null) {
            newList = new ArrayList<>();
        }
        else {
            newList = new ArrayList<>(newItems);
        }

        // generate an efficient update using DiffUtil
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            // return the size of the old list
            @Override public int getOldListSize() {
                return(oldList.size());
            }

            // return the size of the new list
            @Override public int getNewListSize() {
                return(newList.size());
            }

            // compare items on the old and new lists
            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return(oldList.get(oldPos).itemId == newList.get(newPos).itemId);
            }

            // compare the contents of items on the old and new lists
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                ReportRow oList = oldList.get(oldPos);
                ReportRow nList = newList.get(newPos);

                // compare fields that affect the row UI
                return((oList.itemQuantity == nList.itemQuantity)
                        && (oList.itemName.equals(nList.itemName)));
            }
        });

        // load the new list
        rows.clear();
        rows.addAll(newList);

        // only update the new rows
        diff.dispatchUpdatesTo(this);
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        // UI variables
        TextView nameText;
        TextView quantityText;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);

            // get name and quantity for view
            nameText = itemView.findViewById(R.id.reportItemName);
            quantityText = itemView.findViewById(R.id.reportItemQuantity);
        }
    }
}
