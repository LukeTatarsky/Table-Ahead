package com.example.tableahead.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * RecyclerView for search results, used in ResultScreen
 * @author Ethan Rody
 */
public class ResultRecyclerViewAdapter extends RecyclerView.Adapter<ResultRecyclerViewAdapter.ResultViewHolder> {
    private final ResultRVInterface resultRVInterface;
    final Context context;
    final ArrayList<RestaurantModel> restaurantModels;
    public ResultRecyclerViewAdapter(Context context, ArrayList<RestaurantModel> restaurantModels, ResultRVInterface resultRVInterface) {
        this.context = context;
        this.restaurantModels = restaurantModels;
        this.resultRVInterface = resultRVInterface;
    }

    @NonNull
    @Override
    public ResultRecyclerViewAdapter.ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.restaurant_row, parent, false);

        return new ResultRecyclerViewAdapter.ResultViewHolder(view, resultRVInterface);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ResultRecyclerViewAdapter.ResultViewHolder holder, int position) {
        holder.name_tv.setText(restaurantModels.get(position).getName());

        if (!restaurantModels.get(position).getImageLink().equals("")) {
            Picasso.get()
                    .load(restaurantModels.get(position).getImageLink())
                    .into(holder.rest_iv);
        }
        // after info received
        if (restaurantModels.get(position).getDistance() >= 0) {
            float distance = restaurantModels.get(position).getDistance()/1000;
            if (distance < 10) holder.dist_tv.setText(String.format("%.2f km", distance));
            else if (distance < 100) holder.dist_tv.setText(String.format("%.1f km", distance));
            else if (distance < 1000) holder.dist_tv.setText(String.format("%.0f km", distance));
            else holder.dist_tv.setText(R.string.big_dist);

            // Rating updates
            Double rating = restaurantModels.get(position).getReviewRating();
            holder.ratingBar.setRating(restaurantModels.get(position).getReviewRating().floatValue());
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            holder.ratingTxt.setText( decimalFormat.format(rating));
            holder.ratingCountTxt.setText(String.format("(%s)", restaurantModels.get(position).getReviewCount()));
        }
    }

    @Override
    public int getItemCount() { return restaurantModels.size(); }

    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        final TextView name_tv;
        final TextView dist_tv;
        final FloatingActionButton book_btn;
        final ImageView rest_iv;
        final RatingBar ratingBar;
        final TextView ratingTxt;
        final TextView ratingCountTxt;

        public ResultViewHolder(@NonNull View itemView, ResultRVInterface resultRVInterface) {
            super(itemView);

            name_tv = itemView.findViewById(R.id.name_tv);
            dist_tv = itemView.findViewById(R.id.dist_tv);
            book_btn = itemView.findViewById(R.id.book_btn);
            rest_iv = itemView.findViewById(R.id.rest_iv);
            ratingBar = itemView.findViewById(R.id.ratingBarReview);
            ratingTxt = itemView.findViewById(R.id.txtRating);
            ratingCountTxt = itemView.findViewById(R.id.txtReviewCount);

            book_btn.setOnClickListener(view -> {
                if (resultRVInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        resultRVInterface.onBtnClickResult(pos);
                    }
                }
            });

            ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser){
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // revert bar to original rating, send selected rating on.
                        // french language uses , insead of .   quick fix
                        String[] split = ratingTxt.getText().toString().split(",");
                        if (split.length == 2){
                            ratingBar.setRating(Float.parseFloat(split[0]+"."+split[1]));
                        }
                        else{
                            ratingBar.setRating(Float.parseFloat(ratingTxt.getText().toString()));
                        }

                        resultRVInterface.onReviewClickResult(pos, rating);
                    }
                }
            });
        }
    }
}
