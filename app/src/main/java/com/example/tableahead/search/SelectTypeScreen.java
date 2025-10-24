package com.example.tableahead.search;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tableahead.R;
import com.example.tableahead.accountScreen;
import com.example.tableahead.bookings.userBookings;
import com.example.tableahead.helpScreen;

import java.util.Objects;

/**
 * Activity that serves to transition the user to their selected search type.
 * Consists of multiple buttons that merely start different activities.
 * @author Ethan Rody
 */
public class SelectTypeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_search_screen);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        //Toolbar
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(c -> finish());
        Button bookingsButton = findViewById(R.id.bookingsButton);
        bookingsButton.setOnClickListener(c -> startActivity(new Intent(this, userBookings.class)));
        ImageButton helpImg = findViewById(R.id.helpImg);
        helpImg.setOnClickListener(c -> startActivity(new Intent(this, helpScreen.class)));
        ImageButton acntImg = findViewById(R.id.accountImg);
        acntImg.setOnClickListener(c -> startActivity(new Intent(this, accountScreen.class)));


        Button search_by_name_btn = findViewById(R.id.search_by_name_btn);
        Button search_by_category_btn = findViewById(R.id.search_by_category_btn);
        Button find_nearby_btn = findViewById(R.id.find_nearby_btn);

        search_by_category_btn.setOnClickListener(c -> startActivity(new Intent(this, CategoryScreen.class)));
        search_by_name_btn.setOnClickListener(c -> startActivity(new Intent(this, NameScreen.class)));
        find_nearby_btn.setOnClickListener(c -> startActivity(new Intent(this, DistanceScreen.class)));
    }
}