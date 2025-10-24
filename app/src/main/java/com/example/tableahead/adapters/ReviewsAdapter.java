package com.example.tableahead.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

import com.example.tableahead.models.Review;
import com.example.tableahead.R;
import com.google.firebase.auth.FirebaseAuth;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
    private final Context context;
    private final List<Review> reviewsList;
    private final OnItemClickListener clickListener;
    final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public ReviewsAdapter(List<Review> reviewsList, ReviewsAdapter.OnItemClickListener listener, Context context) {
        this.clickListener = listener;
        this.reviewsList = reviewsList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView reviewerNameTextView;
        final TextView reviewContentTextView;
        final TextView ratingTextView;
        final Button editReviewButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewerNameTextView = itemView.findViewById(R.id.reviewerNameTextView);
            reviewContentTextView = itemView.findViewById(R.id.reviewContentTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            editReviewButton = itemView.findViewById(R.id.btnEditReview);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }


    public interface OnItemClickListener {
        /** @noinspection unused, unused */
        void onItemClick(Review review, int position);
//        void onReviewEdit(Review review, int position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewsList.get(position);
        holder.reviewerNameTextView.setText(review.getReviewerName());
        holder.reviewContentTextView.setText(review.getReviewContent());
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        holder.ratingTextView.setText( String.format("%s: %s", context.getString(R.string.rating) , decimalFormat.format(review.getRating())));
        // edit your own reviews
        if (review.getReviewerId().equals(mAuth.getUid())){
            holder.editReviewButton.setVisibility(View.VISIBLE);
        }else{
            holder.editReviewButton.setVisibility(View.INVISIBLE);
        }

//        // Set an OnClickListener for the item view
//        holder.itemView.setOnClickListener(view -> {
//            if (clickListener != null) {
//                clickListener.onItemClick(review, position);
//            }
//        });
        holder.editReviewButton.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onItemClick(review, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }
}
