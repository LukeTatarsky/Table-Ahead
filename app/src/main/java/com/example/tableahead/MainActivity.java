package com.example.tableahead;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tableahead.bookings.userBookings;
import com.example.tableahead.search.SelectTypeScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    /** @noinspection FieldCanBeLocal*/
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        //Toolbar
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(c -> startActivity(new Intent(this, SelectTypeScreen.class)));
        Button bookingsButton = findViewById(R.id.bookingsButton);
        bookingsButton.setOnClickListener(c -> startActivity(new Intent(this, userBookings.class)));
        ImageButton helpImg = findViewById(R.id.helpImg);
        helpImg.setOnClickListener(c -> startActivity(new Intent(this, helpScreen.class)));
        ImageButton acntImg = findViewById(R.id.accountImg);
        acntImg.setOnClickListener(c -> startActivity(new Intent(this, accountScreen.class)));


        Button bookingsBtn = findViewById(R.id.bookingsBtn);
        bookingsBtn.setOnClickListener(c -> startActivity(new Intent(MainActivity.this, userBookings.class)));

        Button searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(c -> startActivity(new Intent(MainActivity.this, SelectTypeScreen.class)));


        FirebaseUser currentUser2 = mAuth.getCurrentUser();
        Log.d(TAG, currentUser2.getUid() + " " + currentUser2.getEmail() );
        Log.d(TAG, mAuth.getUid());


    }
}