package com.example.tableahead.bookings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.MenuSelect;
import com.example.tableahead.R;
import com.example.tableahead.Table;
import com.example.tableahead.accountScreen;
import com.example.tableahead.helpScreen;
import com.example.tableahead.models.MenuItemModel;
import com.example.tableahead.search.SelectTypeScreen;
import com.example.tableahead.tableScreen;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** @noinspection FieldCanBeLocal*/
public class BookingOverview extends AppCompatActivity {
//    private static final String TAG = "BookingOverview";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Executor executor = Executors.newSingleThreadExecutor();
    private ArrayList<MenuItemModel> order;
    private BookingMenuAdapterRV adapter;
    private Table table = null;
    private Float subtotal = null;
    private TextView txtRestName;
    private TextView txtDate;
    private TextView txtTime;
    private TextView txtTableNum;
    private TextView txtSection;
    private TextView txtSubtotal;
    private TextView txtSeats;
    private TextView txtPartySize;
    private EditText editTxtNote;
    static final int UPDATE_REQUEST_CODE = 4;
    static final int ORDER_REQUEST_CODE = 5;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_overview);

        //Toolbar
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

        // get intent data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //noinspection unchecked
            order = (ArrayList<MenuItemModel>) extras.get("order");
            table = (Table) extras.get("table");
            subtotal = (Float) extras.get("subtotal");

            if (subtotal == null && order != null) {
                // if we don't receive a subtotal, calculate it.
                subtotal = calculateSubtotal(order);
            }
            else if (subtotal == null) {
                subtotal = 0f;
            }
        }
        if (order == null) {
            // Read the map from database
            order = new ArrayList<>();
            getOrderFromDatabase();
        }
        if (order != null){
            makeNewRecyclerView();
        }

        // set text fields
        txtRestName = findViewById(R.id.txtRestNameOverview);
        txtDate = findViewById(R.id.txtDateOverview);
        txtTime = findViewById(R.id.txtTimeOverview);
        txtTableNum = findViewById(R.id.txtTableNumberOverview);
        txtSection = findViewById(R.id.txtSectionOverView);
        txtSubtotal = findViewById(R.id.txtSubtotalOverview);
        txtSeats = findViewById(R.id.txtTableSeatsOverview);
        txtPartySize = findViewById(R.id.txtPartySizeOverview);
        txtSubtotal = findViewById(R.id.txtSubtotalOverview);
        editTxtNote = findViewById(R.id.editTxtNote);

        txtSubtotal.setText(String.format(Locale.US, "%.2f", subtotal));
        setTextFields(table);

        // buttons
        Button btnCancelBooking = findViewById(R.id.btnCancelBooking);
        btnCancelBooking.setOnClickListener(v -> showDeleteConfirmation(table));
        Button btnChangeBooking = findViewById(R.id.btnChangeTimeOver);
        btnChangeBooking.setOnClickListener(v -> showTableScreen(table));
        Button btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            updateNote();
            returnToBookings();
        });
        Button btnChangeOrder = findViewById(R.id.btnChangeOrder);
        btnChangeOrder.setOnClickListener(v -> changeOrder(order));

    }
    private void setTextFields(Table table){
        txtRestName.setText(table.getRestaurantName());
        txtDate.setText(userBookings.parseDate(table.getBookingDate()));
        txtTime.setText(table.getBookingTime());
        txtSection.setText(table.getSection());
        txtTableNum.setText(table.getTableNumber());
        txtSeats.setText(String.valueOf(table.getTableSeats()));
        txtPartySize.setText(String.valueOf(table.getPartySize()));
        if (table.getNote() != null) {
            editTxtNote.setText(table.getNote());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void makeNewRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rvMenuOverview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingMenuAdapterRV(this, order, this::showAlertDialog);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /** @noinspection unchecked*/
    private void getOrderFromDatabase() {
        DocumentReference orderRef = db.collection("restaurants")
                .document(table.getRestaurantId())
                .collection("bookings")
                .document(table.getBookingId());

        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    ArrayList<MenuItemModel> readItems = new ArrayList<>();
                    HashMap<String, HashMap<String, String>> tempOrder = ((HashMap<String, HashMap<String, String>>) document.getData().get("order"));
                    if (tempOrder != null) {
                        for (Map.Entry<String, HashMap<String, String>> entry : tempOrder.entrySet()) {
                            HashMap<String, String> value = entry.getValue();
                            String itemName = value.get("itemName");
                            String itemDescription = value.get("itemDescription");
                            String itemPrice = value.get("itemPrice");
                            readItems.add(new MenuItemModel(itemName, itemDescription, itemPrice));
                        }
                        subtotal = calculateSubtotal(readItems);
                        txtSubtotal.setText(String.format(Locale.US, "%.2f", subtotal));
                        fillItemsArray(readItems);
                    }
//                    Log.d(TAG, "readItems arraylist: " + readItems);
                }
//                else { Log.d(TAG, "No such document");}
            }
//            else { Log.d(TAG, "get failed with ", task.getException()); }
        });
    }

    private void fillItemsArray(ArrayList<MenuItemModel> items) {
        if (items != null) {
            order = items;
            makeNewRecyclerView();
//            Log.d(TAG, "Order array filled: " + order);
        }
    }

    /**
     * Do something with menu item clicks
     * - show item description
     *
     * @param menuItem menu item object.
     * @noinspection EmptyMethod
     */
    private void showAlertDialog(MenuItemModel menuItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(menuItem.getItemName());
        builder.setMessage(menuItem.getItemDescription());

        builder.setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.dismiss());
        builder.create().show();
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

    private void showDeleteConfirmation(Table table){
        AlertDialog.Builder builderConfirm = new AlertDialog.Builder(this);
        builderConfirm.setTitle(getString(R.string.confirm))
                .setMessage(getString(R.string.deleteConfirm))
                .setPositiveButton(getString(R.string.yes), (dialog2, id2) -> {
                    // Delete Booking
                    deleteBooking(table);
                })
                .setNegativeButton(getString(R.string.no), (dialog2, id2) -> dialog2.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteBooking(Table table) {
        DocumentReference bookingRef = db.collection("restaurants")
                .document(table.getRestaurantId())
                .collection("bookings")
                .document(table.getBookingId());
        bookingRef.delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, getString(R.string.bookingDeleted), Toast.LENGTH_LONG).show();
//                    Log.d(TAG, "Booking deleted successfully.");
                    setResult(Activity.RESULT_OK, new Intent().putExtra("resultCode", "BOOKING_DELETED"));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.error_deleting_booking), Toast.LENGTH_LONG).show();
