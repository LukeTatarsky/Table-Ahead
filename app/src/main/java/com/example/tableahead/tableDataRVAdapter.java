package com.example.tableahead;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class tableDataRVAdapter extends RecyclerView.Adapter<tableDataRVAdapter.MyViewHolder> {

    private final Context context;
    private final ArrayList<Table> tableList;
    private final OnItemClickListener clickListener;

    public tableDataRVAdapter(Context context, ArrayList<Table> tableList, OnItemClickListener listener) {
        this.context = context;
        this.tableList = tableList;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.table_info_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Table table = tableList.get(position);
        holder.tableNumber.setText(String.format("%s", table.getTableNumber()));
        holder.tableSeats.setText(String.format("%s", table.getSeats()));
        holder.tableSection.setText(String.format("%s", table.getSection()));


        // Set an OnClickListener for the item view
        holder.itemView.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onItemClick(table);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView tableNumber;
        final TextView tableSeats;
        final TextView tableSection;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tableNumber = itemView.findViewById(R.id.tableNumber);
            tableSeats = itemView.findViewById(R.id.tableSeats);
            tableSection = itemView.findViewById(R.id.tableSection);
        }
    }

    // Define an interface for item click events
    public interface OnItemClickListener {
        /** @noinspection unused*/
        void onItemClick(Table table);
    }

}
