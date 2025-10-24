package com.example.tableahead.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.tableahead.R;
import com.example.tableahead.accountScreen;
import com.example.tableahead.helpScreen;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * Activity class that has the user select some selection of categories, imported from database,
 * queries the database on all restaurants that have that category as their category attribute,
 * passes found IDs to ResultScreen. Implements interfaces for RecyclerView item button functionality.
 * @author Ethan Rody
 */
public class CategoryScreen extends AppCompatActivity implements CategoryRVInterface, SelectedRVInterface {
    private static final String SELECTED = "selected_categories";
    public static final String SHARED_PREFS = "sharedPrefs";
    final ArrayList<CategoryModel> categoryModels = new ArrayList<>();
    final ArrayList<CategoryModel> selectedCategories = new ArrayList<>();
    CategoryRecyclerViewAdapter catAdapter;
    SelectedRecyclerViewAdapter selAdapter;
    FragmentContainerView fragmentContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_screen);

        fragmentContainerView = findViewById(R.id.fragment);

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

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < selectedCategories.size(); i++) {
                sb.append(selectedCategories.get(i).getId()).append(" ");
            }
            String selectedIDs = sb.toString();
            editor.putString(SELECTED, selectedIDs);
            editor.apply();

            startActivity(new Intent(this, SelectTypeScreen.class));
        });

        setUpCategoryModels();

        RecyclerView categories_rv = findViewById(R.id.categories_rv);
        RecyclerView selected_rv = findViewById(R.id.selected_rv);

        catAdapter = new CategoryRecyclerViewAdapter(this, categoryModels, this);
        categories_rv.setAdapter(catAdapter);
        categories_rv.setLayoutManager(new LinearLayoutManager(this));

        selAdapter = new SelectedRecyclerViewAdapter(this, selectedCategories, this);
        selected_rv.setAdapter(selAdapter);
        selected_rv.setLayoutManager(new LinearLayoutManager(this));

        Button searchBtn = findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(search);

    }

    /**
     * Queries database to get list of categories. Also gets saved selected categories from
     * SharedPreferences.
     */
    private void setUpCategoryModels() {
        ArrayList<String> categoryIDs = new ArrayList<>();
        ArrayList<String> categoryNames = new ArrayList<>();
        ArrayList<String> categoryDescriptions = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference categoriesRef = db.collection("categories");
        Task<QuerySnapshot> query = categoriesRef.get();

        query.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    String id = document.getId();
                    String name = document.getString("categoryName");
                    String description = document.getString("categoryDescription");

                    categoryIDs.add(id);
                    categoryNames.add(name);
                    categoryDescriptions.add(description);
                }

                SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                String selectedIDString = prefs.getString(SELECTED, null);

                if (selectedIDString != null && !selectedIDString.equals("")) {
                    String[] selectedIDStringSplit = selectedIDString.split(" ");

                    ArrayList<String> selectedIDs = new ArrayList<>();
                    Collections.addAll(selectedIDs, selectedIDStringSplit);

                    for (int i = 0; i < categoryIDs.size(); i++){
                        if (selectedIDs.contains(categoryIDs.get(i))) {
                            selectedCategories.add(new CategoryModel(categoryNames.get(i), categoryDescriptions.get(i), categoryIDs.get(i)));
                            selAdapter.notifyItemInserted(selectedCategories.size()-1);
                        } else {
                            categoryModels.add(new CategoryModel(categoryNames.get(i), categoryDescriptions.get(i), categoryIDs.get(i)));
                            catAdapter.notifyItemInserted(selectedCategories.size()-1);
                        }
                    }
                } else {
                    for (int i = 0; i < categoryIDs.size(); i++){
                        categoryModels.add(new CategoryModel(categoryNames.get(i), categoryDescriptions.get(i), categoryIDs.get(i)));
                        catAdapter.notifyItemInserted(selectedCategories.size()-1);
                    }
                }
                fragmentContainerView.removeAllViews();
            }
            else {
                Log.e("CategoryScreen", "Error: failed to get categories from Firestore db");
            }
        });
    }

    /**
     * Implements search, queries database
     */
    private final View.OnClickListener search = view -> {
        if (selectedCategories.size() == 0) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.category_screen), R.string.selectAtLeastOneCat, Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            ArrayList<String> selectedCategoryIDs = new ArrayList<>();

            SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < selectedCategories.size(); i++) {
                selectedCategoryIDs.add(selectedCategories.get(i).getId());
                sb.append(selectedCategories.get(i).getId()).append(" ");
            }
            String selectedIDs = sb.toString();
            editor.putString(SELECTED, selectedIDs);
            editor.apply();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference restaurantsRef = db.collection("restaurants");
            Query query = restaurantsRef.whereIn("category", selectedCategoryIDs);
            Task<QuerySnapshot> querySnapshotTask = query.get();

            querySnapshotTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ArrayList<String> restaurantIds = new ArrayList<>();
                    QuerySnapshot querySnapshot = task.getResult();

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
    };

    /**
     * CategoryRecyclerView text click functionality, displays category description in AlertDialog
     * @param position of item in associated ArrayList
     */
    @Override
    public void onItemClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryScreen.this);
        builder.setMessage(categoryModels.get(position).getDescription()) //Add a dialog message to strings.xml
            .setTitle(categoryModels.get(position).getName())
            .setPositiveButton(R.string.ok, (dialog, id) -> {
            })
            .show();
    }

    /**
     * CategoryRecyclerView button functionality, moves category to other RV
     * @param position of item in associated ArrayList
     */
    @Override
    public void onBtnClick(int position) {
        CategoryModel selectedCategory = categoryModels.get(position);
        categoryModels.remove(position);
        catAdapter.notifyItemRemoved(position);

        selectedCategories.add(selectedCategory);
        selAdapter.notifyItemInserted(selectedCategories.size() - 1);
    }

    /**
     * SelectedRecyclerView button functionality, moves category to other RV
     * @param position of item in associated ArrayList
     */
    @Override
    public void onItemClickSelected(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryScreen.this);
        builder.setMessage(selectedCategories.get(position).getDescription()) //Add a dialog message to strings.xml
            .setTitle(selectedCategories.get(position).getName())
            .setPositiveButton(R.string.ok, (dialog, id) -> {
            })
            .show();
    }

    /**
     * SelectedRecyclerView text click functionality, displays category description in AlertDialog
     * @param position of item in associated ArrayList
     */
    @Override
    public void onBtnClickSelected(int position) {
        CategoryModel deselectedCategory = selectedCategories.get(position);
        selectedCategories.remove(position);
        selAdapter.notifyItemRemoved(position);

        categoryModels.add(deselectedCategory);
        catAdapter.notifyItemInserted(categoryModels.size() - 1);
    }
}