package com.example.android.favoritebooks;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.example.android.favoritebooks.data.BookContract.BookEntry;
import com.example.android.favoritebooks.databinding.ActivityEditorBinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int LOADER_ID = 0;
    private static final int CALL_PHONE_REQUEST = 0;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String URI_STATE = "Uri state";

    //Item uri from Main Activity
    private Uri itemUri;
    //Image uri
    private Uri uriForImage;
    private String imageUri;
    private ActivityEditorBinding binding;

    // Defines a new Uri object that receives the result of the insertion
    private Uri newUri;

    private int quantity;

    //boolean for checking if book details have changed and needs to be saved before quitting
    private boolean bookHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            bookHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor);

        binding.buttonCall.setOnClickListener(this);
        binding.buttonPickImage.setOnClickListener(this);
        binding.buttonDecrease.setOnClickListener(this);
        binding.buttonIncrease.setOnClickListener(this);

        binding.editName.setOnTouchListener(mTouchListener);
        binding.editDescription.setOnTouchListener(mTouchListener);
        binding.editPrice.setOnTouchListener(mTouchListener);
        binding.editQuantity.setOnTouchListener(mTouchListener);
        binding.editSupplier.setOnTouchListener(mTouchListener);
        binding.editPhone.setOnTouchListener(mTouchListener);
        binding.buttonPickImage.setOnTouchListener(mTouchListener);

        itemUri = getIntent().getData();

        if (itemUri != null) {
            setTitle(getString(R.string.update_product));
            getLoaderManager().initLoader(LOADER_ID, null, this);
        } else {
            setTitle(getString(R.string.add_product));
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (uriForImage != null)
            outState.putString(URI_STATE, uriForImage.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(URI_STATE) &&
                !savedInstanceState.getString(URI_STATE).equals("")) {
            uriForImage = Uri.parse(savedInstanceState.getString(URI_STATE));

            ViewTreeObserver viewTreeObserver = binding.bookImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    binding.bookImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    binding.bookImage.setImageBitmap(getBitmapFromUri(uriForImage));
                }
            });
        }
    }

    private void saveBook() {

        // Check that the product name is not null
        String productName = binding.editName.getText().toString().trim();
        if (productName.equals("")) {
            Toast.makeText(this, R.string.name_required, Toast.LENGTH_LONG).show();
            return;
        }

        // Description is optional
        String productDescription = binding.editDescription.getText().toString().trim();

        // Check that the price is not null and valid
        Integer productPrice = 0;
        if (!binding.editPrice.getText().toString().equals("")) {
            productPrice = Integer.parseInt(binding.editPrice.getText().toString());
        }
        if (binding.editPrice.getText().toString().equals("") || productPrice < 0) {
            Toast.makeText(this, R.string.correct_price, Toast.LENGTH_LONG).show();
            return;
        }

        // Check that the quantity is valid
        Integer productQuantity = 1;
        if (!binding.editQuantity.getText().toString().equals("")) {
            productQuantity = Integer.parseInt(binding.editQuantity.getText().toString().trim());
        }
        if (binding.editQuantity.getText().toString().equals("") || productQuantity < 0) {
            Toast.makeText(this, R.string.correct_quantity, Toast.LENGTH_LONG).show();
            return;
        }

        // Check that the supplier is not null
        String supplierName = binding.editSupplier.getText().toString().trim();
        if (supplierName.equals("")) {
            Toast.makeText(this, R.string.provide_supplier, Toast.LENGTH_LONG).show();
            return;
        }

        // Check that the supplier phone number is not null
        String supplierPhoneNumber = "";
        if (!binding.editPhone.getText().toString().equals("")) {
            supplierPhoneNumber = binding.editPhone.getText().toString().trim();
        }
        if (binding.editPhone.getText().toString().equals("")) {
            Toast.makeText(this, R.string.provide_supplier_phone, Toast.LENGTH_LONG).show();
            return;
        }

        if (itemUri != null) {

            // Create a new map of values, where column names are the keys
            // Defines an object to contain the new values to insert
            ContentValues values = new ContentValues();

            /*
             * Sets the values of each column and inserts the word. The arguments to the "put"
             * method are "column name" and "value"
             */
            values.put(BookEntry.COLUMN_PRODUCT, productName);
            values.put(BookEntry.COLUMN_PRODUCT_DESCRIPTION, productDescription);
            values.put(BookEntry.COLUMN_PRICE, productPrice);
            values.put(BookEntry.COLUMN_QUANTITY, productQuantity);
            values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
            values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);
            if (imageUri == null) {
                imageUri = getString(R.string.default_image);
            } else {
                values.put(BookEntry.COLUMN_PRODUCT_IMAGE_URI, imageUri);
            }

            int updatedRow = getContentResolver().update(
                    itemUri,   // the books table content URI
                    values,    // the values to insert
                    null,
                    null
            );
            // Show a toast message depending on whether or not the insertion was successful
            if (updatedRow == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.error_updating_book, Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.update_successful,
                        Toast.LENGTH_SHORT).show();
                //Exit activity
                finish();
            }

        } else {
            if (TextUtils.isEmpty(productName) &&
                    TextUtils.isEmpty(productDescription) &&
                    TextUtils.isEmpty(binding.editQuantity.getText().toString()) &&
                    TextUtils.isEmpty(binding.editPrice.getText().toString()) &&
                    TextUtils.isEmpty(supplierName) &&
                    TextUtils.isEmpty(binding.editPhone.getText().toString())) {
                finish();
            } else {
                // Create a new map of values, where column names are the keys
                // Defines an object to contain the new values to insert
                ContentValues values = new ContentValues();
                /*
                 * Sets the values of each column and inserts the word. The arguments to the "put"
                 * method are "column name" and "value"
                 */
                values.put(BookEntry.COLUMN_PRODUCT, productName);
                values.put(BookEntry.COLUMN_PRODUCT_DESCRIPTION, productDescription);
                values.put(BookEntry.COLUMN_PRICE, productPrice);
                values.put(BookEntry.COLUMN_QUANTITY, productQuantity);
                values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
                values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);
                if (imageUri == null) {
                    imageUri = getString(R.string.default_image);
                } else {
                    values.put(BookEntry.COLUMN_PRODUCT_IMAGE_URI, imageUri);
                }


                newUri = getContentResolver().insert(
                        BookEntry.CONTENT_URI,   // the books table content URI
                        values                        // the values to insert
                );
                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, R.string.error_saving_data, Toast.LENGTH_LONG).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, R.string.product_saved,
                            Toast.LENGTH_SHORT).show();
                    //Exit activity
                    finish();
                }
            }
        }
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
                saveBook();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                if (!bookHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                } else {
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, close the current activity.
                                    finish();
                                }
                            };

                    // Show dialog that there are unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (itemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!bookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
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
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button_call:
                //Solution how to get permission: https://stackoverflow.com/questions/40125931/
                // how-to-ask-permission-to-make-phone-call-from-android-from-android-version-marsh
                String supplierPhoneNumber = binding.editPhone.getText().toString().trim();
                Log.d("EditorActivity", "Phone is: " + supplierPhoneNumber);
                if (!supplierPhoneNumber.equals("")) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel",
                            supplierPhoneNumber, null));
                    if (ContextCompat.checkSelfPermission(EditorActivity.this,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditorActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_REQUEST);
                    } else {
                        startActivity(intent);
                    }
                } else
                    Toast.makeText(EditorActivity.this, R.string.enter_phone, Toast.LENGTH_SHORT).show();
                break;

            case R.id.button_pick_image:
                // Sources: https://discussions.udacity.com/t/unofficial-how-to-pick-an-image-from-the-gallery/314971
                // and https://github.com/crlsndrsjmnz/MyShareImageExample
                selectImage();
                break;

            case R.id.button_increase:
                if (!binding.editQuantity.getText().toString().equals("")) {
                    quantity = Integer.parseInt(binding.editQuantity.getText().toString());
                    quantity++;
                    binding.editQuantity.setText(String.valueOf(quantity));
                }
                break;

            case R.id.button_decrease:
                if (!binding.editQuantity.getText().toString().equals("")) {
                    if (quantity > 0) quantity--;
                    binding.editQuantity.setText(String.valueOf(quantity));
                }
                break;

            default:
                break;
        }
    }

    public void selectImage() {
        //Source: https://developer.android.com/guide/topics/providers/document-provider#client
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.
        //Source: https://developer.android.com/guide/topics/providers/document-provider#client
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                uriForImage = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + uriForImage.toString());
                binding.bookImage.setImageBitmap(getBitmapFromUri(uriForImage));
                imageUri = uriForImage.toString();
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        // Source: https://developer.android.com/training/camera/photobasics#TaskScalePhoto
        int targetW = binding.bookImage.getWidth();
        int targetH = binding.bookImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_book);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        //delete book
        // Defines a variable to contain the number of rows deleted
