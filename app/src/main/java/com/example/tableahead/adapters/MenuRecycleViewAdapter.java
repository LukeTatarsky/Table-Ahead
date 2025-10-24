package com.example.tableahead.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.MenuItemInterface;
import com.example.tableahead.models.MenuItemModel;
import com.example.tableahead.R;

import java.util.ArrayList;

public class MenuRecycleViewAdapter extends RecyclerView.Adapter<MenuRecycleViewAdapter.MyViewHolder>{
    private final MenuItemInterface menuItemInterface;

    final Context context;
    final ArrayList<MenuItemModel> menuItemModels;
    final ArrayList<String> viewTypes;
    public MenuRecycleViewAdapter(Context context, ArrayList<MenuItemModel> menuItemModels, ArrayList<String> viewTypes, MenuItemInterface menuItemInterface) {
        this.context = context;
        this.menuItemModels = menuItemModels;
        this.viewTypes = viewTypes;
        this.menuItemInterface = menuItemInterface;
    }

    @Override
    public int getItemViewType(int position) {
        // viewType = 0  MenuItemModel using menu_row.xml
        // viewType = 1  MenuCategoryModel using menu_category_row.xml
        switch (viewTypes.get(position)) {
            case "false":
                return 0;
            case "true": //item is a category  title
                return 1;
            case "order": //item is in the order, removable
                return 2;
            default:
                Log.e("MenuSelect", "Wrong category in category list for items");
                return -1;
        }
    }

    @NonNull
    @Override
    public MenuRecycleViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        if (viewType == 0) {
            // Item
            view = inflater.inflate(R.layout.menu_row, parent, false);
            return new MenuRecycleViewAdapter.MyViewHolderItem(view, menuItemInterface);
        }
        else if (viewType == 2) {
            // Item removable
            view = inflater.inflate(R.layout.menu_order_row, parent, false);
            return new MenuRecycleViewAdapter.MyViewHolderItem(view, menuItemInterface);
        }
        else {
            // Category
            view = inflater.inflate(R.layout.menu_category_row, parent, false);
            return new MenuRecycleViewAdapter.MyViewHolder(view, menuItemInterface);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MenuRecycleViewAdapter.MyViewHolder holder, int position) {
        // This works for both types of layout because they use the same ID
        holder.tvName.setText(menuItemModels.get(position).getItemName());
        holder.tvDescription.setText(menuItemModels.get(position).getItemDescription());
        holder.tvPrice.setText(menuItemModels.get(position).getItemPrice());
    }

    @Override
    public int getItemCount() {

        return menuItemModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        final TextView tvName;
        final TextView tvDescription;
        final TextView tvPrice;

        /** @noinspection unused*/
        public MyViewHolder(@NonNull View itemView, MenuItemInterface menuItemInterface) {
            super(itemView);

            tvName = itemView.findViewById(R.id.txtItemName);
            tvDescription = itemView.findViewById(R.id.txtItemDescription);
            tvPrice = itemView.findViewById(R.id.txtItemPrice);

        }
    }

    public static class MyViewHolderItem extends MyViewHolder{
        // This class is for items that also have the add button
        final ImageView tvAddBtn;

        public MyViewHolderItem(@NonNull View itemView, MenuItemInterface menuItemInterface) {
            super(itemView, menuItemInterface);

            tvAddBtn = itemView.findViewById(R.id.item_add_btn);

            tvAddBtn.setOnClickListener(view -> {
                if(menuItemInterface != null) {
                    int pos = getAdapterPosition();

                    if (pos != RecyclerView.NO_POSITION) {
                        menuItemInterface.onItemClick(pos);
                    }
                }
            });

        }
    }
}
