package com.example.tableahead;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.tableahead.bookings.Booking;

public class Table implements Parcelable {
    private String restaurantName;
    private String tableNumber;
    private long tableSeats;
    private long partySize;
    private String section;
    private String restaurantId;
    private String bookingId;
    private String tableId;
    private String bookingDate;
    private String bookingTime;
    private String note;


    public Table() {
    }

    public Table(Booking booking) {
        this.restaurantName = booking.getRestaurantName();
        this.tableNumber = booking.getTableNumber();
        this.tableSeats = booking.getTableSeats();
        this.partySize = booking.getPartySize();
        this.section = booking.getSection();
        this.restaurantId = booking.getRestaurantId();
        this.bookingId = booking.getBookingId();
        this.tableId = booking.getTableId();
        this.bookingDate = booking.getDate();
        this.bookingTime = booking.getTime();
        this.note = booking.getNote();
    }

    public Table(String tableId, String restaurant, String tableNumber, Long seats, String section) {
        this.tableId = tableId;
        this.restaurantName = restaurant;
        this.tableNumber = tableNumber;
        this.tableSeats = seats;
        this.section = section;
    }


    public void setRestaurantName(String restaurant) {
        this.restaurantName = restaurant;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public Long getSeats() {
        return tableSeats;
    }

    public String getSection() {
        return section;
    }

    public String getTableId() {
        return tableId;
    }

    public long getTableSeats() {return tableSeats; }

    public void setTableSeats(long tableSeats) { this.tableSeats = tableSeats; }

    public String getRestaurantId() { return restaurantId; }

    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getBookingId() { return bookingId; }

    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public long getPartySize() { return partySize; }

    public void setPartySize(long partySize) { this.partySize = partySize; }

    public String getBookingDate() { return bookingDate; }

    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getNote() { return note; }

    public void setNote(String note) { this.note = note; }


    @NonNull
    @Override
    public String toString() {
        return "Table{" +
                "tableId='" + tableId + '\'' +
                ", tableSeats=" + tableSeats +
                ", section='" + section + '\'' +
                ", tableNum='" + tableNumber + '\'' +
                ", restName='" + restaurantName + '\'' +
                ", partySize=" + partySize +
                ", restId='" + restaurantId + '\'' +
                ", bookId='" + bookingId + '\'' +
                ", bookTime='" + bookingTime + '\'' +
                ", bookDate='" + bookingDate + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(restaurantName);
        dest.writeString(tableNumber);
        dest.writeLong(tableSeats);
        dest.writeLong(partySize);
        dest.writeString(section);
        dest.writeString(restaurantId);
        dest.writeString(bookingId);
        dest.writeString(tableId);
        dest.writeString(bookingTime);
        dest.writeString(bookingDate);
        dest.writeString(note);

    }

    public static final Parcelable.Creator<Table> CREATOR = new Parcelable.Creator<Table>() {
        @Override
        public Table createFromParcel(Parcel in) {
            return new Table(in);
        }
        @Override
        public Table[] newArray(int size) {
            return new Table[size];
        }
    };

    private Table(Parcel in) {
        restaurantName = in.readString();
        tableNumber = in.readString();
        tableSeats = in.readLong();
        partySize = in.readLong();
        section = in.readString();
        restaurantId = in.readString();
        bookingId = in.readString();
        tableId = in.readString();
        bookingTime= in.readString();
        bookingDate = in.readString();
        note = in.readString();
    }
}
