package com.example.tableahead.search;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.R;
import com.example.tableahead.RestaurantReview;
import com.example.tableahead.accountScreen;
import com.example.tableahead.helpScreen;
import com.example.tableahead.tableScreen;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The ResultScreen activity displays information about the restaurant IDs
 * passed to it in a RecyclerView. It implements ResultRVInterface which allows
 * the user to click a button that will move them to the tableScreen activity and
 * pass the ID of the restaurant that the button was clicked on.
 * @ author Ethan Rody
 */
public class ResultScreen extends AppCompatActivity implements ResultRVInterface, AdapterView.OnItemSelectedListener {
    private static final int REQUEST_LOCATION = 1;
    private static final int REQUEST_CODE = 2;
    final ArrayList<RestaurantModel> restaurantModels = new ArrayList<>();
    ResultRecyclerViewAdapter adapter;
    RecyclerView results_rv;
    Spinner spinner;
    final ArrayList<String> paths = new ArrayList<>();
    double currLat = 43.458290;
    double currLon = -80.539580;
    // used to update the rating upon adding a review  (edit, delete not done)
    Double newRating = 0.0;
    long newCount = 0;
    int positionG;

    /** @noinspection unchecked*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result_screen);
        int[] stringIds = {R.string.distance, R.string.popularity, R.string.rating};
        for (int id : stringIds) {
            paths.add(getResources().getString(id));
        }
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paths);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

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

        results_rv = findViewById(R.id.results_rv);

        adapter = new ResultRecyclerViewAdapter(this, restaurantModels, this);
        results_rv.setAdapter(adapter);
        results_rv.setLayoutManager(new LinearLayoutManager(this));

        results_rv.setVisibility(View.INVISIBLE);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ArrayList<String> resultIds = (ArrayList<String>) extras.get("ids");
            if (resultIds != null) {
                for (int i = 0; i < resultIds.size(); i++) {
                    restaurantModels.add(new RestaurantModel(resultIds.get(i)));
                    adapter.notifyItemInserted(restaurantModels.size()-1);
                }
            }
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, SelectTypeScreen.class));
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        LoadRestaurantInfo();
    }

    /**
     * Sorts restaurantList by their distance attributes
     * @param restaurantList ArrayList<RestaurantModel> to sort
     */
    @SuppressLint("NotifyDataSetChanged")
    private void SortByDistance(ArrayList<RestaurantModel> restaurantList) {
        Comparator<RestaurantModel> distanceComparator = Comparator.comparingDouble(RestaurantModel::getDistance);
        restaurantList.sort(distanceComparator);
        adapter.notifyDataSetChanged();
    }


    /**
     * Sorts restaurantList by their reviewRating attributes
     * @param restaurantList ArrayList<RestaurantModel> to sort
     */
    @SuppressLint("NotifyDataSetChanged")
    private void SortByRating(ArrayList<RestaurantModel> restaurantList) {
        Comparator<RestaurantModel> popularityComparator = Comparator.comparingDouble(RestaurantModel::getReviewRating);
        restaurantList.sort(popularityComparator.reversed());
        adapter.notifyDataSetChanged();
    }

    /**
     * Sorts restaurantList by their popularity attributes
     * @param restaurantList ArrayList<RestaurantModel> to sort
     */
    @SuppressLint("NotifyDataSetChanged")
    private void SortByPopularity(ArrayList<RestaurantModel> restaurantList) {
        Comparator<RestaurantModel> popularityComparator = Comparator.comparingInt(RestaurantModel::getPopularity);
        restaurantList.sort(popularityComparator.reversed());
        adapter.notifyDataSetChanged();
    }

    /**
     * Listener for when user location changes
     * Upon change get new location and reload restaurant info to update distances.
     */
    @SuppressLint("NotifyDataSetChanged")
    final LocationListener locationListener = location -> {
        currLat = location.getLatitude();
        currLon = location.getLongitude();

        LoadRestaurantInfo();
        adapter.notifyDataSetChanged();
    };

