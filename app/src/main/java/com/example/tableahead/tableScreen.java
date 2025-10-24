package com.example.tableahead;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.search.SelectTypeScreen;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class tableScreen extends AppCompatActivity implements TableCustomDialog.DialogListener  {
//    private static final String TAG = "tableScreen";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final FirebaseFirestore  db = FirebaseFirestore.getInstance();

    /** @noinspection FieldMayBeFinal*/
    private String restaurantId = "RfGanWYlN67GAVgcZvu4"; // Default Id. Received with intent
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    /** @noinspection FieldMayBeFinal, FieldCanBeLocal */
    private final String userId = mAuth.getUid();
    /** @noinspection FieldMayBeFinal*/
    private final int intervalMinutes = 60; // could be received from restaurant
    /** @noinspection SpellCheckingInspection*/
    private String restoName;   // Restaurant Name
    private String date_string; // booking date in string format "yyyy-mm-dd"
    private String time_string; //booking time in string format "HH-mm"
    private String open_time;   // restaurant open time "9:00 AM"
    private String close_time;  // restaurant close time "10:00 PM"
    TextView textViewRestaurantName;
    private RecyclerView recyclerViewTables;
    /** @noinspection FieldMayBeFinal*/
    private final ArrayList<Table> tableListRV = new ArrayList<>();
    private tableDataRVAdapter adapter;
    private Button party_date_time_button;
    private int current_party_size = 2;
    private Date bookingDate;
    private Boolean dateChangedFlag = Boolean.FALSE;
    final Calendar bookingCalendar = Calendar.getInstance();
    final TableLoadFragment loadingFragment = new TableLoadFragment();
    // This is passed from booking overview when you want to change time
    Table tableBookingOverview = null;

    // doing any conversions, use this
    final SimpleDateFormat bookingTimeButtonFormat = new SimpleDateFormat("EEE, MMM d '-' h:'00' a", Locale.US);
    // use this to display
    SimpleDateFormat buttonThisLocale ;

    /**
     *  suggest the nearest available time when the slot is fully booked
     *  if you know how many tables a restaurant has. you can query all bookings after a date.
     *  sort by time and count each time slot until you have an empty table.
     *  */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_screen);

        buttonThisLocale = new SimpleDateFormat("EEE, MMM d '-' h:'00' a", Locale.getDefault());

        Intent intent = getIntent();
        if (intent != null){
            tableBookingOverview = intent.getParcelableExtra("table");
            if (tableBookingOverview != null){
                // We are coming from the booking overview to change time or table
                restaurantId = tableBookingOverview.getRestaurantId();
                current_party_size = (int) tableBookingOverview.getPartySize();
            }
            else if (intent.getStringExtra("restaurantId") != null) {
                restaurantId = intent.getStringExtra("restaurantId");
            }

        }

        party_date_time_button = findViewById(R.id.btn_date_time_party);
        showLoadingFragment();

        getRestaurantDetails(restaurantId);

        // views
        textViewRestaurantName = findViewById(R.id.restaurantName);
//        TextView loadText = findViewById(R.id.loadText);

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

        recyclerViewTables = findViewById(R.id.tableRV);
        recyclerViewTables.setLayoutManager(new LinearLayoutManager(this));
        adapter = new tableDataRVAdapter(this, tableListRV, this::showAlertDialog);
        recyclerViewTables.setAdapter(adapter);

        if (tableBookingOverview == null) {
            // show the tables for the next time interval.
            // get current time and round up to next intervalMinutes.
            bookingDate = new Date();
            bookingCalendar.setTime(bookingDate);

            // set next possible booking period
            bookingCalendar.add(Calendar.MINUTE, this.intervalMinutes);
            bookingDate = bookingCalendar.getTime();
            update_date_time(bookingDate);
        }else{
            // using Table object from booking overview
            update_date_time();
        }
