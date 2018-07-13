package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class InventoryProvider extends ContentProvider {


    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int PRODUCTS = 100;

    /**
     * URI matcher code for the content URI for a single product in the inventory table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     */
    private static final UriMatcher iUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        iUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, PRODUCTS);
        iUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", PRODUCT_ID);
    }

    /**
     * Database helper object
     */
    private InventoryDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // To hodl the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = iUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = iUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = iUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */

    //TODO not to save data when invalid (except of IllegalArgumentException)
    private Uri insertItem(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that the price is not null and has the positive value
        Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        // Check that the quantity is not null and has the positive value
        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quandity");
        }

        // Check that the supplier's name is not null
        String supplierName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product name requires a suppliers name");
        }

        // Supplier's phone number can be null

        // Get writable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the inventory content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments. Return the number of rows that were successfully updated.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = iUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check that the name is not null
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that the price is not null and has the positive value
        Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        // Check that the quantity is not null and has the positive value
        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quandity");
        }

        // Check that the supplier's name is not null
        String supplierName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product name requires a suppliers name");
        }

        // Supplier's phone number can be null

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get writable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Insert the new product with the given values
        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = iUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

}
