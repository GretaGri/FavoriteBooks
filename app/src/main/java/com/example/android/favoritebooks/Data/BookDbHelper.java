package com.example.android.favoritebooks.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.favoritebooks.Data.BookContract.BookEntry;

/**
 * Created by Greta Grigutė on 2018-06-13.
 */
public class BookDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FavoriteBooks.db";
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + BookEntry.TABLE_NAME + " (" +
            BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BookEntry.COLUMN_PRODUCT + " TEXT NOT NULL, " +
            BookEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT DEFAULT 'No description', " +
            BookEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +
            BookEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 1, " +
            BookEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, " +
            BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " LONG" +
            ")";


    //Constructor
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
       // Log to check the SQL create database table string
        Log.d("BookDbHelper", "SQL create string is : " + SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //do nothing for now
    }
}