//        showCustomDialog(this.getCurrentFocus()); // if you want to show the dialog right away.
    }

    /**
     * This can be removed if the intent passes more info than just restaurantID
     * @param restId Restaurant ID
     */
    private void getRestaurantDetails(String restId){
        DocumentReference restaurantRef = db.collection("restaurants").document(restId);
        restaurantRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                this.restoName = documentSnapshot.getString("name");
                this.open_time = documentSnapshot.getString("open_time");
                this.close_time = documentSnapshot.getString("close_time");

                textViewRestaurantName.setText(String.format("%s - %s", restoName, getString(R.string.choose_a_table)));

                // if first available booking is before today's open or after closing then increase days by 1
                try {
                    if (tableBookingOverview == null) {
                        check_Hours();
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                // query upon startup
                if (tableBookingOverview == null) {
                    getAvailableTables(restaurantId,
                            this.date_string,
                            this.time_string);
                }else{
                    getAvailableTables(restaurantId,
                            tableBookingOverview.getBookingDate(),
                            tableBookingOverview.getBookingTime());
                }
            }
        });
    }
    /**
     * checks if bookingDate is outside of restaurants working hours
     * change next available booking accordingly
     */
    private void check_Hours() throws ParseException {

        //  date and time is not valid after 12:00 AM. it assumes the same day.
        //  // main issue is CustomDialog. too much work to redo now.

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US);
        Calendar targetCloseTimeCalendar = Calendar.getInstance();
        Calendar targetOpenTimeCalendar = Calendar.getInstance();
        Date targetCloseTime;
        Date targetOpenTime;
        try {
            targetCloseTime = dateFormat.parse(this.date_string + " " + this.close_time);
            targetOpenTime = dateFormat.parse(this.date_string + " " + this.open_time);
//            Log.d(TAG, "open dateTime: " + targetOpenTime + "close dateTime: " + targetCloseTime);
            assert targetCloseTime != null;
            targetCloseTimeCalendar.setTime(targetCloseTime);

            // if the restaurant is open till 12am or later. add a day to close
            if (targetCloseTime.before(targetOpenTime))
                targetCloseTimeCalendar.add(Calendar.HOUR_OF_DAY, 24);
            assert targetOpenTime != null;
            targetOpenTimeCalendar.setTime(targetOpenTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // current time
        Calendar currentTimeCalendar = Calendar.getInstance();
        currentTimeCalendar.add(Calendar.MINUTE, this.intervalMinutes);

        // Check if the current time is after the closing time
        boolean isCurrentTimeAfterCloseTime = currentTimeCalendar.after(targetCloseTimeCalendar)
                || currentTimeCalendar.equals(targetCloseTimeCalendar);

        if (isCurrentTimeAfterCloseTime) {
            displayToast( getString(R.string.too_late) + " "+ targetCloseTimeCalendar.getTime() + " "+ currentTimeCalendar.getTime());
            // increase days by one and set next booking to opening time.
            bookingCalendar.add(Calendar.DAY_OF_YEAR, 1);
            this.dateChangedFlag = Boolean.TRUE;
            dateFormat = new SimpleDateFormat("h:mm a", Locale.US);
            targetCloseTime = dateFormat.parse(this.open_time);
            assert targetCloseTime != null;
            targetCloseTimeCalendar.setTime(targetCloseTime);
            bookingCalendar.set(Calendar.HOUR_OF_DAY, targetCloseTimeCalendar.get(Calendar.HOUR_OF_DAY));
            bookingDate = bookingCalendar.getTime();
            update_date_time(bookingDate);

        }
        // Check if the current time is the same day but before Open
        boolean isCurrentTimeBeforeOpenTime = currentTimeCalendar.before(targetOpenTimeCalendar);

        if (isCurrentTimeBeforeOpenTime) {
            displayToast( getString(R.string.too_early) + targetCloseTimeCalendar.getTime() + " "+ currentTimeCalendar.getTime());
            // increase days by one and set next booking to opening time.
            dateFormat = new SimpleDateFormat("h:mm a", Locale.US);
            targetCloseTime = dateFormat.parse(this.open_time);
            assert targetCloseTime != null;
            targetCloseTimeCalendar.setTime(targetCloseTime);
            bookingCalendar.set(Calendar.HOUR_OF_DAY, targetCloseTimeCalendar.get(Calendar.HOUR_OF_DAY));
            bookingDate = bookingCalendar.getTime();
            update_date_time(bookingDate);
        }
    }
    /**
     * Updates date_string and time.
     * "yyyy-MM-dd"
     * "h:'00' a"
     * @param date new date
     */
    private void update_date_time(Date date) {
        SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        this.date_string = sdf_date.format(date);
        sdf_date = new SimpleDateFormat("h:'00' a", Locale.US);
        this.time_string = sdf_date.format(date);
        String buttonDate = bookingTimeButtonFormat.format(bookingDate);
        String displayButtonDate = buttonThisLocale.format(bookingDate);
        party_date_time_button.setTag(String.format("%s - %s",this.current_party_size, buttonDate));
        party_date_time_button.setText(String.format("%s - %s",this.current_party_size, displayButtonDate));
    }

    private void update_date_time() {
        this.date_string = tableBookingOverview.getBookingDate();
        this.time_string = tableBookingOverview.getBookingTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US);
        try {
            bookingDate = dateFormat.parse(this.date_string + " " + this.time_string);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert bookingDate != null;
        String buttonDate = bookingTimeButtonFormat.format(bookingDate);
        String displayButtonDate = buttonThisLocale.format(bookingDate);
        party_date_time_button.setTag(String.format("%s - %s",tableBookingOverview.getPartySize(), buttonDate));
        party_date_time_button.setText(String.format("%s - %s",tableBookingOverview.getPartySize(), displayButtonDate));
    }

    /**
     * retrieves available tables for a specific time and date.
     *     populates recycler view.
     *    uses executor
     * @param restaurantId document ID
     * @param date  "2023-10-26"
     * @param time  "18:30"
     */
    @SuppressLint("NotifyDataSetChanged")
    private void getAvailableTables(String restaurantId, String date, String time) {

        CollectionReference restaurantBookingsRef = db.collection("restaurants").document(restaurantId).collection("bookings");
        tableListRV.clear();
        adapter.notifyDataSetChanged();
        // Query bookings and tables for availability
        executor.execute(() -> {
            try {
                queryBookingsForAvailability(restaurantBookingsRef, date, time);
            } catch (Exception e) {
                displayToast(getString(R.string.error_getting_tables) + e.getMessage());
            }
        });
    }
    private void queryBookingsForAvailability(CollectionReference restaurantBookingsRef, String date, String time) {
        // Query bookings for the specified date and time
        CollectionReference restaurantTablesRef = db.collection("restaurants").document(restaurantId).collection("tables");
        Task<QuerySnapshot> bookingsTask = restaurantBookingsRef.whereEqualTo("date", date).whereEqualTo("time", time).get();
        List<String> bookedTableIds = new ArrayList<>();

        bookingsTask.addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot booking : queryDocumentSnapshots) {
                DocumentReference bookedTableRef = booking.getDocumentReference("table");
                if (bookedTableRef != null) {

                    bookedTableIds.add(bookedTableRef.getId());
//                    Log.d(TAG,"table already booked ");
                }
            }
            runOnUiThread(() -> {
                // Filter available tables based on bookings
                queryAvailableTables(restaurantTablesRef, this.current_party_size, bookedTableIds);
            });
        });
    }
    @SuppressLint("NotifyDataSetChanged")
    private void queryAvailableTables(CollectionReference restaurantTablesRef, int currentPartySize, List<String> fullyBookedTableIds) {
        restaurantTablesRef.whereGreaterThanOrEqualTo("seats", currentPartySize).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot tableDoc : queryDocumentSnapshots) {
                        if (!fullyBookedTableIds.contains(tableDoc.getId())
                                // if modifying a booking, show the same table you already booked.
                        || (tableBookingOverview != null && tableBookingOverview.getTableId().equals(tableDoc.getId()))) {
                            Table availTable = new Table(
                                    tableDoc.getId(),
                                    tableDoc.getString("restaurant"),
                                    tableDoc.getString("tableNumber"),
                                    tableDoc.getLong("seats"),
                                    tableDoc.getString("section"));
                            tableListRV.add(availTable);
                        }
                    }
        // Update the UI with the retrieved data
        runOnUiThread(() -> {
            if (tableListRV.isEmpty()) {
                displaySnackBar(getString(R.string.noTablesFound));
            }
            adapter.notifyDataSetChanged();
            hideLoadingFragment();
        });
    });
    }

    /**
     * Shows confirmation dialog for a table.
     *
     * @param table table object.
     */
    private void showAlertDialog(Table table) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmation));
        String message = String.format("" +
                "%s %s \n" +
                "%s %s \n" +
                "%s %s \n", getString(R.string.tableNumber), table.getTableNumber(),
                getString(R.string.table_row_section), table.getSection(),
                getString(R.string.seats), table.getSeats());
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.bookTable), (dialog, id) -> {
            if (tableBookingOverview == null) {
                book_table(table);
            }else {
                updateBooking(table);
            }
            displayToast(getString(R.string.booking_accepted));
//            completeTableBooking();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            // do nothing
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void book_table(Table table){
        // TODO to not double book, do a quick check that table has not been booked.
//        Log.d(TAG, "Booking this table: "+ table);
        CollectionReference restaurantBookingsRef = db.collection("restaurants")
                .document(restaurantId).collection("bookings");
        DocumentReference tableRef = db.collection("restaurants")
                .document(restaurantId).collection("tables").document(table.getTableId());

        // Provide details for next activity
//        table.setTableRef(tableRef);
        table.setRestaurantName(this.restoName);
        table.setRestaurantId(this.restaurantId);
        table.setPartySize(this.current_party_size);

        Map<String, Object> bookingDoc = new HashMap<>();
        bookingDoc.put("date", this.date_string);
        bookingDoc.put("guests", this.current_party_size);
        bookingDoc.put("table", tableRef);
        bookingDoc.put("time", this.time_string);
        bookingDoc.put("userID", this.userId);

        restaurantBookingsRef.add(bookingDoc)
                .addOnSuccessListener(documentReference -> {
                    table.setBookingId(documentReference.getId());
                    table.setBookingTime(time_string);
                    table.setBookingDate(date_string);
                    incrementBookingCounter(table);
//                    Log.d(TAG, "Table successfully booked " +  documentReference.getId());
                })
//                .addOnFailureListener(e -> Log.e(TAG, "Error booking table: " + e.getMessage()))
        ;
    }

    public void updateBooking(Table table) {
        // tableId is received from recycler view Table
        DocumentReference tableRef = db.collection("restaurants")
                .document(restaurantId).collection("tables").document(table.getTableId());
        DocumentReference documentRef = db.collection("restaurants")
                .document(tableBookingOverview.getRestaurantId()).collection("bookings")
                .document(tableBookingOverview.getBookingId());

        Map<String, Object> newBooking = new HashMap<>();
        newBooking.put("date", this.date_string);
        newBooking.put("table", tableRef);
        newBooking.put("time", this.time_string);
        newBooking.put("guests", this.current_party_size);

        documentRef
                .update(newBooking)
                .addOnSuccessListener(a -> {
                    // Document updated successfully
                    // return object and update booking overview
                    tableBookingOverview.setBookingDate(this.date_string);
                    tableBookingOverview.setBookingTime(this.time_string);
                    tableBookingOverview.setPartySize(this.current_party_size);
                    tableBookingOverview.setTableNumber(table.getTableNumber());
                    tableBookingOverview.setTableSeats(table.getTableSeats());
                    Intent intent = new Intent();
                    intent.putExtra("resultCode", "BOOKING_UPDATED");
                    intent.putExtra("table", tableBookingOverview);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                })
                .addOnFailureListener(e -> {
//                    Log.d(TAG, "Failed to update booking");
                });
    }

    private void incrementBookingCounter(Table table) {
        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);
        // Atomically increment the counter by 1
        restaurantRef.update("popularity", FieldValue.increment(1))
                .addOnSuccessListener(a -> {
//                    Log.d(TAG, "Booking counter incremented.");
                    completeTableBooking(table);
                })
//                .addOnFailureListener(e -> Log.e(TAG, "Failed to increment booking counter: " + e.getMessage()))
        ;
    }

    /**
     * Shows date, time and party size selection dialog for chosen restaurant.
     */
    public void showCustomDialog(View view) {
        if (!isFinishing() && !isDestroyed()) {
            TableCustomDialog dialog = new TableCustomDialog();
            String partySize = party_date_time_button.getTag().toString().split("-")[0].trim();
            dialog.setCurrent_party_size(Integer.parseInt(partySize));
            dialog.setDate_string(this.date_string);
            dialog.setTime(this.time_string);
            dialog.setCalendar(this.bookingCalendar);
            dialog.setClose_time(this.close_time);
            dialog.setOpen_time(this.open_time);
            dialog.setIntervalMinutes(this.intervalMinutes);
            dialog.setListener(this);
            dialog.setDateChangedFlag(this.dateChangedFlag);
            dialog.showDialog(this);
        }
    }

    /**
     * upon completion of Dialog we have some data
     * @param partySize new party size
     * @param date new date
     * @param time  new time
     */
    @Override
    public void onDialogComplete(String partySize, Date date, String time) {
        SimpleDateFormat sdf_date_btn = new SimpleDateFormat("EEE, MMM d", Locale.US);
        SimpleDateFormat sdf_date_btn_locale = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        party_date_time_button.setTag(String.format("%s - %s - %s", partySize, sdf_date_btn.format(date), time));
        party_date_time_button.setText(String.format("%s - %s - %s", partySize, sdf_date_btn_locale.format(date), time));
        current_party_size = Integer.parseInt(partySize);
//        displayToast("getting tables for Party of " + partySize + "  on " + date.toString() + " @ " + time);
        SimpleDateFormat sdf_date = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        bookingDate = date;

        this.date_string = sdf_date.format(date);
        this.time_string = time;

        getAvailableTables(restaurantId,this.date_string,this.time_string);

        showLoadingFragment();
    }

    /** @noinspection SameParameterValue*/
    private void displaySnackBar(String message) {
        Snackbar snackbar = Snackbar.make(recyclerViewTables, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
    private void showLoadingFragment(){
        party_date_time_button.setVisibility(View.INVISIBLE);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.loadFragmentContainer, loadingFragment)
                .commit();
    }
    private void hideLoadingFragment(){
        party_date_time_button.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .remove(loadingFragment)
                .commit();
    }

    private void completeTableBooking(Table table){
        // Activity complete. Go to menu screen
//        Log.d(TAG, "Booking Complete "+ table);
        Intent intent = new Intent(this, MenuSelect.class);
        intent.putExtra("restaurantId", restaurantId);
        intent.putExtra("table", table);
        startActivity(intent);
        finish();

    }


    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}