package com.example.tableahead.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.tableahead.R;
import com.example.tableahead.accountScreen;
import com.example.tableahead.helpScreen;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity class that has user enter numeric distance in kilometers and queries database for all
 * restaurants within that distance. Uses users current location to calculate distance. Passes
 * resulting IDs to ResultScreen.
 * @author Ethan Rody
 */
public class DistanceScreen extends AppCompatActivity {
    public static final String SHARED_PREFS = "sharedPrefs";

    public static final String DISTANCE = "distance";
    private static final int REQUEST_LOCATION = 1;
    float selectedDistance;
    double currLat = 43.458290;
    double currLon = -80.539580;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_screen);

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
        backBtn.setOnClickListener(view -> {
            SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putFloat(DISTANCE, selectedDistance);
            editor.apply();

            startActivity(new Intent(this, SelectTypeScreen.class));
        });

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        selectedDistance = prefs.getFloat(DISTANCE, 5.0f);

        EditText distance_selector = findViewById(R.id.distance_selector);
        distance_selector.setText(Float.toString(selectedDistance));

        ImageButton increment_btn = findViewById(R.id.increment_btn);
        ImageButton decrement_btn = findViewById(R.id.decrement_btn);

        increment_btn.setOnClickListener(view -> {
            selectedDistance += 0.5;
            distance_selector.setText(Float.toString(selectedDistance));
        });

        decrement_btn.setOnClickListener(view -> {
            if (selectedDistance - 0.5 > 0) selectedDistance -= 0.5f;
            else selectedDistance = 0f;

            distance_selector.setText(Float.toString(selectedDistance));
        });

        distance_selector.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                try {
                    selectedDistance = Float.parseFloat(text);
                } catch (NumberFormatException e) {
                    selectedDistance = 0f;
                }
            }
        });

        Button search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(search);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, SelectTypeScreen.class));
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
    }

    final LocationListener locationListener = location -> {
        currLat = location.getLatitude();
        currLon = location.getLongitude();
    };

    /**
     * Implements search button functionality, queries database
     */
    private final View.OnClickListener search = view -> {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putFloat(DISTANCE, selectedDistance);
            editor.apply();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference restaurantsRef = db.collection("restaurants");
            Task<QuerySnapshot> querySnapshotTask = restaurantsRef.get();

            querySnapshotTask.addOnCompleteListener(runnable -> {
                if (querySnapshotTask.isSuccessful()) {
                    QuerySnapshot querySnapshot = querySnapshotTask.getResult();

                    ArrayList<String> restaurantIds = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Float lat = Objects.requireNonNull(document.getDouble("lat")).floatValue();
                        Float lon = Objects.requireNonNull(document.getDouble("lon")).floatValue();
                        String id = document.getId();

                        float[] results = new float[3];
                        Location.distanceBetween(currLat, currLon, lat, lon, results);
                        float distance = results[0];

                        Log.d("DistanceScreen", Float.toString(distance));
                        Log.d("DistanceScreen", String.format("%f %f | %f %f", currLat, currLon, lat, lon));

                        if (distance <= (selectedDistance * 1000)) {
                            restaurantIds.add(id);
                        }
                    }

                    Intent intent = new Intent(this, ResultScreen.class);
                    intent.putExtra("ids", restaurantIds);
                    startActivity(intent);

                } else {
                    Log.e("CategoryScreen", "Error: failed to get categories from Firestore db");
                }
            });
        });
    };
}