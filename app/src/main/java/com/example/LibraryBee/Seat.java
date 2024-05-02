package com.example.LibraryBee;

import java.util.ArrayList;
import java.util.List;

public class Seat {
    private String number;
    private Status status;
    private long reservationTimestamp; // Timestamp for reservation
    private List<ReserveStatus> reserveStatusList; // List to hold reserve statuses

    public enum Status {
        AVAILABLE,
        SELECTED,
        RESERVED
    }

    public enum ReserveStatus {
        MORNING,
        EVENING,
        FULL_DAY
    }

    // Updated constructor to include reservation timestamp
    public Seat(String number, Status status, long reservationTimestamp) {
        this.number = number;
        this.status = status;
        this.reservationTimestamp = reservationTimestamp;
        this.reserveStatusList = new ArrayList<>(2); // Initialize the list
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getReservationTimestamp() {
        return reservationTimestamp;
    }

    public void setReservationTimestamp(long reservationTimestamp) {
        this.reservationTimestamp = reservationTimestamp;
    }

    public List<ReserveStatus> getReserveStatusList() {
        return reserveStatusList;
    }

    @Override
    public String toString() {
        return  number + ", Status: " + status;
    }

    public void setReserveStatusList(List<ReserveStatus> reserveStatusList) {
        this.reserveStatusList = reserveStatusList;
    }

    public boolean hasReserveStatus(ReserveStatus status) {
        return reserveStatusList != null && reserveStatusList.contains(status);
    }

    public void addReserveStatus(ReserveStatus status) {
        if (reserveStatusList == null) {
            reserveStatusList = new ArrayList<>();
        }
        reserveStatusList.add(status);
    }
}
