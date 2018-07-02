package com.example.android.favoritebooks;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.example.android.favoritebooks.data.BookContract.BookEntry;
import com.example.android.favoritebooks.data.BookDbHelper;
import com.example.android.favoritebooks.databinding.ActivityEditorBinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int CALL_PHONE_REQUEST = 0;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String URI_STATE = "Uri state";
    private Uri uri;
    private String imageUri;
    private ActivityEditorBinding binding;
    private int quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor);

        binding.buttonCall.setOnClickListener(this);
        binding.buttonPickImage.setOnClickListener(this);
        binding.buttonDecrease.setOnClickListener(this);
        binding.buttonIncrease.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (uri != null)
            outState.putString(URI_STATE, uri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(URI_STATE) &&
                !savedInstanceState.getString(URI_STATE).equals("")) {
            uri = Uri.parse(savedInstanceState.getString(URI_STATE));

            ViewTreeObserver viewTreeObserver = binding.bookImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    binding.bookImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    binding.bookImage.setImageBitmap(getBitmapFromUri(uri));
                }
            });
        }
    }



    private void insertBook() {
        String productName = binding.editName.getText().toString().trim();
        String productDescription = binding.editDescription.getText().toString().trim();
        Integer productPrice = Integer.parseInt(binding.editPrice.getText().toString());
        Integer productQuantity = Integer.parseInt(binding.editQuantity.getText().toString().trim());
        String supplierName = binding.editSupplier.getText().toString().trim();
        Long supplierPhoneNumber = Long.parseLong(binding.editPhone.getText().toString().trim());

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
        values.put(BookEntry.COLUMN_PRODUCT_IMAGE_URI,imageUri);


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
                if (!binding.editQuantity.getText().toString().equals("")){
                quantity = Integer.parseInt(binding.editQuantity.getText().toString());
                quantity++;
                binding.editQuantity.setText(String.valueOf(quantity));}
                break;

            case R.id.button_decrease:
                if (!binding.editQuantity.getText().toString().equals("")){if(quantity>0) quantity--;
                binding.editQuantity.setText(String.valueOf(quantity));}
                break;

            default:
                break;
        }
    }

    public void selectImage(){
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
                uri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + uri.toString());
                binding.bookImage.setImageBitmap(getBitmapFromUri(uri));
                imageUri = uri.toString();
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
            bitmap = rotateImageIfRequired(this, bitmap, uri);
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
    /**
     * Rotate an image if required.
     *
     * @param bitmap           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap bitmap, Uri selectedImage) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}

