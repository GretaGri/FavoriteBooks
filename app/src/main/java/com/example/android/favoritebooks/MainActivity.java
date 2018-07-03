package com.example.android.favoritebooks;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.android.favoritebooks.data.BookContract.BookEntry;
import com.example.android.favoritebooks.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int LOADER_ID = 0;

    private BookCursorAdapter bookAdapter;

    private ActivityMainBinding binding;

    // Defines a new Uri object that receives the result of the insertion
    private Uri newUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Setup FAB to open EditorActivity
       binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        binding.listViewBooks.setEmptyView(binding.emptyView);

        // Setup cursor adapter using cursor
        bookAdapter = new BookCursorAdapter(this,null);

        // Attach cursor adapter to the ListView
       binding.listViewBooks.setAdapter(bookAdapter);

        binding.listViewBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openEditorActivity = new Intent(MainActivity.this, EditorActivity.class);
                Uri itemUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                openEditorActivity.setData(itemUri);
                startActivity(openEditorActivity);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void insertBook() {

        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT, getString(R.string.fault_in_stars));
        values.put(BookEntry.COLUMN_PRODUCT_DESCRIPTION, getString(R.string.description_fault_in_stars));
        values.put(BookEntry.COLUMN_PRICE, 1000);
        values.put(BookEntry.COLUMN_QUANTITY, 1);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, getString(R.string.supplier_fault_in_stars));
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, 23450);
        values.put(BookEntry.COLUMN_PRODUCT_IMAGE_URI, getString(R.string.default_image));

        newUri = getContentResolver().insert(
                BookEntry.CONTENT_URI,   // the books table content URI
                values);                        // the values to insert
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll(){
        showDeleteConfirmationDialog();
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the books.
                int mRowsDeleted = getContentResolver().delete(
                        BookEntry.CONTENT_URI,   // the content URI
                        null, // the column to select on
                        null // the value to compare to
                );

                if (mRowsDeleted == 0) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(MainActivity.this, R.string.main_delete_books_failed, Toast.LENGTH_LONG).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(MainActivity.this, R.string.main_delete_books_successful,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {BookEntry._ID,
               BookEntry.COLUMN_PRODUCT,
              BookEntry.COLUMN_PRICE,
               BookEntry.COLUMN_QUANTITY,
               BookEntry.COLUMN_PRODUCT_IMAGE_URI};

        return new CursorLoader(this, BookEntry.CONTENT_URI,
               projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookAdapter.swapCursor(null);
    }
}
