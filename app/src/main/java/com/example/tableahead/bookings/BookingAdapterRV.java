package com.example.tableahead.bookings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.example.tableahead.R;


import java.util.List;

/** @noinspection FieldMayBeFinal, FieldCanBeLocal */
public class BookingAdapterRV extends RecyclerView.Adapter<BookingAdapterRV.BookingViewHolder> {
    private List<Booking> bookingList;
    /** @noinspection unused*/
    private final Context context;
    private final OnItemClickListener clickListener;

//    public BookingAdapterRV(Context context, List<Booking> bookings, tableDataRVAdapter.OnItemClickListener listener) {
    public BookingAdapterRV(Context context, List<Booking> bookings, OnItemClickListener listener ) {
        this.bookingList = bookings;
        this.context = context;
        this.clickListener = listener;
    }
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_card_view, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingAdapterRV.BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.textRestName.setText(booking.getRestaurantName());
        holder.textPartSize.setText(String.valueOf(booking.getPartySize()));
        holder.textDate.setText(userBookings.parseDate(booking.getDate()));
        holder.textTime.setText(booking.getTime());

        Picasso.get()
                .load(booking.getImageUrl())
                .into(holder.imgRestaurant);

        // Set an OnClickListener for the item view
        holder.itemView.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onItemClick(booking, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<Booking> itemList) {
        this.bookingList = itemList;
        notifyDataSetChanged();
    }

    // Define an interface for item click events
    public interface OnItemClickListener {
        /** @noinspection unused, unused */
        void onItemClick(Booking booking, int position);
    }

    /**
     * VIEW HOLDER
     */
    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        final TextView textRestName;
        final TextView textPartSize;
        final TextView textDate;
        final TextView textTime;
        final ImageView imgRestaurant;
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textRestName = itemView.findViewById(R.id.textViewTitle);
            textPartSize = itemView.findViewById(R.id.txtParty);
            textDate = itemView.findViewById(R.id.txtBookDate);
            textTime = itemView.findViewById(R.id.txtBookTime);
            imgRestaurant = itemView.findViewById(R.id.restImageView);
        }
    }
}
