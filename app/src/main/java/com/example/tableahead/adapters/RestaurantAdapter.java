package com.example.tableahead.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tableahead.search.RestaurantModel;
import com.example.tableahead.R;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    private List<RestaurantModel> restaurantList;

    /** @noinspection unused*/
    public RestaurantAdapter(List<RestaurantModel> restaurantList) {
        this.restaurantList = restaurantList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView restaurantNameTextView;
        /** @noinspection unused*/
        final TextView restaurantAddressTextView;

        /** @noinspection unused*/
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantNameTextView = itemView.findViewById(R.id.restaurantNameTextView);
            restaurantAddressTextView = itemView.findViewById(R.id.restaurantAddressTextView);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RestaurantModel restaurant = restaurantList.get(position);
        holder.restaurantNameTextView.setText(restaurant.getName());
    }
    @Override
    public int getItemCount() {
        return restaurantList.size();
    }
    /** @noinspection unused*/
    public void setRestaurants(List<RestaurantModel> restaurantList) {
        this.restaurantList = restaurantList;
    }
}