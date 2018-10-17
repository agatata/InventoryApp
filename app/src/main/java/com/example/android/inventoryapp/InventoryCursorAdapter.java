package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InventoryCursorAdapter extends CursorAdapter {

    InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder;
        // Inflate a list item view using the layout specified in list_item.xml
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ButterKnife.bind(this, view);
        holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        // Find the columns of product attributes
        int idColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);


        // Read the product attributes from the Cursor for the current item
        final int productId = cursor.getInt(idColumnIndex);
        final String productName = cursor.getString(nameColumnIndex);
        final int productPrice = cursor.getInt(priceColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current item
        holder.nameTextView.setText(productName);
        holder.priceTextView.setText(Integer.toString(productPrice));
        holder.quantityTextView.setText(Integer.toString(productQuantity));

        //The functionality for the "sell" button
        holder.sellButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (productQuantity > 0) {
                    int newQuantity = productQuantity - 1;
                    Uri quantityUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, productId);
                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                    context.getContentResolver().update(quantityUri, values, null, null);
                } else {
                    Toast.makeText(context, "This item is out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    static class ViewHolder {
        @BindView(R.id.list_item_name)
        TextView nameTextView;
        @BindView(R.id.list_item_price)
        TextView priceTextView;
        @BindView(R.id.list_item_quantity)
        TextView quantityTextView;
        @BindView(R.id.list_item_sell_btn)
        Button sellButton;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
