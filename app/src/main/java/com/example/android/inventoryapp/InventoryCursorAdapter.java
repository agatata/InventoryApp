package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.android.inventoryapp.data.InventoryContract;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
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
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        // Read the pet attributes from the Cursor for the current pet
        String productName = cursor.getString(nameColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current item
        holder.nameTextView.setText(productName);
        holder.priceTextView.setText(Integer.toString(productPrice));
        holder.quantityTextView.setText(Integer.toString(productQuantity));
    }

    static class ViewHolder {
        @BindView(R.id.list_item_name)
        TextView nameTextView;
        @BindView(R.id.list_item_price)
        TextView priceTextView;
        @BindView(R.id.list_item_quantity)
        TextView quantityTextView;
        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
