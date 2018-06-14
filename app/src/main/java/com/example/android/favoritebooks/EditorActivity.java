package com.example.android.favoritebooks;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.favoritebooks.Data.BookContract.BookEntry;
import com.example.android.favoritebooks.Data.BookDbHelper;

public class EditorActivity extends AppCompatActivity {

    private EditText productNameEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supNameEditText;
    private EditText phoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        productNameEditText = findViewById(R.id.edit_name);
        descriptionEditText = findViewById(R.id.edit_description);
        priceEditText = findViewById(R.id.edit_price);
        quantityEditText = findViewById(R.id.edit_quantity);
        supNameEditText = findViewById(R.id.edit_supplier);
        phoneEditText = findViewById(R.id.edit_phone);

    }

    private void insertBook() {
        String productName = productNameEditText.getText().toString().trim();
        String productDescription = descriptionEditText.getText().toString().trim();
        Integer productPrice = Integer.parseInt(priceEditText.getText().toString());
        Integer productQuantity = Integer.parseInt(quantityEditText.getText().toString().trim());
        String supplierName = supNameEditText.getText().toString().trim();
        Long supplierPhoneNumber = Long.parseLong(phoneEditText.getText().toString().trim());

        BookDbHelper DbHelper = new BookDbHelper(this);

        SQLiteDatabase db = DbHelper.getWritableDatabase();


        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT, productName);
        values.put(BookEntry.COLUMN_PRODUCT_DESCRIPTION, productDescription);
        values.put(BookEntry.COLUMN_PRICE, productPrice);
        values.put(BookEntry.COLUMN_QUANTITY, productQuantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);


        //Insert a new row, returning the primary key value of the new row.
        long newRowId = db.insert(BookEntry.TABLE_NAME, null, values);

        if (newRowId == -1)
            Toast.makeText(this, R.string.error_saving_data, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.product_saved, newRowId), Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                insertBook();
                //Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

