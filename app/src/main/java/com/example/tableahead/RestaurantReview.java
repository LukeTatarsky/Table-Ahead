package com.example.tableahead;

import android.content.Intent;
import android.os.Bundle;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.adapters.ReviewsAdapter;
import com.example.tableahead.models.Review;
import com.example.tableahead.search.RestaurantModel;
import com.example.tableahead.search.SelectTypeScreen;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** @noinspection FieldCanBeLocal */
public class RestaurantReview extends AppCompatActivity {
//    private final String TAG = "RestaurantReview";
    private static final int REQUEST_CODE = 2;
    private RecyclerView reviewsRecyclerView;
    private ReviewsAdapter reviewsAdapter;
    private List<Review> reviewsList;
    private RestaurantModel restaurant;
    private FirebaseFirestore db;
    private TextView txtRestName;
    private FloatingActionButton fabAddReview;
    String restaurantId = "RfGanWYlN67GAVgcZvu4";
    Double userSelectedRating = 0.0;
    long reviewCount;
    Double reviewRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_review);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // from resultScreen
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            restaurantId =  extras.getString("restaurantId");
            userSelectedRating = extras.getDouble("selectedRating");

        }
//        Log.d(TAG, " The user selected a rating of " + userSelectedRating + " incase we want to use it.");
        txtRestName = findViewById(R.id.restaurantNameTextView);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsList = new ArrayList<>(); // Initialize the reviews list
        restaurant = new RestaurantModel(restaurantId);
//        restaurantList = new ArrayList<>(); // Initialize the list for restaurants

        // Initialize ReviewsAdapter
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        reviewsAdapter = new ReviewsAdapter(reviewsList, this::editReview, getApplicationContext());
        reviewsRecyclerView.setAdapter(reviewsAdapter); // Set ReviewsAdapter to RecyclerView
        fabAddReview = findViewById(R.id.fabAddReview);
        fabAddReview.setOnClickListener(c -> addReview());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        ImageButton accountBtn = findViewById(R.id.accountBtn);
        accountBtn.setOnClickListener(c -> startActivity(new Intent(this, accountScreen.class)));
        ImageButton homeBtn = findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(c -> {
            Intent intent = new Intent(this, SelectTypeScreen.class);
            // Kill activity stack
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        ImageButton helpBtn = findViewById(R.id.helpBtn);
        helpBtn.setOnClickListener(c -> startActivity(new Intent(this, helpScreen.class)));
        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(c -> finish());

        fetchRestaurantData(restaurantId);

        fetchReviewsforRestaurant(restaurantId);

    }

    /** @noinspection deprecation, unused */ // onItemClickListener runs this
    private void editReview(Review review, int position) {
//        Log.d(TAG, "editing " + review.toString());
        Intent intent = new Intent(this, RestaurantReviewWrite.class);
        intent.putExtra("restaurantId", restaurantId);
        intent.putExtra("reviewId", review.getReviewId());
        intent.putExtra("reviewerId", review.getReviewerId());
        intent.putExtra("reviewerName", review.getReviewerName());
        intent.putExtra("reviewContent", review.getReviewContent());
        intent.putExtra("userOriginalRating", review.getRating());
        intent.putExtra("reviewRating", reviewRating);
        intent.putExtra("reviewCount", reviewCount);
        intent.putExtra("editing", true);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /** @noinspection deprecation*/ // FAB runs this
    private void addReview() {
        Intent intent = new Intent(this, RestaurantReviewWrite.class);
        intent.putExtra("restaurantId", restaurantId);
        intent.putExtra("userSelectedRating", userSelectedRating);
        intent.putExtra("reviewRating", reviewRating);
        intent.putExtra("reviewCount", reviewCount);

        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get the result from the second activity
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

//     Fetch restaurant data from Firestore
    private void fetchRestaurantData(String restaurantId) {
        db.collection("restaurants").document(restaurantId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        restaurant.setName(document.getString("name"));
                        restaurant.setPopularity(document.getLong("popularity").intValue());
                        txtRestName.setText(restaurant.getName());
                        reviewCount = document.getLong("reviewCount");
                        reviewRating =  document.getDouble("reviewRating");
                    }

                })
//                .addOnFailureListener(e -> Log.e("RestaurantReview", "Error fetching restaurant data: " + e.getMessage()))
        ;
    }

    // Fetch reviews from Firestore
    private void fetchReviewsforRestaurant(String restaurantId) {
        db.collection("restaurants").document(restaurantId)
                .collection("reviews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {

                        String reviewerName = document.getString("reviewerName");
                        String reviewContent = document.getString("reviewContent");
                        Double rating = document.getDouble("rating");
                        Review review = new Review(reviewerName, reviewContent, rating);
                        review.setReviewerId(document.getString("reviewerId"));
                        review.setReviewId(document.getId());
                        reviewsList.add(review);
                        reviewsAdapter.notifyItemInserted(reviewsList.size());
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
//                    Log.e("FetchReviews", "Failed to fetch reviews: " + e.getMessage());
                });
    }
}
