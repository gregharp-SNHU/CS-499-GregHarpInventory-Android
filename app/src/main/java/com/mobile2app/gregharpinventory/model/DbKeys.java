package com.mobile2app.gregharpinventory.model;

public class DbKeys {
    // keys for users database
    public static final String USERS_COLL = "users";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String ROLE = "role";

    // keys for items database
    public static final String ITEMS_COLL = "items";
    public static final String ITEM_NAME = "itemName";
    public static final String ITEM_QTY = "itemQuantity";

    // prevent instantiation
    private DbKeys() {}
}
