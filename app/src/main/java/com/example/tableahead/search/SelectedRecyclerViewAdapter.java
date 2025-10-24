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
 * RecyclerView for selected category models, used in CategoryScreen
 * @author Ethan Rody
 */
public class SelectedRecyclerViewAdapter extends RecyclerView.Adapter<SelectedRecyclerViewAdapter.SelectedViewHolder> {
    private final SelectedRVInterface selectedRVInterface;
    final Context context;
    final ArrayList<CategoryModel> categoryModels;
    public SelectedRecyclerViewAdapter(Context context, ArrayList<CategoryModel> categoryModels, SelectedRVInterface selectedRVInterface) {
        this.context = context;
        this.categoryModels = categoryModels;
        this.selectedRVInterface = selectedRVInterface;
    }

    @NonNull
    @Override
    public SelectedRecyclerViewAdapter.SelectedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.selected_row, parent, false);

        return new SelectedRecyclerViewAdapter.SelectedViewHolder(view, selectedRVInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedRecyclerViewAdapter.SelectedViewHolder holder, int position) {
        holder.tvName.setText(categoryModels.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return categoryModels.size();
    }

    public static class SelectedViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final FloatingActionButton btn;
        public SelectedViewHolder(@NonNull View itemView, SelectedRVInterface selectedRVInterface) {
            super(itemView);

            tvName =  itemView.findViewById(R.id.category_tv);
            btn = itemView.findViewById(R.id.btn);

            tvName.setOnClickListener(view -> {
                if (selectedRVInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        selectedRVInterface.onItemClickSelected(pos);
                    }
                }
            });

            btn.setOnClickListener(view -> {
                if (selectedRVInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        selectedRVInterface.onBtnClickSelected(pos);
                    }
                }
            });
        }
    }
}
