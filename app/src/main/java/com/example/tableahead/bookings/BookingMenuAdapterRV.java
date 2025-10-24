package com.example.tableahead.bookings;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.models.MenuItemModel;
import com.example.tableahead.R;

import java.util.List;

public class BookingMenuAdapterRV extends RecyclerView.Adapter<BookingMenuAdapterRV.MenuItemViewHolder> {
    private List<MenuItemModel> orderList;
    /** @noinspection FieldCanBeLocal, unused */
    private final Context context;
    private final OnItemClickListener clickListener;

//    public BookingAdapterRV(Context context, List<Booking> bookings, tableDataRVAdapter.OnItemClickListener listener) {
    public BookingMenuAdapterRV(Context context, List<MenuItemModel> orderList, OnItemClickListener listener ) {
        this.orderList = orderList;
        this.context = context;
        this.clickListener = listener;
    }
    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_row_overview, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingMenuAdapterRV.MenuItemViewHolder holder, int position) {
        MenuItemModel menuItem = orderList.get(position);
        holder.txtDesc.setText(menuItem.getItemDescription());
        holder.txtName.setText(menuItem.getItemName());
        holder.txtPrice.setText(menuItem.getItemPrice());


        // Set an OnClickListener for the item view
        holder.itemView.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onItemClick(menuItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    /** @noinspection unused*/
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<MenuItemModel> itemList) {
        this.orderList = itemList;
        notifyDataSetChanged();
    }

    // Define an interface for item click events
    public interface OnItemClickListener {
        /** @noinspection unused*/
        void onItemClick(MenuItemModel menuItem);
    }

    /**
     * VIEW HOLDER
     */
    public static class MenuItemViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName;
        final TextView txtPrice;
        final TextView txtDesc;
        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtItemName);
            txtPrice = itemView.findViewById(R.id.txtItemPrice);
            txtDesc = itemView.findViewById(R.id.txtItemDescription);
        }
    }
}
