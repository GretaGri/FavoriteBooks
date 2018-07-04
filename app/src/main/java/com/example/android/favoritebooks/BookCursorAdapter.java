package com.example.android.favoritebooks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.favoritebooks.data.BookContract.BookEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;

/**
 * Created by Greta Grigutė on 2018-07-02.
 */
public class BookCursorAdapter extends CursorAdapter{
    public static final String LOG_TAG = BookCursorAdapter.class.getSimpleName();
    private ImageView imageViewProduct;
    private Uri uriImage;
    private Context mContext;

    // Constructor
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mContext = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // Fill out this method
        // Find fields to populate in inflated template
        TextView textViewBookTitle = view.findViewById(R.id.text_view_book_title);
        TextView textViewPrice = view.findViewById(R.id.text_view_price);
        TextView textViewQuantity = view.findViewById(R.id.text_view_in_stock);
        ImageView imageViewProduct = view.findViewById(R.id.list_item_image);
        RelativeLayout buttonBuy = view.findViewById(R.id.buy_button);

        // Extract properties from cursor
        String bookTitle = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT));
        Integer price = cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_PRICE));
        Double priceToShow = (double)price/100;
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_QUANTITY));
        String stringUriImage = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_PRODUCT_IMAGE_URI));

        // Populate fields with extracted properties
        if (stringUriImage != null && !stringUriImage.equals(context.getString(R.string.default_image))){
            // imageViewProduct.setImageResource(R.drawable.default_book_nathan_dumlao_unsplash);
            uriImage = Uri.parse(stringUriImage);
            Log.d (LOG_TAG, "Image uri is: " + uriImage);
            imageViewProduct.setImageBitmap(getBitmapFromUri(uriImage));}


        textViewBookTitle.setText(bookTitle);
        textViewPrice.setText(context.getString(R.string.price, NumberFormat.getCurrencyInstance().format(priceToShow)) );
        textViewQuantity.setText(context.getString(R.string.in_stock, quantity));
        final int id = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));

        buttonBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (quantity > 0){
                    int finQuantity = quantity-1;

//Getting the URI with the append of the ID for the row
                    Uri quantityUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

//update the value
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_QUANTITY, finQuantity);
                    mContext.getContentResolver().update(quantityUri, values, null, null);
                }
                else Toast.makeText(mContext, "Out of stock!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        // Source: https://developer.android.com/training/camera/photobasics#TaskScalePhoto
        int targetW = 360; //imageViewProduct.getWidth();
        int targetH = 360;  //imageViewProduct.getHeight();

        InputStream input = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);

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

            input = mContext.getContentResolver().openInputStream(uri);
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
}