package com.mobile2app.gregharpinventory.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DiffUtil;

import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.model.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    // private list of inventory items
    private List<InventoryItem> itemList;

    // constructor
    public InventoryAdapter(List<InventoryItem> itemList) {
        // protect against null
        if (itemList != null) {
            this.itemList = itemList;
        }
        else {
            this.itemList = new ArrayList<>();
        }

        // enable stable IDs so RecyclerView can keep identity across updates
        setHasStableIds(true);
    }

    // inflate the layout for each inventory item
    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventory_item, parent, false);
        return new InventoryViewHolder(view);
    }

    // bind the specific inventory item to the view holder
    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);
        holder.bind(item);

        // set up increment button functionality
        holder.incrementButton.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION) {
                // get item at refreshed position
                InventoryItem itemTemp = itemList.get(adapterPos);

                // increment the item quantity
                int oldQty = itemTemp.getItemQuantity();
                int newQty = itemTemp.incrementItemQuantity();

                // notify the UI immediately of the change
                notifyItemChanged(adapterPos);

                // update the db so livedata is accurate
                if (qtyListener != null) {
                    qtyListener.onChange(itemTemp, oldQty, newQty);
                }
            }
        });

        // set up decrement button functionality
        holder.decrementButton.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION) {
                // get item at refreshed position
                InventoryItem itemTemp = itemList.get(adapterPos);

                // decrement the item quantity
                int oldQty = itemTemp.getItemQuantity();
                int newQty = itemTemp.decrementItemQuantity();

                // notify the UI immediately of the change
                notifyItemChanged(adapterPos);

                // update the db so livedata is accurate
                if (qtyListener != null) {
                    qtyListener.onChange(itemTemp, oldQty, newQty);
                }
            }
        });

        // set up delete button functionality
        holder.deleteButton.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION && deleteListener != null) {
                deleteListener.onItemDelete(itemList.get(adapterPos));
            }
        });

        // set up the long click trigger to edit the item
        holder.itemName.setOnLongClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && editListener != null) {
                editListener.onItemEdit(adapterPosition, itemList.get(adapterPosition));
                return true;
            }
            return false;
        });
    }

    // return the number of items in the inventory
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // replace items in the adapter and refresh the list
    public void setItems(List<InventoryItem> items) {
        // storage for new and old lists - protect against null
        List<InventoryItem> newList = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
        List<InventoryItem> oldList = this.itemList;

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
                return(oldList.get(oldPos).getItemId() == newList.get(newPos).getItemId());
            }

            // compare the contents of items on the old and new lists
            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                InventoryItem oList = oldList.get(oldPos);
                InventoryItem nList = newList.get(newPos);
                return((oList.getItemQuantity() == nList.getItemQuantity())
                        && (oList.getItemName().equals(nList.getItemName())));
            }
        });

        // load the new list
        this.itemList = newList;

        // only update the new rows
        diff.dispatchUpdatesTo(this);
    }

    @Override
    public long getItemId(int position) {
        // use the Room to maintain position updates
        return itemList.get(position).getItemId();
    }

    // long click listener for to edit an item
    public interface OnItemEditListener {
        void onItemEdit(int position, InventoryItem item);
    }

    // install the long click listener
    private OnItemEditListener editListener;
    public void setOnItemEditListener(OnItemEditListener listener) {
        this.editListener = listener;
    }

    // add quantity-change listener to persist inc/dec via the viewmodel
    public interface OnItemQuantityChangeListener {
        void onChange(InventoryItem item, int oldQty, int newQty);
    }


    // quantity change listener
    private OnItemQuantityChangeListener qtyListener;
    public void setOnItemQuantityChangeListener(OnItemQuantityChangeListener listener) {
        this.qtyListener = listener;
    }


    // delete-click listener to let the Activity persist deletes
    public interface OnItemDeleteListener {
        void onItemDelete(InventoryItem item);
    }

    // delete listener
    private OnItemDeleteListener deleteListener;
    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    // class for the item inventory viewer items
    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemQuantity;
        ImageButton incrementButton;
        ImageButton decrementButton;
        ImageButton deleteButton;

        // bind UI components of the item layout to this view holder
        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            incrementButton = itemView.findViewById(R.id.incrementButton);
            decrementButton = itemView.findViewById(R.id.decrementButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        // populate the view with the item details
        void bind(InventoryItem item) {
            itemName.setText(item.getItemName());
            itemQuantity.setText(String.valueOf(item.getItemQuantity()));

            // set background red if quantity is zero
            int color = ContextCompat.getColor(
                    itemView.getContext(),
                    item.getItemQuantity() == 0
                            ? R.color.inventory_item_zero_qty
                            : R.color.inventory_item_normal
            );
            itemQuantity.setBackgroundColor(color);
        }
    }
}
