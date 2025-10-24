package com.example.tableahead;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tableahead.search.SelectTypeScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

public class RestaurantReviewWrite extends AppCompatActivity {
    // --Commented out by Inspection (11/26/2023 4:26 PM):private final String TAG = "RestaurantReviewWrite";
    private String restaurantId = "";
    private String reviewId = "";
    Double userSelectedRating;
    Double userOriginalRating;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private RatingBar ratingBar;
    private EditText ratingContentEditText;
    long reviewCount;
    Double reviewRating;
    private boolean editing = false;
    private String reviewContent;


    public static class Invoker implements Executor {
        @Override
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_review_write);

        ratingBar = findViewById(R.id.ratingBarReview);
        ratingContentEditText = findViewById(R.id.editTxtRatingContent);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            restaurantId =  extras.getString("restaurantId");
            userSelectedRating = extras.getDouble("userSelectedRating");
            userOriginalRating = extras.getDouble("userOriginalRating");
            reviewCount = extras.getLong("reviewCount");
            reviewRating = extras.getDouble("reviewRating");
            reviewId = extras.getString("reviewId");
            editing = extras.getBoolean("editing");
            reviewContent = extras.getString("reviewContent");

        }

        if (editing){
            ratingBar.setRating(userOriginalRating.floatValue());
            ratingContentEditText.setText(reviewContent);
        }else{
            ratingBar.setRating(userSelectedRating.floatValue());
        }

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

        Button submitBtn = findViewById(R.id.btnSubmitReview);
        submitBtn.setOnClickListener(c -> getReviewerName());

        Button cancelBtn = findViewById(R.id.btnCancel);
        cancelBtn.setOnClickListener(c -> finish());
    }
    private void getReviewerName(){
        Executor exe = new Invoker();
        exe.execute( () -> db.collection("userData").document(mAuth.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String firstName = (document.getString("firstName"));
                        String lastName = (document.getString("lastName"));

                        runOnUiThread(() -> {
                            if (editing){
                                updateReview(reviewId, userOriginalRating);
                            }else {
                                writeReview(firstName, lastName);
                            }
                        });
                    }
                })
//                .addOnFailureListener(e -> Log.e("RestaurantReview", "Error fetching reviewer name: " + e.getMessage()))
        );
    }

    private void updateReview(String reviewId, Double userOgRating) {
        Executor exe = new Invoker();
        exe.execute( () -> {
            CollectionReference restaurantReviewRef = db.collection("restaurants")
                    .document(restaurantId).collection("reviews");
            Map<String, Object> reviewDoc = new HashMap<>();
            reviewDoc.put("rating", this.ratingBar.getRating());
            reviewDoc.put("reviewContent", this.ratingContentEditText.getText().toString());

            restaurantReviewRef.document(reviewId).update(reviewDoc)
                    .addOnSuccessListener(document ->  {
//                        Log.d(TAG, "Review succesfully edited " + reviewId);

                        // remove original rating
                        double removedRating = 0.0;
                        if ( reviewCount > 1){
                             removedRating = ((reviewRating * reviewCount) - userOgRating) / (reviewCount - 1);
                        }
//                        Log.d(TAG, String.format("((%s  * %s) - %s) / (%s - 1) = %s",reviewRating, reviewCount, userOgRating, reviewCount, removedRating));
                        Double newRating = ((removedRating * (reviewCount-1)) + this.ratingBar.getRating()) / ((reviewCount));
//                        Log.d(TAG, String.format("((%s  * %s-1) + %s) / (%s) = %s",removedRating, reviewCount, this.ratingBar.getRating(), reviewCount, newRating));
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("reviewRating", newRating);

                        restaurantReviewRef.getParent().update(updates)
                                .addOnSuccessListener(a -> {
//                                    Log.d(TAG, "Rating succesfully updated ");
                                    runOnUiThread(() -> {
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("newRating", newRating);
                                        resultIntent.putExtra("newCount", reviewCount);
                                        // return new rating to results screen and update the recycler view
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    });
                                });

                    })
//                .addOnFailureListener(e -> Log.e(TAG, "Error writing review: " + e.getMessage()))
            ;
        });
    }

    private void writeReview(String firstName, String lastName) {
        Executor exe = new Invoker();
        exe.execute( () -> {
            CollectionReference restaurantReviewRef = db.collection("restaurants")
                    .document(restaurantId).collection("reviews");
                    Map<String, Object> reviewDoc = new HashMap<>();
                    reviewDoc.put("rating", this.ratingBar.getRating());
                    reviewDoc.put("reviewContent", this.ratingContentEditText.getText().toString());
                    reviewDoc.put("reviewerId", mAuth.getUid());
                    reviewDoc.put("reviewerName", String.format("%s %s",firstName,lastName));

            restaurantReviewRef.add(reviewDoc)
                    .addOnSuccessListener(document ->  {
//                        Log.d(TAG, "Review succesfully written ");
                        Double newRating = ((reviewRating * reviewCount) + this.ratingBar.getRating()) / (reviewCount+1);
//                        Log.d(TAG, String.format("((%s  * %s) + %s) / (%s + 1) = %s",reviewRating, reviewCount, this.ratingBar.getRating(), reviewCount, newRating));
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("reviewRating", newRating);
                        updates.put("reviewCount", reviewCount + 1);

                        restaurantReviewRef.getParent().update(updates)
                                .addOnSuccessListener(a -> {
//                                    Log.d(TAG, "Rating succesfully updated ");
                                    runOnUiThread(() -> {
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("newRating", newRating);
                                        resultIntent.putExtra("newCount", reviewCount + 1);

                                        // return new rating to results screen and update the recycler view
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    });
                        });

                    })
//                .addOnFailureListener(e -> Log.e(TAG, "Error writing review: " + e.getMessage()))
                    ;
        });
    }



}