package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks <Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    //Identifier for the product data loader
    private static final int EXISTING_PRODUCT_LOADER = 0;
    //Identifier for pick image request
    private static final int PICK_IMAGE_REQUEST = 1;

    @BindView(R.id.edit_name)
    EditText nameEditText;
    @BindView(R.id.edit_price)
    EditText priceEditText;
    @BindView(R.id.edit_quantity)
    EditText quantityEditText;
    @BindView(R.id.edit_supplier_name)
    EditText supplierNameEditText;
    @BindView(R.id.edit_phone_number)
    EditText phoneNumberEditText;
    @BindView(R.id.edit_quantity_decrease)
    Button quantityDecreaseButton;
    @BindView(R.id.edit_quantity_increase)
    Button quantityIncreaseButton;
    @BindView(R.id.edit_order_button)
    Button orderButton;
    @BindView(R.id.add_photo_btn)
    Button addPhotoButton;
    @BindView(R.id.photo_image_view)
    ImageView photoImageView;

    // Content URI for the existing product (null if it's a new product)
    private Uri currentProductUri;

    // URI for added photo
    private Uri photoUri;

    // Boolean flag that keeps track of whether the product has been edited
    private boolean productHasChanged = false;

    //int for quantity check
    private int givenQuantity;


    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the productHasChanged boolean to true.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        // If the intent does not contain a product content URI, create a new product
        if (currentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_item));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
            orderButton.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        nameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        phoneNumberEditText.setOnTouchListener(touchListener);
        quantityIncreaseButton.setOnTouchListener(touchListener);
        quantityIncreaseButton.setOnTouchListener(touchListener);
        photoImageView.setOnTouchListener(touchListener);

        //Decrease quantity of products
        quantityDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = quantityEditText.getText().toString();
                if (TextUtils.isEmpty(quantity)) {
                    quantityEditText.setText("0");
                } else {
                    givenQuantity = Integer.parseInt(quantity);
                    //To validate if quantity is greater than 0
                    if (givenQuantity >= 1) {
                        quantityEditText.setText(String.valueOf(givenQuantity - 1));
                    } else {
                        Toast.makeText(EditorActivity.this, R.string.editor_quantity_less_than_zero, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //Increase quantity of products
        quantityIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = quantityEditText.getText().toString();
                if (TextUtils.isEmpty(quantity)) {
                    quantityEditText.setText("0");
                } else {
                    givenQuantity = Integer.parseInt(quantity);
                    quantityEditText.setText(String.valueOf(givenQuantity + 1));
                }

            }
        });

        // addPhotoButton - Open Image Selector, in case of adding the first photo
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
        // addPhotoButton - Open Image Selector, in case of photo's change
        photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });

        // Implementing orderButton functionality
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEditText.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(EditorActivity.this, R.string.editor_phone_number_empty, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveItem();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // If there are unsaved changes, setup a dialog.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * SAVE ITEM - get user input from editor and save new item into database.
     */
    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplierNameString = supplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = phoneNumberEditText.getText().toString().trim();

        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        if (currentProductUri == null && TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierPhoneNumberString)) {
            Toast.makeText(this, getString(R.string.editor_all_empty),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.editor_empty_name),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.editor_empty_price),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.editor_empty_supplier),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(supplierPhoneNumberString)) {
            Toast.makeText(this, getString(R.string.editor_empty_phone_number),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        int price = Integer.parseInt(priceString);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, price);
        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);
        String photo;
        if (photoUri != null) {
            photo = String.valueOf(photoUri);
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PHOTO_ID, photo);
        }

        // Determine if this is a new or existing product
        if (currentProductUri == null) {
            // This is a new item, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // An existing product: update with content URI: currentProductUri and pass in
            // the new ContentValues. Pass in null for the selection and selection args -
            // currentProductUri will already identify the correct row in the database.
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * DELETE ITEM - delete confirmation dialog and deleteItem method
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        // Only perform the delete if this is an existing product.
        if (currentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);

            // Toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * BACK to parent activity (menu option) - a dialog that warns about unsaved changes
     *
     * @param discardButtonClickListener for what to do when the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    /**
     * BACK - This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }
        // If there are unsaved changes, setup a dialog to warn the user.
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

    @Override
    public Loader <Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all items attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PHOTO_ID};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentProductUri,              // Query the content URI for the current pet
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader <Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the proper columns of product attributes
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            int photoColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PHOTO_ID);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            String photo = cursor.getString(photoColumnIndex);


            // Update the views on the screen with the values from the database
            nameEditText.setText(name);
            priceEditText.setText(Integer.toString(price));
            quantityEditText.setText(Integer.toString(quantity));
            supplierNameEditText.setText(supplierName);
            phoneNumberEditText.setText(supplierPhone);
            if (photo != null) {
                Uri photoUri = Uri.parse(photo);
                photoImageView.setImageBitmap(getBitmapFromUri(photoUri));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        supplierNameEditText.setText("");
        phoneNumberEditText.setText("");
    }

    /**
     * ADD PHOTO - pic the photo from external gallery and get its URI
     * Thank to: https://github.com/crlsndrsjmnz/MyShareImageExample
     * and Sudhir Khanger
     */

    // open image selector and set the file's type
    public void openImageSelector() {
        Intent photoIntent;
        // create Intent depending on the API
        if (Build.VERSION.SDK_INT < 19) {
            photoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            photoIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            photoIntent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        photoIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(photoIntent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            //  contain documents URI in the return intent
            if (resultData != null) {
                photoUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + photoUri.toString());
                photoImageView.setImageBitmap(getBitmapFromUri(photoUri));
            }
        } else {
            Log.i(LOG_TAG, "Something doesn't work");
            Toast.makeText(this, "Something doesn't work!", Toast.LENGTH_SHORT).show();
        }
    }

    // getBitmapFromUri helper method
    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null || uri.toString().isEmpty())
            return null;
        // Get the dimensions of the View
        int targetW = photoImageView.getWidth();
        int targetH = photoImageView.getHeight();
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
            } catch (IOException ioe) {}
        }
    }
}
