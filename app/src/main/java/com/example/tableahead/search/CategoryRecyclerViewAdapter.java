package com.example.tableahead.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * RecyclerView for non-selected category models, used in CategoryScreen
 * @author Ethan Rody
 */
public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryRecyclerViewAdapter.MyViewHolder> {
    private final CategoryRVInterface categoryRVInterface;
    final Context context;
    final ArrayList<CategoryModel> categoryModels;

    public CategoryRecyclerViewAdapter(Context context, ArrayList<CategoryModel> categoryModels, CategoryRVInterface categoryRVInterface) {
        // constructor
        this.context = context;
        this.categoryModels = categoryModels;
        this.categoryRVInterface = categoryRVInterface;
    }

    @NonNull
    @Override
    public CategoryRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates layout (gives a look to the rows)
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.category_row, parent, false);

        return new CategoryRecyclerViewAdapter.MyViewHolder(view, categoryRVInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryRecyclerViewAdapter.MyViewHolder holder, int position) {
        // assigns values to views created in category_row.xml layout file
        holder.tvName.setText(categoryModels.get(position).getName());
    }

    @Override
    public int getItemCount() {
        // gets number of items that are to be displayed
        return categoryModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // grabs views from category_row.xml layout file
        final TextView tvName;
        final FloatingActionButton btn;
        public MyViewHolder(@NonNull View itemView, CategoryRVInterface categoryRVInterface) {
            super(itemView);

            tvName =  itemView.findViewById(R.id.category_tv);
            btn = itemView.findViewById(R.id.btn);

            tvName.setOnClickListener(view -> {
                if (categoryRVInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        categoryRVInterface.onItemClick(pos);
                    }
                }
            });

            btn.setOnClickListener(view -> {
                if (categoryRVInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        categoryRVInterface.onBtnClick(pos);
                    }
                }
            });
        }
    }
}
