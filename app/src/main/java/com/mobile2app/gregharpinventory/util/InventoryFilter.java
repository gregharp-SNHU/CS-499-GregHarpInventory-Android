package com.mobile2app.gregharpinventory.util;

// This utility class handles filtering and sorting inventory items in memory.
// This work is done in memory rather than via database queries as it keeps
// the algorithmic logic separate from the data source. This allows for better
// testing, analysis, and maintenance of the filtering and sorting code. It
// follows my philosophy of separation of concerns. In-memory handling is
// appropriate for the size of datasets used in this application (even if it
// amounts to hundreds or thousands).

// NOTE: All methods here are static and return new lists without modifying
// the input. This is a design decision for code safety and stability.
// The calling method retains a clean reference to the original data and can
// re-filter or re-sort without risk of corruption.

import com.mobile2app.gregharpinventory.model.InventoryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class InventoryFilter {
    // prevent instantiation - This is a utility class
    private InventoryFilter() {}

    // quantity status filter options
    public static final int STATUS_ALL = 0;
    public static final int STATUS_IN_STOCK = 1;
    public static final int STATUS_LOW_STOCK = 2;
    public static final int STATUS_OUT_OF_STOCK = 3;

    // default threshold for low stock
    // items at or below this amount are low stock
    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    // sort options
    public static final int SORT_NAME_AZ      = 0;
    public static final int SORT_NAME_ZA      = 1;
    public static final int SORT_QTY_LOW_HIGH = 2;
    public static final int SORT_QTY_HIGH_LOW = 3;

    // processAll() - apply active filters and sort the result in one call
    //
    // The order of filter application is: 1) name, 2) status, 3) quantity.
    // Sorting is done after filtering.
    //
    // Each filter stage is O(n) and sorting is O(n log n). This means
    // overall complexity is O(n log n).
    //
    // Timsort is used as it's the most efficient choice available.
    public static List<InventoryItem> processAll(List<InventoryItem> items,
                                                 String query,
                                                 int status,
                                                 int threshold,
                                                 int minQty,
                                                 int maxQty,
                                                 int sortOption) {
        // ensure items list is not null/empty
        if (items == null || items.isEmpty()) {
            // return an empty list
            return new ArrayList<>();
        }

        // apply filters sequentially - each pass is O(n)
        List<InventoryItem> result = filterByName(items, query);
        result = filterByStatus(result, status, threshold);
        result = filterByQuantityRange(result, minQty, maxQty);

        // sort the filtered results - this is O(n log n) via Timsort
        result = sort(result, sortOption);

        // return the filtered and sorted result
        return result;
    }

    // filterByName() - filter items by name using case-insensitive substring
    // matching.
    //
    // returns a new filtered list - input is not modified
    //
    // The time complexity of a linear search is O(n * l) where n = item count
    // and l = average item name length. This is effectively O(n) for the
    // length of inventory names used in this app. For that reason, it makes no
    // sense to implement a more complicated lookup using a tree-based approach
    // which would still require n (items) build steps.
    public static List<InventoryItem> filterByName(List<InventoryItem> items,
                                                   String query) {
        // ensure items list is not null/empty
        if (items == null || items.isEmpty()) {
            // return an empty list
            return new ArrayList<>();
        }

        // ensure query is not blank
        if (query == null || query.trim().isEmpty()) {
            // return a copy of the unfiltered items list
            return new ArrayList<>(items);
        }

        // convert the query to lowercase
        String lowerQuery = query.trim().toLowerCase();

        // perform a linear scan checking each item name for a substring match
        List<InventoryItem> filteredItems = new ArrayList<>();
        for (InventoryItem item: items) {
            // if this item contains the query string
            if (item.getItemName().toLowerCase().contains(lowerQuery)) {
                // add this item to the filtered items list
                filteredItems.add(item);
            }
        }

        // return the list of filtered items
        return filteredItems;
    }

    // filterByStatus() - filter items by stock status category.
    //
    // returns a new filtered list - input is not modified
    //
    // The time complexity of a single pass through the list is O(n).
    // Compare time is constant for each item.
    public static List<InventoryItem> filterByStatus(List<InventoryItem> items,
                                                     int statusFilter,
                                                     int lowStockthreshold) {
        // ensure items list is not null/empty
        if (items == null || items.isEmpty()) {
            // return an empty list
            return new ArrayList<>();
        }

        // no filter needed for STATUS_ALL
        if (statusFilter == STATUS_ALL) {
            // return a copy of the unfiltered items list
            return new ArrayList<>(items);
        }

        // perform a linear scan filtering by status (this is O(n))
        List<InventoryItem> filteredItems = new ArrayList<>();
        for (InventoryItem item : items) {
            // get the quantity of this item
            int qty = item.getItemQuantity();

            // filter the result based upon the type of filter selected
            switch (statusFilter) {
                case STATUS_IN_STOCK:
                    // items with quantity greater than zero
                    if (qty > 0)
                        filteredItems.add(item);
                    break;

                case STATUS_LOW_STOCK:
                    // items at or below threshold but not zero
                    if (qty > 0 && qty <= lowStockthreshold)
                        filteredItems.add(item);
                    break;

                case STATUS_OUT_OF_STOCK:
                    // items with zero quantity
                    if (qty == 0)
                        filteredItems.add(item);
                    break;

                default:
                    // undefined filter -- include the item by default
                    filteredItems.add(item);
                    break;
            }
        }

        // return the list of filtered items
        return filteredItems;
    }

    // filterByQuantityRange() - filter items to those within a quantity range.
    //
    // returns a new filtered list - input is not modified
    //
    // The time complexity of a single pass through the list is O(n).
    // Compare time is constant for each item.
    //
    // If minQty or maxQty is -1, this disables filtering for that bound.
    public static List<InventoryItem> filterByQuantityRange(List<InventoryItem> items,
                                                            int minQty,
                                                            int maxQty) {
        // ensure items list is not null/empty
        if (items == null || items.isEmpty()) {
            // return an empty list
            return new ArrayList<>();
        }

        // if both min and max bounds are disabled
        if (minQty < 0 && maxQty < 0) {
            // return a copy of the unfiltered items list
            return new ArrayList<>(items);
        }

        // perform a linear scan filtering by quantity range (this is O(n))
        List<InventoryItem> filteredItems = new ArrayList<>();
        for (InventoryItem item : items) {
            // get the quantity of this item
            int qty = item.getItemQuantity();

            // skip items below the minimum if lower bound is enabled
            if (minQty >= 0 && qty < minQty)
                continue;

            // skip items above the maximum if upper bound is enabled
            if (maxQty >= 0 && qty > maxQty)
                continue;

            // item is within range, so add it to the filtered list
            filteredItems.add(item);
        }

        // return the list of filtered items
        return filteredItems;
    }

    // sort() - sort list of inventory items using specified sort option.
    //
    // returns a new sorted list - input is not modified
    //
    // This sort method uses Collections.sort() which implements Timsort.
    // Timsort is a hybrid merge sort and insertion sort algorithm that is
    // O(n log n) in the average case and worst case. In the best case of
    // a partially sorted list, Timsort approaches O(n).
    //
    // Another aspect of Timsort is that the result is stable. Two items
    // that have equal sort results maintain their order. This is
    // important for sorted lists presented in a user interface because
    // it means items don't arbitrarily change positions upon refresh.
    //
    // I chose to use Timsort rather than write my own sort algorithm
    // because good programmers know when to use the available tools
    // rather than attempt to reinvent them and incur the risk of error
    // or suboptimal peformance.
    public static List<InventoryItem> sort(List<InventoryItem> items,
                                           int sortOption) {
        // ensure items list is not null/empty
        if (items == null || items.isEmpty()) {
            // return an empty list
            return new ArrayList<>();
        }

        // make a copy of the items list to sort
        List<InventoryItem> sortedItems = new ArrayList<>(items);

        // comparator based upon the selected sort option
        Comparator<InventoryItem> comparator;
        switch (sortOption) {
            case SORT_NAME_ZA:
                // case-insensitive alphabetical descending sort
                comparator = (a, b) ->
                        b.getItemName().compareToIgnoreCase(a.getItemName());
                break;

            case SORT_QTY_LOW_HIGH:
                // ascending quantity sort (use item name for tiebreaker)
                comparator = Comparator
                        .comparingInt(InventoryItem::getItemQuantity)
                        .thenComparing(InventoryItem::getItemName,
                                String.CASE_INSENSITIVE_ORDER);
                break;

            case SORT_QTY_HIGH_LOW:
                // descending quantity sort (use item name for tiebreaker)
                comparator = Comparator
                        .comparingInt(InventoryItem::getItemQuantity)
                        .reversed() // this makes it high to low
                        .thenComparing(InventoryItem::getItemName,
                                String.CASE_INSENSITIVE_ORDER);
                break;

            case SORT_NAME_AZ:
            default: // default sort is ascending alphabetical by name
                // case-insensitive alphabetical ascending sort
                comparator = (a, b) ->
                        a.getItemName().compareToIgnoreCase(b.getItemName());
                break;
        }

        // perform the Timsort, which is O(n log n)
        sortedItems.sort(comparator);

        // return the sorted list
        return sortedItems;
    }
}
