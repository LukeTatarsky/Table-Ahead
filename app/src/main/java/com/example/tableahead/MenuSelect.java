package com.example.tableahead;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.adapters.MenuRecycleViewAdapter;
import com.example.tableahead.models.MenuItemModel;
import com.example.tableahead.search.SelectTypeScreen;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MenuSelect extends AppCompatActivity implements MenuItemInterface{
    // --Commented out by Inspection (11/26/2023 4:25 PM):private static final String TAG = "MenuSelect";
    private String restaurantDocID = "RfGanWYlN67GAVgcZvu4"; //Default
    private Table table = null; // Contains info such as bookingId and RestaurantId
    private Map<String, Map<String, Object>> MenuCategories;
    private ArrayList<MenuItemModel> itemModels;
    private ArrayList<String> itemIsCategory;
    private ArrayList<MenuItemModel> itemOrder;
    boolean editing = false;
    MenuRecycleViewAdapter adapter;
    final int LAUNCH_SECOND_ACTIVITY = 1;

    /** @noinspection unchecked*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_select);
        MenuCategories = new HashMap<>();
        itemOrder = new ArrayList<>();
        itemModels = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.menuItemsRecyclerView);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        ImageButton accountBtn = findViewById(R.id.accountBtn);
        accountBtn.setOnClickListener(c -> startActivity(new Intent(this, accountScreen.class)));
        ImageButton homeBtn = findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(c -> {
            Intent intent2 = new Intent(this, SelectTypeScreen.class);
            // Kill activity stack
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent2);
            finish();
        });
        ImageButton helpBtn = findViewById(R.id.helpBtn);
        helpBtn.setOnClickListener(c -> startActivity(new Intent(this, helpScreen.class)));
        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(c -> finish());


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            table = (Table) extras.get("table");
            ArrayList<MenuItemModel> tempOrder = (ArrayList<MenuItemModel>) extras.get("order");
            editing = extras.getBoolean("editing");
            if (tempOrder != null) {
                itemOrder = tempOrder;
            }
        }

//        Log.d(TAG, "MenuSelect started up! :)");


        restaurantDocID = table.getRestaurantId();

        setUpMenuData();

        //Log.d(TAG, "order total: " + calculateSubtotal(itemOrder));
        adapter = new MenuRecycleViewAdapter(this, itemModels, itemIsCategory, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button orderButton = findViewById(R.id.orderButton);
        orderButton.setOnClickListener(view -> startOrderEdit());
    }

    /** @noinspection deprecation*/
    private void startOrderEdit() {
        Intent intent = new Intent(this, OrderSelect.class);
        intent.putExtra("order", itemOrder);
        intent.putExtra("table", table);
        intent.putExtra("subtotal", calculateSubtotal(itemOrder));
        if (editing) {
            intent.putExtra("editing", true);
        }
        startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
    }

    /** @noinspection EmptyMethod*/
    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, "order total (resume): " + calculateSubtotal(itemOrder));
    }

    /** @noinspection unchecked*/
    private void setUpMenuData() {
        ArrayList<String> itemNames = new ArrayList<>();
        ArrayList<String> itemDescriptions = new ArrayList<>();
        ArrayList<String> itemPrices = new ArrayList<>();
        itemIsCategory = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants").document(restaurantDocID).collection("menu").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //Log.d(TAG, "Got items successfully!");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            //Get all categories in the "menu" collection
                            MenuCategories.put(document.getId(), document.getData());
                            //Log.d(TAG, Objects.requireNonNull(MenuCategories.get(document.getId())).toString());
                        }
                        /* Every item in MenuCategories is its own map, which
                           describes a category. each category has a name, description
                           and Maps for each item. For each document, we need to get the category
                           name and description, and then iterate through all the items which
                           will always be of the Map type.

                           Should probably redo all of this in just a single "order" map with maps inside of it for items
                            */
                        for (Map.Entry<String, Map<String, Object>> entry : MenuCategories.entrySet()) {
                            Map<String, Object> category = entry.getValue();
                            // Get category name and description
                            String catName = "Default category";
                            String catDesc = "Default category desc";
                            String catPrice = " ";

                            if (category.get("name") != null) { catName = (String) category.get("name");}
                            if (category.get("description") != null) { catDesc = (String) category.get("description");}

                            itemNames.add(catName);
                            itemDescriptions.add(catDesc);
                            itemPrices.add(catPrice);
                            itemIsCategory.add("true");

                            //Log.d(TAG, "Type of object: " + category.getClass());
                            for (Map.Entry<String, Object> sub_entry : category.entrySet()) {
                                //Log.d(TAG, "Object: " + sub_entry.getValue().toString());
//                                if (sub_entry.getValue() instanceof String) {
//                                    //Log.d(TAG, "String: " + sub_entry.getValue());
//                                }
//                                else
                                    if (sub_entry.getValue() instanceof Map) {
                                    //Log.d(TAG, "Map: " + sub_entry.getValue().toString());
                                    // Should work.... this sub entry is of type Map
                                    HashMap<String, Object> currentItem = (HashMap<String, Object>) sub_entry.getValue();
                                    String itemName = "Default name";
                                    String itemDesc = "Default desc";
                                    String itemPrice = "0.00"; //default price
                                    if (currentItem.get("name") != null) { itemName = (String) currentItem.get("name");}
                                    if (currentItem.get("description") != null) { itemDesc = (String) currentItem.get("description");}
                                    if (currentItem.get("price") != null) {itemPrice = currentItem.get("price").toString();}

                                    itemNames.add(itemName);
                                    itemDescriptions.add(itemDesc);
                                    itemPrices.add(itemPrice);
                                    itemIsCategory.add("false");

                                }
                            }
                        }

                        for (int i = 0; i<itemNames.size(); i++) {
                            itemModels.add(new MenuItemModel(itemNames.get(i), itemDescriptions.get(i), itemPrices.get(i)));
                            adapter.notifyItemInserted(itemModels.size()-1);
                        }

                    }
//                    else { Log.d(TAG, "Error getting documents: ", task.getException());}
            });
    }


    @Override
    public void onItemClick(int position) {
        Toast.makeText(getApplicationContext(), itemModels.get(position).getItemName() + " added!", Toast.LENGTH_SHORT).show();
        // Add item to the array of orders.
        MenuItemModel clone = itemModels.get(position).clone();
        itemOrder.add(clone);
        //Log.d(TAG, "Order: " + itemOrder);
    }

    /** @noinspection unchecked*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "MenuSelect onActivityResult  "+ resultCode + "    request " + requestCode);

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    itemOrder = (ArrayList<MenuItemModel>) data.getSerializableExtra("result");
                    boolean editing_finished = data.getBooleanExtra("finished", false);
                    if (editing_finished) {
                        // Editing is finished and we want to return to BookingOverview
//                        Log.d(TAG, "MenuSelect: Editing is finished: " + itemOrder);

                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        returnIntent.putExtra("result", itemOrder);
                        returnIntent.putExtra("table", table);
                        returnIntent.putExtra("restaurantID", table.getRestaurantId());
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                }
            }
            else if(resultCode == Activity.RESULT_FIRST_USER) {
//                Log.d(TAG, "Order was created and BookingOverview was confirmed");
                finish();

            }
        }
    }


    private float calculateSubtotal(ArrayList<MenuItemModel> items) {
        float itemPriceF;
        float subTotal = 0;
        if (items == null) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemPrice() == null) {
                itemPriceF = 0;
            }
            else {
                itemPriceF = Float.parseFloat(items.get(i).getItemPrice());
            }
            subTotal += itemPriceF;
        }
        return subTotal;
    }

}