package com.example.tableahead.bookings;

import androidx.annotation.NonNull;

// In hind sight, should have simply used the same object instead of Table and Booking
public class Booking {
    private String restaurantName;
    private String restaurantId;
    private long partySize;
    private String date;
    private String time;
    private String bookingId;
    private String tableId;
    private String section;
    private String tableNumber;
    private long tableSeats;
    private String imageUrl;
    private String note;


    public Booking() {

    }


    public String getRestaurantName() {
        return restaurantName;
    }

    public long getPartySize() {
        return partySize;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getRestaurantId() {return restaurantId; }

    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }

    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public void setPartySize(long partySize) { this.partySize = partySize; }

    public void setDate(String date) { this.date = date; }

    public void setTime(String time) { this.time = time; }

    public String getBookingId() { return bookingId; }

    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getTableId() { return tableId; }

    public void setTableId(String tableId) { this.tableId = tableId; }

    public long getTableSeats() { return tableSeats; }

    public void setTableSeats(long tableSeats) { this.tableSeats = tableSeats; }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getNote() { return note; }

    public void setNote(String note) { this.note = note; }

    @NonNull
    @Override
    public String toString() {
        return "Booking{" +
                "restaurantName='" + restaurantName + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                ", partySize=" + partySize +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", tableId='" + tableId + '\'' +
                ", section='" + section + '\'' +
                ", tableNumber='" + tableNumber + '\'' +
                ", tableSeats=" + tableSeats +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
