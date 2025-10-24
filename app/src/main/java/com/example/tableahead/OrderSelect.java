package com.example.tableahead;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.adapters.MenuRecycleViewAdapter;
import com.example.tableahead.bookings.BookingOverview;
import com.example.tableahead.models.MenuItemModel;
import com.example.tableahead.search.SelectTypeScreen;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** @noinspection deprecation*/
public class OrderSelect extends AppCompatActivity implements MenuItemInterface{
    // --Commented out by Inspection (11/26/2023 4:25 PM):private static final String TAG = "MenuSelectOrder";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<MenuItemModel> order;
    MenuRecycleViewAdapter adapter;
    Table table = null;
    Float subtotal = 0f;
    String defaultSubText;
    boolean editing = false;
    final int FINISH_REQUEST_CODE = 1;


    /** @noinspection unchecked*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_select);
        //Should be all "order" and same size as itemOrder
        ArrayList<String> itemIsCategory = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.orderItemsRecyclerView);

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
            order = (ArrayList<MenuItemModel>) extras.get("order");
            table = (Table) extras.get("table");
            subtotal = (Float) extras.get("subtotal");
            editing = extras.getBoolean("editing"); //primitive datatype, dont need to check for null
            if (order != null) {
                //Log.d(TAG, order.toString());
                for (int i = 0; i < order.size(); i++) {
                    itemIsCategory.add("order");
                }
                // Make a new list with the category type "order"
                adapter = new MenuRecycleViewAdapter(this, order, itemIsCategory, this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
            }
        }

        TextView subtotalText = findViewById(R.id.subtotalTopText);
        defaultSubText = subtotalText.getText().toString();

        @SuppressLint("DefaultLocale")
        String newText = defaultSubText + String.format("%.2f", subtotal);
        subtotalText.setText(newText);

        // confirm button
        Button confirmButton = findViewById(R.id.orderConfirmButton);
        confirmButton.setOnClickListener(view -> {
            // write order to db
            writeMenu();

            if (editing) {
                // Just editing the order, we want to return the intent
//                Log.d(TAG, "OrderSelect: returning after editing");
                setResult(Activity.RESULT_OK);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result",order);
                returnIntent.putExtra("table",table);
                returnIntent.putExtra("restaurantID",table.getRestaurantId());
                returnIntent.putExtra("finished", true);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
            else {
                // First time we go to the menu, we want to go to BookingOverview from here
//                Log.d(TAG, "OrderSelect: returning without editing");
                Intent intent = new Intent(this, BookingOverview.class);
                intent.putExtra("order", order);
                intent.putExtra("table", table);
                intent.putExtra("subtotal", subtotal);
//                Log.d(TAG, "Going to BookingOverview");
//                setResult(Activity.RESULT_OK); // this finishes parent activity
                setResult(Activity.RESULT_FIRST_USER); // this finishes parent activity
                startActivityForResult(intent, FINISH_REQUEST_CODE);
//                finish();
            }

            //startActivity(intent);
            finish();
        });
        // edit order button
        Button backButton = findViewById(R.id.goBackButton);
        backButton.setOnClickListener(view -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result",order);
            setResult(Activity.RESULT_OK,returnIntent);
//            Log.d(TAG, "Returning on back button");
            finish();
        });
    }
    //write to db
    // need to implement this
    public void writeMenu(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
//            Log.d(TAG,String.format("Writing menu order to booking Doc %s  %s",table.getBookingId(), order.toString()));

            // Create hashmap to write in the database
            HashMap<String, MenuItemModel> tempOrder = new HashMap<>();
            String itemKeyString;
            MenuItemModel itemValue;
            for (int i = 0; i < order.size(); i++) {
                itemKeyString = "item-" + i;
                itemValue = order.get(i);
                //Log.d(TAG, "Order item: " + itemValue.getItemName() + " " + itemValue.getItemDescription() + " " + itemValue.getItemPrice());
                tempOrder.put(itemKeyString, itemValue);
            }
//            Log.d(TAG, "Map for writing order in db: " + tempOrder);


            // Make this write the order in the booking
            DocumentReference bookingRef = db.collection("restaurants")
                    .document(table.getRestaurantId())
                    .collection("bookings")
                    .document(table.getBookingId());
            bookingRef.update("order", tempOrder);

            executor.shutdown();
        });
    }


    @Override
    public void onItemClick(int position) {
        //Toast.makeText(getApplicationContext(), order.get(position).itemName + " removed!", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderSelect.this);
        builder.setMessage("Remove " + order.get(position).getItemName() + "?").setTitle(R.string.removeItemDialog).setPositiveButton("Yes", (dialog, id) -> {
            // User clicked OK button
            order.remove(position);
            adapter.notifyItemRemoved(position);
            subtotal = calculateSubtotal(order);

            // Update subtotal
            TextView subtotalText = findViewById(R.id.subtotalTopText);
            @SuppressLint("DefaultLocale") String newText = defaultSubText + String.format("%.2f", subtotal);
            subtotalText.setText(newText);
        })
                .setNegativeButton("No", (dialog, id) -> {
                    // User cancelled the dialog
                    dialog.dismiss();
                })
                .show();

    }

    private float calculateSubtotal(ArrayList<MenuItemModel> items) {
        float itemPriceF;
        float subTotal = 0;
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