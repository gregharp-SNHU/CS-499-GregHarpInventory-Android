package com.mobile2app.gregharpinventory;

import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.util.InventoryFilter;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

// Unit tests for the InventoryFilter utility class.
//
// These tests verify the filtering and sorting algorithms added as
// part of the Algorithms and Data Structures enhancement perform as
// expected.
public class InventoryFilterTest {
    // reusable test data -- rebuilt before each test
    private List<InventoryItem> testItems;

    @Before
    public void setUp() {
        testItems = new ArrayList<>(Arrays.asList(
                new InventoryItem(1, "Apples", 12),
                new InventoryItem(2, "Blueberries", 90),
                new InventoryItem(3, "Bolts", 3),
                new InventoryItem(4, "Cables", 0),
                new InventoryItem(5, "Cats", 0),
                new InventoryItem(6, "Dogs", 5),
                new InventoryItem(7, "Fish", 21),
                new InventoryItem(8, "Hamsters", 2),
                new InventoryItem(9, "Nails", 99),
                new InventoryItem(10, "Hammers", 10)
        ));
    }

    //
    // filterByName tests
    //

    @Test
    public void filterByName_nullList_returnsEmpty() {
        List<InventoryItem> result = InventoryFilter.filterByName(null, "test");
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterByName_emptyList_returnsEmpty() {
        List<InventoryItem> result = InventoryFilter.filterByName(
                new ArrayList<>(), "test");
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterByName_nullQuery_returnsAll() {
        List<InventoryItem> result = InventoryFilter.filterByName(testItems, null);
        assertEquals(testItems.size(), result.size());
    }

    @Test
    public void filterByName_emptyQuery_returnsAll() {
        List<InventoryItem> result = InventoryFilter.filterByName(testItems, "");
        assertEquals(testItems.size(), result.size());
    }

    @Test
    public void filterByName_matchesSubstring() {
        // search for "olt" should return "Bolts"
        List<InventoryItem> result = InventoryFilter.filterByName(testItems, "olt");
        assertEquals(1, result.size());
        assertEquals("Bolts", result.get(0).getItemName());
    }

    @Test
    public void filterByName_caseInsensitive() {
        // search for "APPLE" should return "Apples"
        List<InventoryItem> result = InventoryFilter.filterByName(testItems, "APPLE");
        assertEquals(1, result.size());
        assertEquals("Apples", result.get(0).getItemName());
    }

    @Test
    public void filterByName_noMatch_returnsEmpty() {
        // search for "xyz" should return an empty list
        List<InventoryItem> result = InventoryFilter.filterByName(testItems, "xyz");
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterByName_partialMatch() {
        // "l" appears in Apples, Blueberries, Bolts, Cables, Nails
        List<InventoryItem> result = InventoryFilter.filterByName(testItems, "l");
        assertEquals(5, result.size());
    }

    // --- filterByStatus tests ---

    @Test
    public void filterByStatus_all_returnsAll() {
        List<InventoryItem> result = InventoryFilter.filterByStatus(
                testItems, InventoryFilter.STATUS_ALL, 5);
        assertEquals(testItems.size(), result.size());
    }

    @Test
    public void filterByStatus_nullList_returnsEmpty() {
        List<InventoryItem> result = InventoryFilter.filterByStatus(
                null, InventoryFilter.STATUS_ALL, 5);
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterByStatus_inStock_excludesZeroQty() {
        List<InventoryItem> result = InventoryFilter.filterByStatus(
                testItems, InventoryFilter.STATUS_IN_STOCK, 5);
        assertEquals(8, result.size());
        for (InventoryItem item : result) {
            assertTrue(item.getItemQuantity() > 0);
        }
    }

    @Test
    public void filterByStatus_outOfStock_onlyZeroQty() {
        List<InventoryItem> result = InventoryFilter.filterByStatus(
                testItems, InventoryFilter.STATUS_OUT_OF_STOCK, 5);
        assertEquals(2, result.size());
        for (InventoryItem item : result) {
            assertEquals(0, item.getItemQuantity());
        }
    }

    @Test
    public void filterByStatus_lowStock_withinThreshold5() {
        // threshold 5: Bolts(3), Dogs(5), Hamsters(2)
        List<InventoryItem> result = InventoryFilter.filterByStatus(
                testItems, InventoryFilter.STATUS_LOW_STOCK, 5);
        assertEquals(3, result.size());
        for (InventoryItem item : result) {
            assertTrue(item.getItemQuantity() > 0);
            assertTrue(item.getItemQuantity() <= 5);
        }
    }

    @Test
    public void filterByStatus_lowStock_withinThreshold2() {
        // threshold 2: only Hamsters(2) qualifies
        List<InventoryItem> result = InventoryFilter.filterByStatus(
                testItems, InventoryFilter.STATUS_LOW_STOCK, 2);
        assertEquals(1, result.size());
        assertEquals("Hamsters", result.get(0).getItemName());
    }

    // --- filterByQuantityRange tests ---

    @Test
    public void filterByQuantityRange_nullList_returnsEmpty() {
        List<InventoryItem> result = InventoryFilter.filterByQuantityRange(
                null, 0, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterByQuantityRange_noMatch() {
        // no items with quantity from 30 to 50 should exist
        List<InventoryItem> result = InventoryFilter.filterByQuantityRange(
                testItems, 30, 50);
        assertTrue(result.isEmpty());
    }

    @Test
    public void filterByQuantityRange_bothDisabled_returnsAll() {
        // disabling both minQty and maxQty should return all items
        List<InventoryItem> result = InventoryFilter.filterByQuantityRange(
                testItems, -1, -1);
        assertEquals(testItems.size(), result.size());
    }

    @Test
    public void filterByQuantityRange_minOnly() {
        // min 5: Apples(12), Blueberries(90), Dogs(5),
        //        Fish(21), Nails(99), Hammers(10)
        List<InventoryItem> result = InventoryFilter.filterByQuantityRange(
                testItems, 5, -1);
        assertEquals(6, result.size());
        for (InventoryItem item : result) {
            assertTrue(item.getItemQuantity() >= 5);
        }
    }

    @Test
    public void filterByQuantityRange_bothBounds() {
        // min 1, max 5: Bolts(3), Dogs(5), Hamsters(2)
        List<InventoryItem> result = InventoryFilter.filterByQuantityRange(
                testItems, 1, 5);
        assertEquals(3, result.size());
        for (InventoryItem item : result) {
            assertTrue(item.getItemQuantity() >= 1);
            assertTrue(item.getItemQuantity() <= 5);
        }
    }

    // --- sort tests ---

    @Test
    public void sort_nullList_returnsEmpty() {
        List<InventoryItem> result = InventoryFilter.sort(
                null, InventoryFilter.SORT_NAME_AZ);
        assertTrue(result.isEmpty());
    }

    @Test
    public void sort_emptyList_returnsEmpty() {
        List<InventoryItem> result = InventoryFilter.sort(
                new ArrayList<>(), InventoryFilter.SORT_NAME_AZ);
        assertTrue(result.isEmpty());
    }

    @Test
    public void sort_nameAZ() {
        List<InventoryItem> result = InventoryFilter.sort(
                testItems, InventoryFilter.SORT_NAME_AZ);
        assertEquals("Apples", result.get(0).getItemName());
        assertEquals("Nails", result.get(result.size() - 1).getItemName());
    }

    @Test
    public void sort_nameZA() {
        List<InventoryItem> result = InventoryFilter.sort(
                testItems, InventoryFilter.SORT_NAME_ZA);
        assertEquals("Nails", result.get(0).getItemName());
        assertEquals("Apples", result.get(result.size() - 1).getItemName());
    }

    @Test
    public void sort_qtyLowHigh() {
        List<InventoryItem> result = InventoryFilter.sort(
                testItems, InventoryFilter.SORT_QTY_LOW_HIGH);
        assertEquals(0, result.get(0).getItemQuantity());
        assertEquals(99, result.get(result.size() - 1).getItemQuantity());
    }

    @Test
    public void sort_qtyHighLow() {
        List<InventoryItem> result = InventoryFilter.sort(
                testItems, InventoryFilter.SORT_QTY_HIGH_LOW);
        assertEquals(99, result.get(0).getItemQuantity());
        assertEquals(0, result.get(result.size() - 1).getItemQuantity());
    }

    @Test
    public void sort_qtyTiebreaksByName() {
        // Cables(0) and Cats(0) have same qty -- should sort by name
        List<InventoryItem> result = InventoryFilter.sort(
                testItems, InventoryFilter.SORT_QTY_LOW_HIGH);
        assertEquals("Cables", result.get(0).getItemName());
        assertEquals("Cats", result.get(1).getItemName());
    }

    // --- processAll integration test ---

    @Test
    public void processAll_nullList_returnsEmpty() {
        List<InventoryItem> result = InventoryFilter.processAll(
                null, null, InventoryFilter.STATUS_ALL, 5,
                -1, -1, InventoryFilter.SORT_NAME_AZ);
        assertTrue(result.isEmpty());
    }

    @Test
    public void processAll_allFiltersEliminated_returnsEmpty() {
        List<InventoryItem> result = InventoryFilter.processAll(
                testItems, "xyz", InventoryFilter.STATUS_ALL, 5,
                -1, -1, InventoryFilter.SORT_NAME_AZ);
        assertTrue(result.isEmpty());
    }

    @Test
    public void processAll_combinedFilters() {
        // search "a", in stock, qty 1-15, sort qty high to low
        // "a" matches: Apples(12), Cables(0), Cats(0),
        //              Hamsters(2), Nails(99), Hammers(10)
        // in stock removes: Cables(0), Cats(0)
        // qty 1-15 removes: Blueberries(90), Nails(99)
        // remaining: Apples(12), Hammers(10), Hamsters(2) high to low
        List<InventoryItem> result = InventoryFilter.processAll(
                testItems, "a", InventoryFilter.STATUS_IN_STOCK, 5,
                1, 15, InventoryFilter.SORT_QTY_HIGH_LOW);
        assertEquals(3, result.size());
        assertEquals("Apples", result.get(0).getItemName());
        assertEquals("Hammers", result.get(1).getItemName());
        assertEquals("Hamsters", result.get(2).getItemName());
    }
}