//                    Log.e(TAG, "Error deleting booking: " + e.getMessage());
                });
    }

    // Change order
    /** @noinspection deprecation*/
    private void changeOrder(ArrayList<MenuItemModel> items) {
        // Goes from BookingOverview -> MenuSelect -> OrderSelect and returns when OrderSelect is finished
        Intent intent = new Intent(this, MenuSelect.class);
        intent.putExtra("editing", true);
        intent.putExtra("order", items);
        intent.putExtra("restaurantId", table.getRestaurantId());
        intent.putExtra("table", table);
//        Log.d(TAG, "Going to MenuSelect");
        startActivityForResult(intent, ORDER_REQUEST_CODE);
        //startActivity(intent);
    }

    /** @noinspection deprecation*/ // Change Date, Time, Table, or guests.
    private void showTableScreen(Table table) {
        Intent intent = new Intent(this, tableScreen.class);
        intent.putExtra("table", table);
//        Log.d(TAG, "Going to tableScreen");
        startActivityForResult(intent, UPDATE_REQUEST_CODE);
//        startActivity(intent);
    }

    private void updateNote(){
        table.setNote(editTxtNote.getText().toString());
        executor.execute(() -> {
            DocumentReference bookingRef = db.collection("restaurants")
                    .document(table.getRestaurantId())
                    .collection("bookings")
                    .document(table.getBookingId());
            bookingRef.update("note", editTxtNote.getText().toString())
//            .addOnFailureListener(e -> Log.e(TAG, "Failed to update note."))
            ;
        });
    }

    // confirm button clicked
    private void returnToBookings(){
//        Log.d(TAG, "RETURNING TO BOOKINGS");
        Intent intent = new Intent(this, userBookings.class);
        intent.putExtra("resultCode", "BOOKING_UPDATED");
        intent.putExtra("table", table);
        setResult(Activity.RESULT_OK, intent);
//        startActivity(intent);
        finish();
    }

    /** @noinspection unchecked*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
//        Log.d(TAG, "BookingOverview: onActivityResult called ");

        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                assert intent != null;
                String result = intent.getStringExtra("resultCode");
                if ("BOOKING_UPDATED".equals(result)) {
                    table = intent.getParcelableExtra("table");
                    assert table != null;
                    setTextFields(table);
                }
            }
        }
        if (requestCode == ORDER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
//                Log.d(TAG, "BookingOverview ORDER_REQUEST_CODE return OK");
                assert intent != null;
                //order = intent.getParcelableExtra("result");
                // Get the order
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    ArrayList<MenuItemModel> tempOrder = (ArrayList<MenuItemModel>) extras.get("result");
                    //Log.d(TAG, "BookingOverview TempOrder: " + tempOrder);
                    if (tempOrder != null) {
                        order = tempOrder;
                        subtotal = calculateSubtotal(order);
                        txtSubtotal = findViewById(R.id.txtSubtotalOverview);
                        txtSubtotal.setText(String.format(Locale.US, "%.2f", subtotal));

                        // Uhh
                        makeNewRecyclerView();
                    }
                }
//                Log.d(TAG, "BookingOverview got order: " + order);
            }
        }
    }
}