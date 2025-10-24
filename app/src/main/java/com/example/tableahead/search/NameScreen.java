package com.example.tableahead.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.tableahead.R;
import com.example.tableahead.accountScreen;
import com.example.tableahead.helpScreen;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Activity that has the user enter some text, then queries the database for any restaurants
 * that have that text as part of their name. Passes found restaurant IDs to ResultScreen.
 * @author Ethan Rody
 */
public class NameScreen extends AppCompatActivity {
    EditText search_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_screen);

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

        search_et = findViewById(R.id.search_et);

        Button searchBtn = findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(view -> {
            String searchText;
            if (search_et.getText() == null) {
                searchText = "";
            } else {
                searchText = search_et.getText().toString();
            }

            if (searchText.equals("")) {
                Toast toast = Toast.makeText(NameScreen.this, R.string.emptyName, Toast.LENGTH_LONG);
                toast.show();
            }
            else {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference restaurantsRef = db.collection("restaurants");
                Query query = restaurantsRef
                        .orderBy("name")
                        .startAt(searchText)
                        .endAt(searchText + "\uf8ff");

                Task<QuerySnapshot> querySnapshotTask = query.get();

                querySnapshotTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        ArrayList<String> restaurantIds = new ArrayList<>();

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String id = document.getId();
                            restaurantIds.add(id);
                        }

                        Intent intent = new Intent(this, ResultScreen.class);
                        intent.putExtra("ids", restaurantIds);
                        startActivity(intent);

                    } else {
                        Log.e("CategoryScreen", "Error: failed to get search results from Firestore db");
                    }
                });
            }
        });
    }
}