package com.example.tableahead.bookings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tableahead.R;
import com.example.tableahead.Table;
import com.example.tableahead.TableLoadFragment;
import com.example.tableahead.accountScreen;
import com.example.tableahead.helpScreen;
import com.example.tableahead.search.SelectTypeScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class userBookings extends AppCompatActivity {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final String TAG = "userBookings";
    BookingAdapterRV adapter;
    List<Booking> bookingList = new ArrayList<>();
    final TableLoadFragment loadingFragment = new TableLoadFragment();
    // used to remove items from RV
    int position;
    static final int CANCEL_REQUEST_CODE = 1;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    /** @noinspection FieldMayBeFinal, FieldCanBeLocal */
    private final String userId = mAuth.getUid();
    private String date;

    /** @noinspection SpellCheckingInspection*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bookings);

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

        // Get today's date.
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        date = dateFormat.format(currentDate);

        RecyclerView recyclerView = findViewById(R.id.bookingRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapterRV(this, new ArrayList<>(), this::bookingClicked);

        getBookings();

        recyclerView.setAdapter(adapter);

        showLoadingFragment();
    }

    /**
     *
     */
    private void getBookings() {
        // query bookings and tables
        executor.execute(() -> {
            try {
                bookingList = queryBookings();
                runOnUiThread(() -> {
//                    Log.d(TAG, "booking list size " + bookingList.size());
                    adapter.setItems(bookingList);
                });
            } catch (Exception e) {
                displayToast("An error occurred getting tables: " + e.getMessage());
            }
        });

    }

    /**
     *
     * @return List<Booking>
     * @noinspection DataFlowIssue
     */
    private List<Booking> queryBookings() {
        List<Booking> bookingList = new ArrayList<>();
        Query query = db.collectionGroup("bookings")
                .whereEqualTo("userID", userId)
                .whereGreaterThanOrEqualTo("date", date)
                .orderBy("date", Query.Direction.ASCENDING);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (queryDocumentSnapshots.isEmpty()){
                hideLoadingFragment();
                displayToast("No Bookings Found");
            }
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Booking booking = new Booking();

                String restaurantId = document.getReference().getParent().getParent().getId();

                booking.setBookingId(document.getId());
                booking.setDate(document.getString("date"));
                booking.setTime(document.getString("time"));
                booking.setPartySize(document.getLong("guests"));
                booking.setNote(document.getString("note"));
                booking.setRestaurantId(restaurantId);

                DocumentReference tableRef = document.getDocumentReference("table");

                getTableData(booking, restaurantId, tableRef);
            }
        });

        return bookingList;
    }

    /**
     *
     * @param booking the booking object to update
     * @param restaurantId the restaurant id
     * @param tableRef reference to table booked
     * @noinspection DataFlowIssue
     */
    private void getTableData(Booking booking, String restaurantId, DocumentReference tableRef){
        String tableId = tableRef.getId();
        // Fetch from table document
        tableRef.get().addOnSuccessListener(tableDocument -> {
            if (tableDocument.exists()) {

                long tableSeats = tableDocument.getLong("seats");

                booking.setTableSeats(tableSeats);
                booking.setSection(tableDocument.getString("section"));
                booking.setTableNumber(tableDocument.getString("tableNumber"));
                booking.setTableId(tableId);

                // continue
                getRestaurantData(booking, restaurantId);
            }
//            else { Log.e(TAG, "Table document does not exist for ID: " + tableId); }
        });
    }

    /**
     *
     * @param booking booking object to update
     * @param restaurantId the restaurantId
     */
    private void getRestaurantData(Booking booking, String restaurantId) {
        // Fetch from parent restaurant document
        DocumentReference restaurantRef = db.collection("restaurants").document(restaurantId);
        restaurantRef.get().addOnSuccessListener(restaurantDocument -> {
            if (restaurantDocument.exists()) {

                String restaurantName = restaurantDocument.getString("name");
                String imageUrl = restaurantDocument.getString("imageLink");
                booking.setRestaurantName(restaurantName);
                booking.setImageUrl(imageUrl);
//                            Log.d(TAG, "restaurant found " + restaurantName);
            } else {
                booking.setRestaurantName("Error Name not found");
//                Log.d(TAG, "Restaurant document does not exist for ID: " + restaurantId);
            }
//            Log.d(TAG, "booking added " + booking);

            bookingList.add(booking);
//                    runOnUiThread(() -> { adapter.notifyDataSetChanged(); });
            runOnUiThread(() -> adapter.notifyItemInserted(bookingList.size()-1));
            hideLoadingFragment();
        });

    }


    /**
     * format "2023-11-21" to "Nov, 21 2023"
     * convert date
     * @param date  date to parse
     * @return date String of Date
     */
    public static String parseDate(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date newDate = inputFormat.parse(date);

            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM, dd yyyy", Locale.getDefault());
            assert newDate != null;
            return outputFormat.format(newDate);
        } catch (ParseException e) {
            return date;
        }
    }

    /**
     * Go straight to Booking overview
     * @param booking Booking object
     * @noinspection unused
     */
    private void bookingClicked(Booking booking, int position){
        this.position = bookingList.indexOf(booking);
        showBookingOverview(booking);
    }

    /** @noinspection deprecation*/
    private void showBookingOverview(Booking booking){
        // copy our Booking to a parcelable Table
        Table table = new Table(booking);

        Intent intent = new Intent(this, BookingOverview.class);
//        intent.putExtra("order", order);
        intent.putExtra("table", table);
//        setResult(Activity.RESULT_OK); // this finishes parent activity
        startActivityForResult(intent, CANCEL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == CANCEL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                assert intent != null;
                String result = intent.getStringExtra("resultCode");
                if ("BOOKING_DELETED".equals(result)) {
                    bookingList.remove(this.position);
                    adapter.notifyItemRemoved(this.position);
//                    Log.d(TAG, "removed booking at position" + this.position);
                } else if("BOOKING_UPDATED".equals(result)){
                    Table updatedTable = intent.getParcelableExtra("table");
                    Booking booking = bookingList.get(this.position);
//                    Log.d(TAG, "booking " + booking.toString());
                    assert updatedTable != null;
                    Booking updatedBooking = updateBooking(booking, updatedTable);
//                    Log.d(TAG, "booking " + updatedBooking.toString());

                    // replace the booking
                    bookingList.remove(this.position);
//                    Log.d(TAG, "booking REMOVED");
                    adapter.notifyItemRemoved(this.position);
                    bookingList.add(this.position, updatedBooking);
                    adapter.notifyItemInserted(this.position);
//                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private Booking updateBooking(Booking booking, Table table){
        booking.setDate(table.getBookingDate());
        booking.setTime(table.getBookingTime());
        booking.setTableNumber(table.getTableNumber());
        booking.setTableSeats(table.getTableSeats());
        booking.setPartySize(table.getPartySize());
        booking.setSection(table.getSection());
        booking.setNote(table.getNote());
        return booking;
    }

    /**
     *
     */
    private void showLoadingFragment(){
        getSupportFragmentManager().beginTransaction()
                .add(R.id.loadFragmentContainer2, loadingFragment)
                .commit();
    }

    /**
     *
     */
    private void hideLoadingFragment(){
        getSupportFragmentManager().beginTransaction()
                .remove(loadingFragment)
                .commit();
    }

    /**
     *
     * @param message  message to display in toast
     */
    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}