    /**
     * Loads information about restaurants from database, stores in ArrayList<RestaurantModel> restaurantList
     */
    void LoadRestaurantInfo() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference restaurantsRef = db.collection("restaurants");
            Task<QuerySnapshot> querySnapshotTask = restaurantsRef.get();
            querySnapshotTask.addOnCompleteListener(runnable -> {
                if (querySnapshotTask.isSuccessful()) {
                    QuerySnapshot querySnapshot = querySnapshotTask.getResult();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                       for (RestaurantModel restaurantModel : restaurantModels) {
                           if (restaurantModel.getId().equals(document.getId()) ) {
                               String name = document.getString("name");
                               float lat = Objects.requireNonNull(document.getDouble("lat")).floatValue();
                               float lon = Objects.requireNonNull(document.getDouble("lon")).floatValue();
                               String imageLink = document.getString("imageLink");

                               int popularity = Objects.requireNonNull(document.getDouble("popularity")).intValue();
                               long reviewCount = document.getLong("reviewCount");
                               Double reviewRating = document.getDouble("reviewRating");

                               float[] results = new float[3];
                               Location.distanceBetween(currLat, currLon, lat, lon, results);
                               float distance = results[0];

                               restaurantModel.setName(name);
                               restaurantModel.setDistance(distance);
                               restaurantModel.setPopularity(popularity);
                               restaurantModel.setImageLink(imageLink);
                               restaurantModel.setReviewCount(reviewCount);
                               restaurantModel.setReviewRating(reviewRating);

//                               Log.i("ResultScreen", "image link: " + restaurantModel.getImageLink());
//                               Log.i("ResultScreen", restaurantModel.toString());
                           }
                       }
                    }

                    SortByDistance(restaurantModels);
                    results_rv.setVisibility(View.VISIBLE);

                }
//                else {Log.e("CategoryScreen", "Error: failed to get categories from Firestore db"); }
            });
        });
    }


    /**
     *  Allows the user to click a button that will move them to the tableScreen activity and
     *  passes the ID of the restaurant that the button was clicked on.
     * @param position the restaurant's position within the ArrayList
     */
    @Override
    public void onBtnClickResult(int position) {
        String id = restaurantModels.get(position).getId();
        Intent intent = new Intent(this, tableScreen.class);
        intent.putExtra("restaurantId", id);
//        Log.d("ResultScreen", id);
        //noinspection deprecation
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     *  Displays a list of reviews for the restaurant when clicking on the
     *  review segment of the RecyclerView item
     * @param position the restaurant's position within the ArrayList
     */
    @Override
    public void onReviewClickResult(int position, float selectedRating) {
//        Log.d("ResultScreen", " onReviewClickResult " + selectedRating);
        positionG = position;
        String id = restaurantModels.get(position).getId();
        Intent intent = new Intent(this, RestaurantReview.class);
        intent.putExtra("restaurantId", id);
        intent.putExtra("selectedRating", Double.valueOf(String.valueOf(selectedRating)));
        //noinspection deprecation
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                newRating = data.getDoubleExtra("newRating", newRating);
                newCount = data.getLongExtra("newCount", 1);
//                Log.d("ResultScreen", "new rating " + newRating.toString() + "  newCount " + newCount);
                restaurantModels.get(positionG).setReviewRating(newRating);
                restaurantModels.get(positionG).setReviewCount(newCount);
                adapter.notifyItemChanged(positionG);

            }

        }
    }
    /**
     * Does selected sort type upon selection
     * @param adapterView refers to adapter
     * @param view refers to view
     * @param i refers to item position
     * @param l refers to item id
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                SortByDistance(restaurantModels);
                break;
            case 1:
                SortByPopularity(restaurantModels);
                break;
            case 2:
                SortByRating(restaurantModels);
                break;
        }
    }
    /**
     * Does nothing, auto generated from interface
     * @param adapterView refers to adapter
     */
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

}
