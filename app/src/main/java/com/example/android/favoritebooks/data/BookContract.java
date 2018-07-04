package com.example.android.favoritebooks.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Greta Grigutė on 2018-06-13.
 */
public final class BookContract {
    public static final class BookEntry implements BaseColumns {

        // The Content authority is a name for the entire content provider.
        public static final String CONTENT_AUTHORITY = "com.example.android.favoritebooks";

        // String for adding the table name to content uri.
        public static final String PATH_BOOKS = "books";

        // Create the base of all URI's which apps will use to contact the content provider.
        public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

        // The content URI to access the book data in the provider.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // The MIME type of the {@link #CONTENT_URI} for a list of books.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // The MIME type of the {@link #CONTENT_URI} for a single book.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // Set string for Book table name
        public static final String TABLE_NAME = "books";

        // Set string for Book table columns
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT = "Product";
        public static final String COLUMN_PRODUCT_DESCRIPTION = "Product_description";
        public static final String COLUMN_PRICE = "Price";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_SUPPLIER_NAME = "Supplier";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "Supplier_phone_number";
        public static final String COLUMN_PRODUCT_IMAGE_URI = "Product_image_uri";
    }
}