// Deletes the words that match the selection criteria
        if (itemUri != null) {
            int mRowsDeleted = getContentResolver().delete(
                    itemUri,   // the user dictionary content URI
                    null, // the column to select on
                    null // the value to compare to
            );

            if (mRowsDeleted == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.editor_error_deleting, Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.editor_deleted_successfully,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {BookEntry._ID,
                BookEntry.COLUMN_PRODUCT,
                BookEntry.COLUMN_PRODUCT_DESCRIPTION,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER,
                BookEntry.COLUMN_PRODUCT_IMAGE_URI};

        return new CursorLoader(this, itemUri,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() == 0) {
            finish();
        } else {
            data.moveToFirst();
            String productName = data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT));
            String productDescription = data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT_DESCRIPTION));
            int price = data.getInt(data.getColumnIndexOrThrow(BookEntry.COLUMN_PRICE));
            int quantity = data.getInt(data.getColumnIndexOrThrow(BookEntry.COLUMN_QUANTITY));
            String supplier = data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_SUPPLIER_NAME));
            String phoneNumber = data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER));
            String imageUri = data.getString(data.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT_IMAGE_URI));

            binding.editName.setText(productName);
            binding.editDescription.setText(productDescription);
            binding.editPrice.setText(String.valueOf(price));
            binding.editQuantity.setText(String.valueOf(quantity));
            binding.editSupplier.setText(supplier);
            binding.editPhone.setText(phoneNumber);
            if (imageUri != null && !imageUri.equals(getString(R.string.default_image))) {
                // imageViewProduct.setImageResource(R.drawable.default_book_nathan_dumlao_unsplash);
                Uri uriImage = Uri.parse(imageUri);
                Log.d(LOG_TAG, "Image uri is: " + uriImage);
                binding.bookImage.setImageBitmap(getBitmapFromUri(uriImage));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.reset();
        binding.editName.setText("");
        binding.editDescription.setText("");
        binding.editPrice.setText("");
        binding.editQuantity.setText("");
        binding.editSupplier.setText("");
        binding.editPhone.setText("");
        binding.bookImage.setImageResource(R.drawable.default_book_nathan_dumlao_unsplash);
    }
}

