package com.example.android.favoritebooks.Data;

import android.provider.BaseColumns;

/**
 * Created by Greta Grigutė on 2018-06-13.
 */
public class BookContract {
    public static final class BookEntry implements BaseColumns {
        //Set string for Book table name
        public static final String TABLE_NAME = "Books";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT = "Product";
        public static final String COLUMN_PRICE = "Price";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_SUPPLIER_NAME = "Supplier";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "Supplier_phone_number";
        public static final String COLUMN_PRODUCT_DESCRIPTION = "Product_description";
    }
}
