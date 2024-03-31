package com.example.LibraryBee;


import java.util.ArrayList;
import java.util.List;

public class Seat {
    private String number;
    private Status status;
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

    public Seat(String number, String status) {
        // Initialize default values if needed
    }

    public Seat(String number, Status status) {
        this.number = number;
        this.status = status;
        this.reserveStatusList = new ArrayList<>(); // Initialize the list
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


}
